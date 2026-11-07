package com.example.utils

import androidx.core.content.edit
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.model.MedicineReminder
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Daily medication reminders.
 *
 * Each reminder is a one-shot alarm for its next occurrence, re-armed by the receiver when it
 * fires. Repeating alarms are inexact on modern Android and can arrive hours late, which is not
 * acceptable for a medicine schedule.
 */
object MedicineReminderHelper {
    private const val TAG = "MedicineReminderHelper"
    private const val STORE = "hanita_medicine_alarms"
    private const val KEY_IDS = "scheduled_ids"
    private const val KEY_LEGACY_CLEARED = "legacy_repeating_cleared"

    /** "HH:mm" (any digits) to a time, or null when malformed. */
    fun parseTime(text: String): LocalTime? {
        val parts = PersianNumbers.normalize(text).split(":")
        if (parts.size != 2) return null
        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null
        if (hour !in 0..23 || minute !in 0..59) return null
        return LocalTime.of(hour, minute)
    }

    fun formatTime(time: LocalTime): String = "%02d:%02d".format(java.util.Locale.US, time.hour, time.minute)

    fun nextOccurrence(time: LocalTime, now: LocalDateTime = LocalDateTime.now()): LocalDateTime {
        val today = now.toLocalDate().atTime(time)
        return if (today.isAfter(now)) today else today.plusDays(1)
    }

    fun scheduleReminders(context: Context, reminders: List<MedicineReminder>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // Cancel alarms of reminders that were deleted since the last run, otherwise a removed
        // medicine would keep notifying.
        val store = context.getSharedPreferences(STORE, Context.MODE_PRIVATE)
        val currentIds = reminders.map { it.id }.toSet()
        val previousIds = store.getStringSet(KEY_IDS, emptySet()).orEmpty()
        for (removedId in previousIds - currentIds) {
            alarmManager.cancel(pendingIntent(context, removedId))
        }
        store.edit { putStringSet(KEY_IDS, currentIds) }
        // Version 1.1 used daily repeating alarms without an action; remove them once.
        if (!store.getBoolean(KEY_LEGACY_CLEARED, false)) {
            for (id in previousIds + currentIds) {
                PendingIntent.getBroadcast(
                    context, id.hashCode(), Intent(context, MedicineReminderReceiver::class.java),
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )?.let { alarmManager.cancel(it); it.cancel() }
            }
            store.edit { putBoolean(KEY_LEGACY_CLEARED, true) }
        }

        for (reminder in reminders) {
            val pendingIntent = pendingIntent(context, reminder.id)
            alarmManager.cancel(pendingIntent)
            if (!reminder.isEnabled) continue
            val time = parseTime(reminder.time)
            if (time == null) {
                Log.w(TAG, "Skipping reminder with an invalid time")
                continue
            }
            try {
                val fireAt = nextOccurrence(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAt, pendingIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Error scheduling medication reminder: ${e.message}")
            }
        }
    }

    /** The alarm intent only carries the id: name and dose are read fresh when it fires. */
    private fun pendingIntent(context: Context, reminderId: String): PendingIntent {
        val intent = Intent(context, MedicineReminderReceiver::class.java).apply {
            action = MedicineReminderReceiver.ACTION_REMIND
            putExtra(MedicineReminderReceiver.EXTRA_MED_ID, reminderId)
        }
        return PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
