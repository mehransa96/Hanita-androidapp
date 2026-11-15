package com.hanita.partner.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/*
 * GentleMan's own database: the things that grow over time (habits and their check-ins, the
 * daily challenge, the journal, self-check results and goals). Small settings and the link with
 * Hanita stay in PartnerStore (SharedPreferences). Everything lives only on this phone: the app
 * has no INTERNET permission and Android backup is turned off.
 */

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    /** The suggested habit this came from (Habits.PRESETS), or null for the user's own. */
    val presetId: String? = null,
    val pillarId: String? = null,
    val position: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "habit_logs",
    primaryKeys = ["habitId", "epochDay"],
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("epochDay")]
)
data class HabitLogEntity(val habitId: Long, val epochDay: Long)

/** A day on which the daily challenge was done. */
@Entity(tableName = "challenge_days")
data class ChallengeDayEntity(@PrimaryKey val epochDay: Long, val pillarId: String? = null)

/** One journal page per day: mood, three good things and a free note. */
@Entity(tableName = "journal")
data class JournalEntity(
    @PrimaryKey val epochDay: Long,
    /** 1..5 (Toolkit.MOODS), 0 = not set. */
    val mood: Int = 0,
    val good1: String = "",
    val good2: String = "",
    val good3: String = "",
    val note: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

/** The good things written on this page (kept outside the entity so Room sees only columns). */
val JournalEntity.goods: List<String> get() = listOf(good1, good2, good3).filter { it.isNotBlank() }

val JournalEntity.isBlankPage: Boolean get() = mood == 0 && goods.isEmpty() && note.isBlank()

@Entity(tableName = "assessments", indices = [Index("kind")])
data class AssessmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Assessments.Kind.code */
    val kind: String,
    val takenAt: Long,
    val score: Int,
    /** Chosen option per question (Assessments.encodeAnswers). */
    val answers: String
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    /** The "if… then…" plan that makes the goal concrete. */
    val plan: String = "",
    val pillarId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val doneAt: Long? = null
)

@Dao
interface GrowthDao {
    // ---- Habits ----
    @Query("SELECT * FROM habits ORDER BY position, id")
    fun habits(): Flow<List<HabitEntity>>

    @Query("SELECT COUNT(*) FROM habits")
    suspend fun habitCount(): Int

    @Query("SELECT COALESCE(MAX(position), -1) FROM habits")
    suspend fun lastPosition(): Int

    @Insert
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabit(id: Long)

    /** Check-ins of the last days (for streaks and the week strip). */
    @Query("SELECT * FROM habit_logs WHERE epochDay >= :fromDay")
    fun logsSince(fromDay: Long): Flow<List<HabitLogEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLog(log: HabitLogEntity)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND epochDay = :epochDay")
    suspend fun deleteLog(habitId: Long, epochDay: Long)

    // ---- Daily challenge ----
    @Query("SELECT epochDay FROM challenge_days")
    fun challengeDays(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallengeDays(days: List<ChallengeDayEntity>)

    @Query("DELETE FROM challenge_days WHERE epochDay = :epochDay")
    suspend fun deleteChallengeDay(epochDay: Long)

    // ---- Journal ----
    @Query("SELECT * FROM journal ORDER BY epochDay DESC")
    fun journal(): Flow<List<JournalEntity>>

    @Query("SELECT * FROM journal WHERE epochDay = :epochDay")
    suspend fun journalFor(epochDay: Long): JournalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertJournal(entry: JournalEntity)

    @Query("DELETE FROM journal WHERE epochDay = :epochDay")
    suspend fun deleteJournal(epochDay: Long)

    // ---- Self-checks ----
    @Query("SELECT * FROM assessments ORDER BY takenAt DESC")
    fun assessments(): Flow<List<AssessmentEntity>>

    @Insert
    suspend fun insertAssessment(a: AssessmentEntity): Long

    @Query("DELETE FROM assessments WHERE id = :id")
    suspend fun deleteAssessment(id: Long)

    // ---- Goals ----
    @Query("SELECT * FROM goals ORDER BY doneAt IS NOT NULL, createdAt DESC")
    fun goals(): Flow<List<GoalEntity>>

    @Insert
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoal(id: Long)
}

@Database(
    entities = [
        HabitEntity::class,
        HabitLogEntity::class,
        ChallengeDayEntity::class,
        JournalEntity::class,
        AssessmentEntity::class,
        GoalEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class GentleDatabase : RoomDatabase() {
    abstract fun growth(): GrowthDao

    companion object {
        const val NAME = "gentleman.db"

        @Volatile
        private var instance: GentleDatabase? = null

        fun get(context: Context): GentleDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context.applicationContext, GentleDatabase::class.java, NAME)
                    .build()
                    .also { instance = it }
            }

        /** Tests only: closes the database so the next [get] opens a fresh one. */
        internal fun resetForTests() = synchronized(this) {
            instance?.close()
            instance = null
        }
    }
}
