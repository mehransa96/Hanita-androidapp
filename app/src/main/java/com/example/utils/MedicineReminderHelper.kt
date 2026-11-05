package com.example.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.model.MedicineReminder
import java.util.Calendar

object MedicineReminderHelper {

    fun scheduleReminders(context: Context, reminders: List<MedicineReminder>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // We should really cancel all old alarms first if we had their IDs, 
        // but for now, we assume this sets/updates alarms based on ID hashcode.
        
        for (reminder in reminders) {
            val intent = Intent(context, MedicineReminderReceiver::class.java).apply {
                putExtra("MED_NAME", reminder.name)
                putExtra("MED_DOSAGE", reminder.dosage)
                putExtra("MED_ID", reminder.id)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (!reminder.isEnabled) {
                alarmManager.cancel(pendingIntent)
                continue
            }

            try {
                val parts = reminder.time.split(":")
                if (parts.size == 2) {
                    val hour = parts[0].toIntOrNull() ?: 8
                    val minute = parts[1].toIntOrNull() ?: 0

                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = System.currentTimeMillis()
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    // If time is already past for today, schedule for tomorrow
                    if (calendar.timeInMillis <= System.currentTimeMillis()) {
                        calendar.add(Calendar.DAY_OF_YEAR, 1)
                    }

                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                    )
                    Log.d("MedicineReminderHelper", "Scheduled ${reminder.name} at ${reminder.time}")
                }
            } catch (e: Exception) {
                Log.e("MedicineReminderHelper", "Error parsing time or scheduling: ${e.message}")
            }
        }
    }
}
