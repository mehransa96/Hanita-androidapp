package com.example.utils

import android.util.Log
import com.example.data.dao.DailyLogDao
import com.example.data.model.DailyLog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import com.example.data.dao.CycleRecordDao
import com.example.data.model.CycleRecord

class FirestoreManager(
    private val dailyLogDao: DailyLogDao,
    private val cycleRecordDao: CycleRecordDao? = null
) {
    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Failed to initialize Firebase Firestore: ${e.message}")
            null
        }
    }

    /**
     * Upload single log to Firestore
     */
    suspend fun uploadLog(userName: String, log: DailyLog) {
        if (userName.isBlank()) return
        val db = firestore ?: return
        
        withContext(Dispatchers.IO) {
            try {
                val data = mapOf(
                    "dateStr" to log.dateStr,
                    "periodFlow" to log.periodFlow,
                    "symptoms" to log.symptoms,
                    "moods" to log.moods,
                    "notes" to log.notes
                )
                db.collection("users")
                    .document(userName)
                    .collection("logs")
                    .document(log.dateStr)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d("FirestoreManager", "Successfully uploaded log for ${log.dateStr}")
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirestoreManager", "Failed to upload log: ${e.message}")
                    }
            } catch (e: Exception) {
                Log.e("FirestoreManager", "Error in uploadLog: ${e.message}")
            }
        }
    }

    /**
     * Upload all local logs to Firestore
     */
    suspend fun uploadAllLogs(userName: String, logs: List<DailyLog>): Boolean {
        if (userName.isBlank()) return false
        val db = firestore ?: return false

        return withContext(Dispatchers.IO) {
            try {
                val batch = db.batch()
                val userLogsRef = db.collection("users").document(userName).collection("logs")

                for (log in logs) {
                    val logDocRef = userLogsRef.document(log.dateStr)
                    val data = mapOf(
                        "dateStr" to log.dateStr,
                        "periodFlow" to log.periodFlow,
                        "symptoms" to log.symptoms,
                        "moods" to log.moods,
                        "notes" to log.notes
                    )
                    batch.set(logDocRef, data, SetOptions.merge())
                }

                // Execute the batch
                var success = false
                try {
                    batch.commit().await()
                    success = true
                    Log.d("FirestoreManager", "Successfully batch-uploaded ${logs.size} logs.")
                } catch (e: Exception) {
                    Log.e("FirestoreManager", "Batch upload await failed: ${e.message}")
                }
                success
            } catch (e: Exception) {
                Log.e("FirestoreManager", "Error in uploadAllLogs: ${e.message}")
                false
            }
        }
    }

    /**
     * Fetch all logs from Firestore for the given user, and save to local DB
     */
    suspend fun downloadAndSyncLogs(userName: String): Boolean {
        if (userName.isBlank()) return false
        val db = firestore ?: return false

        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = db.collection("users")
                    .document(userName)
                    .collection("logs")
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    for (document in querySnapshot.documents) {
                        val dateStr = document.id
                        val periodFlow = document.getString("periodFlow") ?: "NONE"
                        val symptoms = document.getString("symptoms") ?: ""
                        val moods = document.getString("moods") ?: ""
                        val notes = document.getString("notes") ?: ""

                        val dailyLog = DailyLog(
                            dateStr = dateStr,
                            periodFlow = periodFlow,
                            symptoms = symptoms,
                            moods = moods,
                            notes = notes
                        )
                        dailyLogDao.insertLog(dailyLog)
                    }
                    Log.d("FirestoreManager", "Synced ${querySnapshot.size()} logs from Firestore.")
                    true
                } else {
                    Log.d("FirestoreManager", "No logs found in Firestore for $userName.")
                    false
                }
            } catch (e: Exception) {
                Log.e("FirestoreManager", "Error syncing from Firestore: ${e.message}")
                false
            }
        }
    }

    /**
     * Upload user weight to Firestore
     */
    suspend fun uploadWeight(userName: String, weight: String): Boolean {
        if (userName.isBlank()) return false
        val db = firestore ?: return false
        return withContext(Dispatchers.IO) {
            try {
                val data = mapOf("weight" to weight)
                db.collection("users")
                    .document(userName)
                    .set(data, SetOptions.merge())
                    .await()
                Log.d("FirestoreManager", "Successfully uploaded weight: $weight")
                true
            } catch (e: Exception) {
                Log.e("FirestoreManager", "Failed to upload weight: ${e.message}")
                false
            }
        }
    }

    /**
     * Upload menstrual cycle data to Firestore
     */
    suspend fun uploadCycleData(userName: String, startDate: String, duration: Int): Boolean {
        if (userName.isBlank()) return false
        val db = firestore ?: return false
        return withContext(Dispatchers.IO) {
            try {
                val data = mapOf(
                    "cycleStartDate" to startDate,
                    "cycleDuration" to duration
                )
                db.collection("users")
                    .document(userName)
                    .set(data, SetOptions.merge())
                    .await()
                Log.d("FirestoreManager", "Successfully uploaded cycle data to Firestore: $startDate, $duration")
                true
            } catch (e: Exception) {
                Log.e("FirestoreManager", "Failed to upload cycle data: ${e.message}")
                false
            }
        }
    }

    /**
     * Delete cycle record from Firestore
     */
    suspend fun deleteCycleRecord(userName: String, cycleId: String): Boolean {
        if (userName.isBlank()) return false
        val db = firestore ?: return false
        return withContext(Dispatchers.IO) {
            try {
                db.collection("users")
                    .document(userName)
                    .collection("cycles")
                    .document(cycleId)
                    .delete()
                    .await()
                Log.d("FirestoreManager", "Successfully deleted cycle record from Firestore")
                true
            } catch (e: Exception) {
                Log.e("FirestoreManager", "Failed to delete cycle record: ${e.message}")
                false
            }
        }
    }

    /**
     * Upload full cycle record to Firestore
     */
    suspend fun uploadCycleRecord(userName: String, cycleRecord: CycleRecord): Boolean {
        if (userName.isBlank()) return false
        val db = firestore ?: return false
        return withContext(Dispatchers.IO) {
            try {
                val data = mapOf(
                    "id" to cycleRecord.id,
                    "startDate" to cycleRecord.startDate,
                    "endDate" to cycleRecord.endDate,
                    "notes" to cycleRecord.notes
                )
                db.collection("users")
                    .document(userName)
                    .collection("cycles")
                    .document(cycleRecord.id.ifEmpty { java.util.UUID.randomUUID().toString() })
                    .set(data, SetOptions.merge())
                    .await()
                Log.d("FirestoreManager", "Successfully uploaded cycle record")
                true
            } catch (e: Exception) {
                Log.e("FirestoreManager", "Failed to upload cycle record: ${e.message}")
                false
            }
        }
    }

    /**
     * Download and sync cycle records from Firestore
     */
    suspend fun downloadAndSyncCycleRecords(userName: String): Boolean {
        if (userName.isBlank() || cycleRecordDao == null) return false
        val db = firestore ?: return false

        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = db.collection("users")
                    .document(userName)
                    .collection("cycles")
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    for (document in querySnapshot.documents) {
                        val id = document.getString("id") ?: document.id
                        val startDate = document.getString("startDate") ?: ""
                        val endDate = document.getString("endDate") ?: ""
                        val notes = document.getString("notes") ?: ""

                        val cycleRecord = CycleRecord(
                            id = id,
                            startDate = startDate,
                            endDate = endDate,
                            notes = notes
                        )
                        cycleRecordDao.insertCycleRecord(cycleRecord)
                    }
                    Log.d("FirestoreManager", "Synced ${querySnapshot.size()} cycle records from Firestore.")
                    true
                } else {
                    Log.d("FirestoreManager", "No cycle records found in Firestore for $userName.")
                    false
                }
            } catch (e: Exception) {
                Log.e("FirestoreManager", "Error syncing cycle records from Firestore: ${e.message}")
                false
            }
        }
    }
}
