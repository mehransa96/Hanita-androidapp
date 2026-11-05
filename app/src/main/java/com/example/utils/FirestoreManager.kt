package com.example.utils

import android.util.Log
import com.example.data.dao.CycleRecordDao
import com.example.data.dao.DailyLogDao
import com.example.data.model.CycleRecord
import com.example.data.model.DailyLog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Optional, end-user-controlled cloud backup of the cycle data.
 *
 * Security model:
 *  - Data is written ONLY for a user signed in with Firebase Authentication.
 *  - Every document lives under `users/{uid}/...`, where `uid` is the Firebase Auth id
 *    (never a display name), and `firestore.rules` allows each user to read/write only their own tree.
 *  - Without a signed-in user (offline / name-only mode) nothing leaves the device.
 */
class FirestoreManager(
    private val dailyLogDao: DailyLogDao,
    private val cycleRecordDao: CycleRecordDao
) {
    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore unavailable (Firebase not configured).")
            null
        }
    }

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    /** The signed-in Firebase user id, or null in offline mode. */
    fun currentUid(): String? = auth?.currentUser?.uid

    fun isCloudAvailable(): Boolean = firestore != null && currentUid() != null

    fun signOut() {
        try {
            auth?.signOut()
        } catch (_: Exception) {
        }
    }

    private fun userDoc(uid: String) = firestore!!.collection(USERS).document(uid)

    private fun DailyLog.toMap(): Map<String, Any?> = mapOf(
        "dateStr" to dateStr,
        "periodFlow" to periodFlow,
        "symptoms" to symptoms,
        "moods" to moods,
        "notes" to notes,
        "emoji" to emoji,
        "takenMeds" to takenMeds,
        "waterIntake" to waterIntake,
        "bbt" to bbt?.toDouble(),
        "weight" to weight?.toDouble(),
        "updatedAt" to updatedAt
    )

    private fun DocumentSnapshot.toDailyLog(): DailyLog? {
        val date = getString("dateStr") ?: id
        if (!DATE_REGEX.matches(date)) return null
        return DailyLog(
            dateStr = date,
            periodFlow = getString("periodFlow")?.takeIf { it.length in 1..20 } ?: "NONE",
            symptoms = getString("symptoms").orEmpty().take(MAX_TEXT),
            moods = getString("moods").orEmpty().take(MAX_TEXT),
            notes = getString("notes").orEmpty().take(MAX_TEXT),
            emoji = getString("emoji").orEmpty().take(16),
            takenMeds = getString("takenMeds").orEmpty().take(MAX_TEXT),
            waterIntake = (getLong("waterIntake") ?: 0L).toInt().coerceIn(0, 30),
            bbt = getDouble("bbt")?.toFloat(),
            weight = getDouble("weight")?.toFloat(),
            updatedAt = getLong("updatedAt") ?: 0L
        )
    }

    suspend fun uploadLog(log: DailyLog): Boolean {
        val uid = currentUid() ?: return false
        firestore ?: return false
        return withContext(Dispatchers.IO) {
            try {
                // Re-read the stored row so the uploaded timestamp matches the local one.
                val stored = dailyLogDao.getLogByDate(log.dateStr) ?: log
                userDoc(uid).collection(LOGS).document(stored.dateStr).set(stored.toMap()).await()
                true
            } catch (e: Exception) {
                Log.w(TAG, "uploadLog failed: ${e.javaClass.simpleName}")
                false
            }
        }
    }

    suspend fun uploadCycleRecord(cycleRecord: CycleRecord): Boolean {
        val uid = currentUid() ?: return false
        firestore ?: return false
        if (cycleRecord.id.isBlank()) return false
        return withContext(Dispatchers.IO) {
            try {
                val data = mapOf(
                    "id" to cycleRecord.id,
                    "startDate" to cycleRecord.startDate,
                    "endDate" to cycleRecord.endDate,
                    "notes" to cycleRecord.notes.take(MAX_TEXT)
                )
                userDoc(uid).collection(CYCLES).document(cycleRecord.id).set(data, SetOptions.merge()).await()
                true
            } catch (e: Exception) {
                Log.w(TAG, "uploadCycleRecord failed: ${e.javaClass.simpleName}")
                false
            }
        }
    }

    suspend fun deleteCycleRecord(cycleId: String): Boolean {
        val uid = currentUid() ?: return false
        firestore ?: return false
        return withContext(Dispatchers.IO) {
            try {
                userDoc(uid).collection(CYCLES).document(cycleId).delete().await()
                true
            } catch (e: Exception) {
                Log.w(TAG, "deleteCycleRecord failed: ${e.javaClass.simpleName}")
                false
            }
        }
    }

    suspend fun uploadProfile(cycleLength: Int, weight: String?): Boolean {
        val uid = currentUid() ?: return false
        firestore ?: return false
        return withContext(Dispatchers.IO) {
            try {
                val data = buildMap<String, Any> {
                    put("cycleDuration", cycleLength)
                    weight?.toDoubleOrNull()?.let { put("weight", it) }
                }
                userDoc(uid).set(data, SetOptions.merge()).await()
                true
            } catch (e: Exception) {
                Log.w(TAG, "uploadProfile failed: ${e.javaClass.simpleName}")
                false
            }
        }
    }

    data class SyncResult(val downloaded: Int, val uploaded: Int, val success: Boolean)

    /**
     * Two-way merge: for every day the newest version (by `updatedAt`) wins on both sides,
     * so a sync never overwrites newer local data with older cloud data.
     */
    suspend fun syncAll(): SyncResult {
        val uid = currentUid() ?: return SyncResult(0, 0, false)
        firestore ?: return SyncResult(0, 0, false)
        return withContext(Dispatchers.IO) {
            try {
                val logsRef = userDoc(uid).collection(LOGS)
                val remote = logsRef.get().await().documents.mapNotNull { it.toDailyLog() }.associateBy { it.dateStr }
                val local = dailyLogDao.getAllLogsList().associateBy { it.dateStr }

                var downloaded = 0
                for ((date, remoteLog) in remote) {
                    val localLog = local[date]
                    if (localLog == null || remoteLog.updatedAt > localLog.updatedAt) {
                        dailyLogDao.insertLog(remoteLog) // keeps the remote timestamp
                        downloaded++
                    }
                }

                val toUpload = local.values.filter { localLog ->
                    val remoteLog = remote[localLog.dateStr]
                    remoteLog == null || localLog.updatedAt > remoteLog.updatedAt
                }
                // Firestore batches are limited to 500 writes.
                toUpload.chunked(400).forEach { chunk ->
                    val batch = firestore!!.batch()
                    chunk.forEach { batch.set(logsRef.document(it.dateStr), it.toMap()) }
                    batch.commit().await()
                }

                val cyclesSnapshot = userDoc(uid).collection(CYCLES).get().await()
                for (doc in cyclesSnapshot.documents) {
                    val id = doc.getString("id") ?: doc.id
                    if (cycleRecordDao.getCycleRecordById(id) == null) {
                        cycleRecordDao.insertCycleRecord(
                            CycleRecord(
                                id = id,
                                startDate = doc.getString("startDate").orEmpty(),
                                endDate = doc.getString("endDate").orEmpty(),
                                notes = doc.getString("notes").orEmpty().take(MAX_TEXT)
                            )
                        )
                    }
                }
                SyncResult(downloaded, toUpload.size, true)
            } catch (e: Exception) {
                Log.w(TAG, "syncAll failed: ${e.javaClass.simpleName}")
                SyncResult(0, 0, false)
            }
        }
    }

    /** Lets the user erase everything stored in the cloud for their account. */
    suspend fun deleteAllCloudData(): Boolean {
        val uid = currentUid() ?: return false
        val db = firestore ?: return false
        return withContext(Dispatchers.IO) {
            try {
                for (collection in listOf(LOGS, CYCLES)) {
                    val docs = userDoc(uid).collection(collection).get().await().documents
                    docs.chunked(400).forEach { chunk ->
                        val batch = db.batch()
                        chunk.forEach { batch.delete(it.reference) }
                        batch.commit().await()
                    }
                }
                userDoc(uid).delete().await()
                true
            } catch (e: Exception) {
                Log.w(TAG, "deleteAllCloudData failed: ${e.javaClass.simpleName}")
                false
            }
        }
    }

    companion object {
        private const val TAG = "FirestoreManager"
        private const val USERS = "users"
        private const val LOGS = "logs"
        private const val CYCLES = "cycles"
        private const val MAX_TEXT = 2_000
        private val DATE_REGEX = Regex("""\d{4}-\d{2}-\d{2}""")
    }
}
