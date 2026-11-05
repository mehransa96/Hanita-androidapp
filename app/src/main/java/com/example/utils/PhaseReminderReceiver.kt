package com.example.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class PhaseReminderReceiver : BroadcastReceiver() {
    companion object {
        const val CHANNEL_ID = "phase_reminders_channel"
        const val NOTIFICATION_ID = 2002
    }

    override fun onReceive(context: Context, intent: Intent) {
        val phaseName = intent.getStringExtra("PHASE_NAME") ?: "فاز جدید چرخه"
        val phaseMessage = intent.getStringExtra("PHASE_MESSAGE") ?: "به فاز جدیدی از چرخه خود وارد شده‌اید."
        
        Log.d("PhaseReminderReceiver", "Alarm received for $phaseName")
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "یادآوری تغییر فاز چرخه",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "اعلان‌های یادآوری فازهای مختلف چرخه قاعدگی"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID).privateOnLockScreen(context, CHANNEL_ID)
            .setSmallIcon(com.example.R.drawable.ic_stat_hanita)
            .setContentTitle("ورود به $phaseName 🌸")
            .setContentText(phaseMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(phaseMessage))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID + phaseName.hashCode(), notification)
        // Schedule the same phase for the next cycle.
        RescheduleWorker.runNow(context)
    }
}
