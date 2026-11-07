package com.example.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.model.DailyLog
import com.example.data.preferences.UserPreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate

class MedicineReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_REMIND = "com.example.hanita.ACTION_MEDICINE_REMIND"
        const val ACTION_TAKEN = "com.example.hanita.ACTION_MEDICINE_TAKEN"
        const val EXTRA_MED_ID = "MED_ID"
        const val CHANNEL_ID = "medicine_channel"

        /** Shows the reminder notification now (used by the reminders screen as a preview, too). */
        fun show(context: Context, reminderId: String, name: String, dosage: String) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                nm.createNotificationChannel(
                    NotificationChannel(CHANNEL_ID, "یادآور داروها و مکمل‌ها", NotificationManager.IMPORTANCE_HIGH).apply {
                        description = "یادآور روزانه زمان مصرف داروها"
                    }
                )
            }
            val notificationId = reminderId.hashCode()
            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(MainActivity.EXTRA_NAVIGATE_TO, "reminders")
            }
            val takenIntent = Intent(context, MedicineReminderReceiver::class.java).apply {
                action = ACTION_TAKEN
                putExtra(EXTRA_MED_ID, reminderId)
            }
            val dose = dosage.takeIf { it.isNotBlank() }?.let { " ($it)" }.orEmpty()
            val notification = NotificationCompat.Builder(context, CHANNEL_ID).privateOnLockScreen(context, CHANNEL_ID)
                .setSmallIcon(com.example.R.drawable.ic_stat_hanita)
                .setContentTitle("یادآور دارو 💊")
                .setContentText("زمان مصرف $name$dose فرا رسیده است.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(
                    PendingIntent.getActivity(context, notificationId, openIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                )
                .addAction(
                    android.R.drawable.checkbox_on_background,
                    "مصرف کردم",
                    PendingIntent.getBroadcast(context, notificationId, takenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                )
                .build()
            nm.notify(notificationId, notification)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val medId = intent.getStringExtra(EXTRA_MED_ID) ?: return
        val prefs = UserPreferencesManager.get(context)
        when (intent.action) {
            ACTION_TAKEN -> markTaken(context, medId)
            else -> {
                // The reminder may have been disabled, edited or deleted after the alarm was set.
                val reminder = prefs.medicationReminders.value.firstOrNull { it.id == medId }
                if (reminder != null && prefs.isMedicationReminderActive(medId)) {
                    show(context, reminder.id, reminder.name, reminder.dosage)
                }
                // Re-arm for tomorrow.
                RescheduleWorker.runNow(context)
            }
        }
    }

    /** "Taken" action: records the dose in today's log, exactly like the checkbox in the calendar. */
    private fun markTaken(context: Context, medId: String) {
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.cancel(medId.hashCode())
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.getDatabase(context).dailyLogDao()
                val today = LocalDate.now().toString()
                val existing = dao.getLogByDate(today)
                val meds = existing?.takenMeds?.split(",")?.filter { it.isNotBlank() }?.toMutableSet() ?: mutableSetOf()
                meds.add(medId)
                val updated = (existing ?: DailyLog(today, "NONE", "", "", "")).copy(
                    takenMeds = meds.joinToString(","),
                    updatedAt = System.currentTimeMillis()
                )
                dao.insertLog(updated)
            } finally {
                pending.finish()
            }
        }
    }
}
