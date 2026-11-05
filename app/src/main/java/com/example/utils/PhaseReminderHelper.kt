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
        cycleLength: Int,
        hour: Int = 9,
        minute: Int = 0
    ) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            
            // Check if targetDate is in the future, if not, project to the next cycle
            val today = LocalDate.now()
            var finalTargetDate = targetDate
            val step = com.example.domain.CyclePredictor.clampCycleLength(cycleLength).toLong()
            while (!finalTargetDate.isAfter(today)) {
                finalTargetDate = finalTargetDate.plusDays(step)
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

            val localDateTime = finalTargetDate.atTime(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
            val zoneId = ZoneId.systemDefault()
            val epochMillis = localDateTime.atZone(zoneId).toInstant().toEpochMilli()

            // Set the alarm
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, epochMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, epochMillis, pendingIntent)
            }
            Log.d("PhaseReminderHelper", "Scheduled phase reminder $phaseIndex on $finalTargetDate")
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
