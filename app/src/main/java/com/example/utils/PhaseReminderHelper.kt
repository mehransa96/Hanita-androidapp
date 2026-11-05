package com.example.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.time.LocalDate
import java.time.ZoneId

object PhaseReminderHelper {
    private const val BASE_REQUEST_CODE = 5000

    fun schedulePhaseReminder(
        context: Context,
        targetDate: LocalDate,
        phaseName: String,
        message: String,
        phaseIndex: Int,
        cycleLength: Int
    ) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            
            // Check if targetDate is in the future, if not, project to the next cycle
            val today = LocalDate.now()
            var finalTargetDate = targetDate
            while (finalTargetDate.isBefore(today) || finalTargetDate.isEqual(today)) {
                finalTargetDate = finalTargetDate.plusDays(cycleLength.toLong())
            }

            val intent = Intent(context, PhaseReminderReceiver::class.java).apply {
                putExtra("PHASE_NAME", phaseName)
                putExtra("PHASE_MESSAGE", message)
            }
            
            val requestCode = BASE_REQUEST_CODE + phaseIndex
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Calculate alarm time in millis (e.g. at 9:00 AM on the target date)
            val localDateTime = finalTargetDate.atTime(9, 0)
            val zoneId = ZoneId.systemDefault()
            val epochMillis = localDateTime.atZone(zoneId).toInstant().toEpochMilli()

            // Set the alarm
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, epochMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, epochMillis, pendingIntent)
            }
            Log.d("PhaseReminderHelper", "Scheduled reminder for $phaseName on $finalTargetDate at 9:00 AM")
        } catch (e: Exception) {
            Log.e("PhaseReminderHelper", "Failed to schedule phase alarm: ${e.message}")
        }
    }

    fun cancelAllPhaseReminders(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            for (i in 0..4) {
                val intent = Intent(context, PhaseReminderReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    BASE_REQUEST_CODE + i,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
            }
            Log.d("PhaseReminderHelper", "Cancelled phase reminders.")
        } catch (e: Exception) {
            Log.e("PhaseReminderHelper", "Failed to cancel phase alarms: ${e.message}")
        }
    }
}
