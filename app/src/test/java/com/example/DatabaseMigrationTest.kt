package com.example

import android.app.Application
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Upgrades real version-9 database files to the current schema. Two different "version 9"
 * files exist: the released 1.2.0 (health_entries table) and development builds of 1.1
 * (pain/sleep/steps columns). Both must open without losing a single record.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DatabaseMigrationTest {

    private val app = ApplicationProvider.getApplicationContext<Application>()

    @After
    fun tearDown() {
        AppDatabase.closeAndForget()
        app.deleteDatabase("hanita_database")
    }

    private fun createV9(extraDailyColumns: Boolean, withHealthEntries: Boolean, fill: (SQLiteDatabase) -> Unit) {
        AppDatabase.closeAndForget()
        app.deleteDatabase("hanita_database")
        val file = app.getDatabasePath("hanita_database")
        file.parentFile?.mkdirs()
        val db = SQLiteDatabase.openOrCreateDatabase(file, null)
        val extra = if (extraDailyColumns) ", `painLevel` INTEGER NOT NULL DEFAULT 0, `sleepHours` REAL, `steps` INTEGER" else ""
        db.execSQL("CREATE TABLE `daily_logs` (`dateStr` TEXT NOT NULL, `periodFlow` TEXT NOT NULL, `symptoms` TEXT NOT NULL, `moods` TEXT NOT NULL, `notes` TEXT NOT NULL, `emoji` TEXT NOT NULL, `takenMeds` TEXT NOT NULL, `waterIntake` INTEGER NOT NULL, `bbt` REAL, `weight` REAL, `updatedAt` INTEGER NOT NULL$extra, PRIMARY KEY(`dateStr`))")
        db.execSQL("CREATE TABLE `cycle_records` (`id` TEXT NOT NULL, `startDate` TEXT NOT NULL, `endDate` TEXT NOT NULL, `notes` TEXT NOT NULL, PRIMARY KEY(`id`))")
        db.execSQL("CREATE TABLE `physical_symptom_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `dateStr` TEXT NOT NULL, `symptomName` TEXT NOT NULL, `severity` TEXT NOT NULL, `severityScore` INTEGER NOT NULL, `category` TEXT NOT NULL, `notes` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
        if (withHealthEntries) {
            db.execSQL("CREATE TABLE `health_entries` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `dateStr` TEXT NOT NULL, `type` TEXT NOT NULL, `value` TEXT NOT NULL, `num1` INTEGER, `num2` INTEGER, `notes` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
        }
        fill(db)
        db.version = 9
        db.close()
    }

    @Test
    fun released120DatabaseKeepsEverythingAndMovesSleepAndSteps() = runBlocking {
        createV9(extraDailyColumns = false, withHealthEntries = true) { db ->
            db.execSQL("INSERT INTO daily_logs VALUES ('2026-10-01', 'HEAVY', 'سردرد', 'آرامش', 'یادداشت', '🙂', '', 3, 36.6, 61.5, 100)")
            db.execSQL("INSERT INTO cycle_records VALUES ('cycle_2026-10-01', '2026-10-01', '2026-10-05', 'نوت')")
            db.execSQL("INSERT INTO health_entries (dateStr, type, value, num1, num2, notes, timestamp) VALUES ('2026-10-01', 'SLEEP', '', 450, NULL, '', 1)")
            db.execSQL("INSERT INTO health_entries (dateStr, type, value, num1, num2, notes, timestamp) VALUES ('2026-10-02', 'STEPS', '', 6000, NULL, '', 2)")
            db.execSQL("INSERT INTO health_entries (dateStr, type, value, num1, num2, notes, timestamp) VALUES ('2026-10-02', 'BLOOD_PRESSURE', '', 118, 76, '', 3)")
        }

        val db = AppDatabase.getDatabase(app)
        val first = db.dailyLogDao().getLogByDate("2026-10-01")
        assertNotNull(first)
        assertEquals("HEAVY", first!!.periodFlow)
        assertEquals("سردرد", first.symptoms)
        assertEquals(61.5f, first.weight!!, 0.001f)
        assertEquals(7.5f, first.sleepHours!!, 0.001f)
        assertNull(first.steps)

        val second = db.dailyLogDao().getLogByDate("2026-10-02")
        assertNotNull("a day with only steps gets a log", second)
        assertEquals("NONE", second!!.periodFlow)
        assertEquals(6000, second.steps)

        assertEquals(1, db.cycleRecordDao().getAllCycleRecordsList().size)
        assertEquals("health entries stay in place", 3, db.healthEntryDao().getAllList().size)
    }

    @Test
    fun developmentDatabaseWithNewColumnsGetsTheHealthTable() = runBlocking {
        createV9(extraDailyColumns = true, withHealthEntries = false) { db ->
            db.execSQL("INSERT INTO daily_logs VALUES ('2026-09-10', 'NONE', '', '', '', '', '', 0, NULL, NULL, 0, 6, 8.0, 4200)")
        }

        val db = AppDatabase.getDatabase(app)
        val log = db.dailyLogDao().getLogByDate("2026-09-10")
        assertNotNull(log)
        assertEquals(6, log!!.painLevel)
        assertEquals(8.0f, log.sleepHours!!, 0.001f)
        assertEquals(4200, log.steps)
        assertEquals(0, db.healthEntryDao().getAllList().size)
    }
}
