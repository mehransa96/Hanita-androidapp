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
import com.example.data.preferences.UserPreferencesManager
import com.example.domain.ReminderPlanner

class PhaseReminderReceiver : BroadcastReceiver() {
    companion object {
        const val CHANNEL_ID = "phase_reminders_channel"
        const val EXTRA_PHASE = "PHASE_KIND"
        private const val NOTIFICATION_ID = 2002
    }

    override fun onReceive(context: Context, intent: Intent) {
        val kind = runCatching { ReminderPlanner.PhaseKind.valueOf(intent.getStringExtra(EXTRA_PHASE).orEmpty()) }
            .getOrNull() ?: return
        // Settings may have changed after the alarm was set.
        val prefs = UserPreferencesManager.get(context)
        if (prefs.isLoggedIn.value && prefs.isNotificationEnabled.value) {
            show(context, kind)
        }
        // Schedule the same phase for the next cycle.
        RescheduleWorker.runNow(context)
    }

    private fun show(context: Context, kind: ReminderPlanner.PhaseKind) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "یادآوری تغییر فاز چرخه",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "اعلان شروع هر فاز چرخه قاعدگی" }
            notificationManager.createNotificationChannel(channel)
        }

        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(MainActivity.EXTRA_NAVIGATE_TO, "calendar")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            300 + kind.ordinal,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val message = PhaseReminderHelper.message(kind)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID).privateOnLockScreen(context, CHANNEL_ID)
            .setSmallIcon(com.example.R.drawable.ic_stat_hanita)
            .setContentTitle("ورود به ${PhaseReminderHelper.title(kind)} 🌸")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID + kind.ordinal, notification)
    }
}
