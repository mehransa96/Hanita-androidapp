package com.example.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.time.LocalDate
import java.time.ZoneId

object PeriodReminderHelper {
    private const val REQUEST_CODE = 4004

    fun scheduleReminder(context: Context, predictedPeriodStart: LocalDate, daysInAdvance: Int) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            
            // Calculate when to show the reminder. Under daysInAdvance:
            val reminderDate = predictedPeriodStart.minusDays(daysInAdvance.toLong())
            
            // Check if reminderDate is in the future
            val today = LocalDate.now()
            if (reminderDate.isBefore(today) || reminderDate.isEqual(today)) {
                Log.d("PeriodReminderHelper", "Reminder date is in the past, skipping scheduling.")
                return
            }

            val intent = Intent(context, PeriodReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Calculate alarm time in millis (e.g. at 9:00 AM on the target date)
            val localDateTime = reminderDate.atTime(9, 0) // 9:00 AM
            val zoneId = ZoneId.systemDefault()
            val epochMillis = localDateTime.atZone(zoneId).toInstant().toEpochMilli()

            // Cancel any old reminders
            alarmManager.cancel(pendingIntent)

            // Set the alarm (using non-exact set to be safe across modern Android permissions)
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, epochMillis, pendingIntent)
            Log.d("PeriodReminderHelper", "Scheduled reminder for $reminderDate at 9:00 AM")
        } catch (e: Exception) {
            Log.e("PeriodReminderHelper", "Failed to schedule alarm: ${e.message}")
        }
    }

    fun cancelReminder(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, PeriodReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            Log.d("PeriodReminderHelper", "Cancelled any scheduled reminder.")
        } catch (e: Exception) {
            Log.e("PeriodReminderHelper", "Failed to cancel alarm: ${e.message}")
        }
    }
}
