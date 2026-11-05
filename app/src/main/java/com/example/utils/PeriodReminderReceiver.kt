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

class PeriodReminderReceiver : BroadcastReceiver() {
    companion object {
        const val CHANNEL_ID = "period_reminders_channel"
        const val NOTIFICATION_ID = 1001
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("PeriodReminderReceiver", "Alarm received!")
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create Notification Channel for Android O (API 26) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "یادآوری شروع دوره احتمالی",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "اعلان‌های یادآوری تاریخ شروع احتمالی پریود"
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

        // Custom notification title and text with Gemini AI personalized prediction support
        var title = "یادآوری سلامت و چرخه قاعدگی 🌸"
        var body = "بر اساس محاسبات ما، دوره قاعدگی احتمالی شما نزدیک است. برای ثبت علائم و مراقبت‌های ویژه این دوره آماده شوید."

        try {
            val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val isAiEnabled = prefs.getBoolean("is_ai_reminders_enabled", true)
            val cachedReport = prefs.getString("cycle_analysis_report", "") ?: ""

            if (isAiEnabled && cachedReport.isNotBlank()) {
                val moshi = com.squareup.moshi.Moshi.Builder()
                    .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                    .build()
                val adapter = moshi.adapter(com.example.ui.viewmodel.CycleAnalysisResult::class.java)
                val parsed = adapter.fromJson(cachedReport)
                if (parsed != null && parsed.predictedNextStartJalali.isNotBlank()) {
                    title = "تحلیل خودمراقبتی هوشمند هانیتا 🧠"
                    val tip = if (parsed.nutritionRecommend.isNotBlank()) {
                        "توصیه تغذیه: " + parsed.nutritionRecommend.take(80) + "..."
                    } else "مراقبت‌های لازم را در نظر بگیرید."
                    body = "دوره بعدی حدود ${parsed.predictedNextStartJalali} پیش‌بینی شده است. $tip"
                }
            }
        } catch (e: Exception) {
            Log.e("PeriodReminderReceiver", "Error parsing cached AI report: ${e.message}")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID).privateOnLockScreen(context, CHANNEL_ID)
            .setSmallIcon(com.example.R.drawable.ic_stat_hanita)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
