package com.example.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.app.AlarmManager
import android.app.PendingIntent
import android.util.Log
import com.example.data.AppDatabase
import com.example.data.preferences.UserPreferencesManager
import com.example.domain.CyclePredictor
import com.example.domain.ReminderPlanner
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * One place that (re)schedules every reminder from the real logged data.
 * Used by the ViewModel (when data or settings change) and by [RescheduleWorker]
 * (after boot, time changes, a fired reminder and once a day).
 */
object ReminderScheduler {
    private const val TAG = "ReminderScheduler"

    data class Settings(
        val onboarded: Boolean,
        val periodEnabled: Boolean,
        val ovulationEnabled: Boolean,
        val daysBeforePeriod: Int,
        val daysBeforeOvulation: Int,
        val hour: Int,
        val minute: Int
    ) {
        fun toPlannerSettings() = ReminderPlanner.Settings(
            periodEnabled = periodEnabled,
            ovulationEnabled = ovulationEnabled,
            daysBeforePeriod = daysBeforePeriod,
            daysBeforeOvulation = daysBeforeOvulation,
            time = LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
        )
    }

    fun settingsFrom(prefs: UserPreferencesManager) = Settings(
        onboarded = prefs.isLoggedIn.value,
        periodEnabled = prefs.isNotificationEnabled.value,
        ovulationEnabled = prefs.isOvulationRemindersEnabled.value,
        daysBeforePeriod = prefs.daysBefore.value,
        daysBeforeOvulation = prefs.ovulationDaysBefore.value,
        hour = prefs.notificationHour.value,
        minute = prefs.notificationMinute.value
    )

    fun schedule(context: Context, prediction: CyclePredictor.Prediction, settings: Settings) {
        cancelLegacyAlarms(context)
        if (!settings.onboarded || prediction.lastPeriodStart == null) {
            CycleNotificationHelper.cancelAll(context)
            PhaseReminderHelper.cancelAllPhaseReminders(context)
            return
        }
        val plan = ReminderPlanner.plan(prediction, settings.toPlannerSettings(), LocalDateTime.now())
        CycleNotificationHelper.apply(context, plan)
        PhaseReminderHelper.apply(context, plan.phases)
    }

    /** Reads everything from storage; safe to call from a background worker. */
    suspend fun rescheduleFromStorage(context: Context) {
        try {
            val prefs = UserPreferencesManager.get(context)
            val db = AppDatabase.getDatabase(context)
            val bleedingDays = db.dailyLogDao().getPeriodDaysList().mapNotNull {
                runCatching { LocalDate.parse(it.dateStr) }.getOrNull()
            }
            val prediction = CyclePredictor.predict(bleedingDays, prefs.averageCycleLength.value)
            schedule(context, prediction, settingsFrom(prefs))
            // Medication reminders belong to the onboarded user only.
            MedicineReminderHelper.scheduleReminders(
                context,
                if (prefs.isLoggedIn.value) prefs.medicationReminders.value else emptyList()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reschedule reminders", e)
        }
    }

    /** Alarms created by app versions <= 1.1 through a receiver that no longer exists. */
    private fun cancelLegacyAlarms(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val legacy = Intent().setComponent(ComponentName(context.packageName, "com.example.utils.PeriodReminderReceiver"))
            PendingIntent.getBroadcast(context, 4004, legacy, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
                ?.let { alarmManager.cancel(it); it.cancel() }
        } catch (_: Exception) {
        }
    }
}
