package com.hanita.partner

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.hanita.core.gentleman.Assessments
import com.hanita.core.gentleman.Habits
import com.hanita.core.gentleman.Status
import com.hanita.partner.data.GrowthRepository
import com.hanita.partner.data.JournalEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

/** The database layer of GentleMan on a real (Robolectric) SQLite: data must never get lost. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GrowthRepositoryTest {

    private lateinit var app: Application
    private lateinit var repo: GrowthRepository
    private val today = LocalDate.of(2026, 11, 15)

    @Before fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        GrowthRepository.resetForTests()
        repo = GrowthRepository.get(app)
    }

    @After fun tearDown() = GrowthRepository.resetForTests()

    @Test fun legacyChallengeDaysMoveIntoTheDatabaseOnce() = runBlocking {
        val days = setOf(today.toEpochDay() - 2, today.toEpochDay() - 1)
        app.getSharedPreferences(PartnerStore.NAME, Context.MODE_PRIVATE).edit()
            .putStringSet("done_days", days.map { it.toString() }.toSet()).commit()

        repo.prepare(Status.SINGLE)

        assertEquals(days, repo.challengeDays.first())
        assertTrue(PartnerStore(app).legacyDoneDays.isEmpty())
    }

    @Test fun starterHabitsAreAddedOnlyOnceEvenAfterTheUserDeletesThem() = runBlocking {
        repo.prepare(Status.MARRIED)
        val first = repo.habits.first()
        assertEquals(Habits.starterIds(Status.MARRIED), first.map { it.presetId })

        first.forEach { repo.deleteHabit(it.id) }
        repo.prepare(Status.MARRIED)
        assertTrue(repo.habits.first().isEmpty())
    }

    @Test fun noStarterHabitsBeforeTheUserChoosesAStatus() = runBlocking {
        repo.prepare(null)
        assertTrue(repo.habits.first().isEmpty())
        assertFalse(PartnerStore(app).starterHabitsAdded)
    }

    @Test fun habitsAreCappedAndBlankTitlesRejected() = runBlocking {
        assertFalse(repo.addHabit("   "))
        repeat(Habits.MAX_HABITS) { assertTrue(repo.addHabit("عادت $it")) }
        assertFalse(repo.addHabit("یکی بیشتر"))
        assertEquals(Habits.MAX_HABITS, repo.habits.first().size)
    }

    @Test fun checkInsFollowTheHabitAndDisappearWithIt() = runBlocking {
        repo.addHabit("پیاده‌روی")
        val habit = repo.habits.first().single()
        repo.setHabitDone(habit.id, today, true)
        repo.setHabitDone(habit.id, today, true) // a double tap must not fail
        assertEquals(setOf(today.toEpochDay()), repo.habitDays(today).first()[habit.id])

        repo.setHabitDone(habit.id, today, false)
        assertNull(repo.habitDays(today).first()[habit.id])

        repo.setHabitDone(habit.id, today, true)
        repo.deleteHabit(habit.id)
        assertTrue(repo.habitDays(today).first().isEmpty())
    }

    @Test fun aClearedJournalPageIsRemovedAndMoodKeepsTheText() = runBlocking {
        val day = today.toEpochDay()
        repo.saveJournal(JournalEntity(day, good1 = "  قدم زدیم  "))
        assertEquals("قدم زدیم", repo.journalFor(day)?.good1)

        repo.setMood(today, 4, repo.journalFor(day))
        val page = repo.journalFor(day)
        assertEquals(4, page?.mood)
        assertEquals("قدم زدیم", page?.good1)

        repo.saveJournal(JournalEntity(day))
        assertNull(repo.journalFor(day))
    }

    @Test fun backgroundJournalSaveFinishesAfterTheScreenIsGone() = runBlocking {
        val day = today.toEpochDay()
        repo.saveJournalInBackground(JournalEntity(day, note = "یادداشت شب"))
        var saved: JournalEntity? = null
        repeat(50) {
            saved = repo.journalFor(day)
            if (saved != null) return@repeat
            Thread.sleep(20)
        }
        assertEquals("یادداشت شب", saved?.note)
    }

    @Test fun assessmentScoresAreStoredAndInvalidAnswersRejected() = runBlocking {
        val answers = List(10) { 0 }
        val score = repo.saveAssessment(Assessments.Kind.SELF_ESTEEM, answers)
        assertEquals(Assessments.score(Assessments.Kind.SELF_ESTEEM, answers), score)
        assertNull(repo.saveAssessment(Assessments.Kind.SELF_ESTEEM, listOf(0, 1)))
        assertEquals(1, repo.assessments.first().size)
    }

    @Test fun goalsCanBeFinishedAndReopened() = runBlocking {
        assertTrue(repo.addGoal("دویدن ۵ کیلومتر", "اگر شنبه شد، آنگاه می‌دوم", null))
        val goal = repo.goals.first().single()
        repo.setGoalDone(goal, true)
        assertTrue(repo.goals.first().single().doneAt != null)
        repo.setGoalDone(repo.goals.first().single(), false)
        assertNull(repo.goals.first().single().doneAt)
    }

    @Test fun deleteEverythingEmptiesEveryTable() = runBlocking {
        repo.addHabit("کتاب")
        repo.addGoal("هدف", "", null)
        repo.saveJournal(JournalEntity(today.toEpochDay(), mood = 3))
        repo.setChallengeDone(today, true, null)
        repo.clearAll()
        assertTrue(repo.habits.first().isEmpty())
        assertTrue(repo.goals.first().isEmpty())
        assertTrue(repo.journal.first().isEmpty())
        assertTrue(repo.challengeDays.first().isEmpty())
    }
}
