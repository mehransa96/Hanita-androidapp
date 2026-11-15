package com.hanita.partner.data

import android.content.Context
import androidx.room.withTransaction
import com.hanita.core.gentleman.Assessments
import com.hanita.core.gentleman.Habits
import com.hanita.core.gentleman.Status
import com.hanita.partner.PartnerStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** The one door to the database for the screens. */
class GrowthRepository private constructor(private val db: GentleDatabase, private val store: PartnerStore) {

    private val dao = db.growth()

    /** Days to look back for streaks and the week strip (a year is plenty for the best streak). */
    private val logWindow = 400L

    val habits: Flow<List<HabitEntity>> = dao.habits()

    /** For every habit id, the epoch days it was done in the last [logWindow] days. */
    fun habitDays(today: LocalDate): Flow<Map<Long, Set<Long>>> =
        dao.logsSince(today.toEpochDay() - logWindow).map { logs ->
            logs.groupBy({ it.habitId }, { it.epochDay }).mapValues { it.value.toSet() }
        }

    val challengeDays: Flow<Set<Long>> = dao.challengeDays().map { it.toSet() }
    val journal: Flow<List<JournalEntity>> = dao.journal()
    val assessments: Flow<List<AssessmentEntity>> = dao.assessments()
    val goals: Flow<List<GoalEntity>> = dao.goals()

    // ---- Habits ----

    suspend fun addHabit(title: String, presetId: String? = null, pillarId: String? = null): Boolean {
        val clean = title.trim().take(Habits.MAX_TITLE)
        if (clean.isEmpty()) return false
        if (dao.habitCount() >= Habits.MAX_HABITS) return false
        dao.insertHabit(HabitEntity(title = clean, presetId = presetId, pillarId = pillarId, position = dao.lastPosition() + 1))
        return true
    }

    suspend fun addPreset(preset: Habits.Preset): Boolean = addHabit(preset.title, preset.id, preset.pillarId)

    suspend fun deleteHabit(id: Long) = dao.deleteHabit(id)

    suspend fun setHabitDone(habitId: Long, day: LocalDate, done: Boolean) {
        if (done) dao.insertLog(HabitLogEntity(habitId, day.toEpochDay()))
        else dao.deleteLog(habitId, day.toEpochDay())
    }

    // ---- Daily challenge ----

    suspend fun setChallengeDone(day: LocalDate, done: Boolean, pillarId: String?) {
        if (done) dao.insertChallengeDays(listOf(ChallengeDayEntity(day.toEpochDay(), pillarId)))
        else dao.deleteChallengeDay(day.toEpochDay())
    }

    // ---- Journal ----

    suspend fun saveJournal(entry: JournalEntity) {
        val clean = entry.copy(
            good1 = entry.good1.trim().take(MAX_LINE),
            good2 = entry.good2.trim().take(MAX_LINE),
            good3 = entry.good3.trim().take(MAX_LINE),
            note = entry.note.trim().take(MAX_NOTE),
            mood = entry.mood.coerceIn(0, 5),
            updatedAt = System.currentTimeMillis()
        )
        if (clean.isBlankPage) dao.deleteJournal(clean.epochDay) else dao.upsertJournal(clean)
    }

    suspend fun setMood(day: LocalDate, mood: Int, current: JournalEntity?) =
        saveJournal((current ?: JournalEntity(day.toEpochDay())).copy(mood = mood))

    suspend fun deleteJournal(epochDay: Long) = dao.deleteJournal(epochDay)

    suspend fun journalFor(epochDay: Long): JournalEntity? = dao.journalFor(epochDay)

    // ---- Self-checks ----

    suspend fun saveAssessment(kind: Assessments.Kind, answers: List<Int>): Int? {
        val score = Assessments.score(kind, answers) ?: return null
        dao.insertAssessment(
            AssessmentEntity(kind = kind.code, takenAt = System.currentTimeMillis(), score = score, answers = Assessments.encodeAnswers(answers))
        )
        return score
    }

    suspend fun deleteAssessment(id: Long) = dao.deleteAssessment(id)

    // ---- Goals ----

    suspend fun addGoal(title: String, plan: String, pillarId: String?): Boolean {
        val t = title.trim().take(MAX_LINE)
        if (t.isEmpty()) return false
        dao.insertGoal(GoalEntity(title = t, plan = plan.trim().take(MAX_LINE), pillarId = pillarId))
        return true
    }

    suspend fun setGoalDone(goal: GoalEntity, done: Boolean) =
        dao.updateGoal(goal.copy(doneAt = if (done) System.currentTimeMillis() else null))

    suspend fun deleteGoal(id: Long) = dao.deleteGoal(id)

    // ---- Setup and reset ----

    /**
     * One-time work when the app opens: moves the challenge days that version 2 kept in
     * SharedPreferences into the database, and gives a new user a few starter habits.
     */
    suspend fun prepare(status: Status?) = withContext(Dispatchers.IO) {
        val legacy = store.legacyDoneDays
        db.withTransaction {
            if (legacy.isNotEmpty()) dao.insertChallengeDays(legacy.map { ChallengeDayEntity(it) })
            if (status != null && !store.starterHabitsAdded) {
                if (dao.habitCount() == 0) {
                    Habits.starterIds(status).mapNotNull { Habits.preset(it) }.forEachIndexed { i, p ->
                        dao.insertHabit(HabitEntity(title = p.title, presetId = p.id, pillarId = p.pillarId, position = i))
                    }
                }
                store.starterHabitsAdded = true
            }
        }
        // Only forget the old copy once it is safely in the database.
        if (legacy.isNotEmpty()) store.clearLegacyDoneDays()
    }

    /** Deletes every row (used by "delete everything" in settings). */
    suspend fun clearAll() = withContext(Dispatchers.IO) { db.clearAllTables() }

    companion object {
        const val MAX_LINE = 200
        const val MAX_NOTE = 2000

        @Volatile
        private var instance: GrowthRepository? = null

        fun get(context: Context): GrowthRepository =
            instance ?: synchronized(this) {
                instance ?: GrowthRepository(GentleDatabase.get(context), PartnerStore(context)).also { instance = it }
            }
    }
}
