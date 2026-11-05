package com.example.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.MainActivity

class CycleNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_PERIOD_REMINDER = "com.example.hanita.ACTION_PERIOD_REMINDER"
        const val ACTION_OVULATION_REMINDER = "com.example.hanita.ACTION_OVULATION_REMINDER"

        const val CHANNEL_PERIOD_ID = "channel_period_reminders_v2"
        const val CHANNEL_OVULATION_ID = "channel_ovulation_reminders_v2"

        const val NOTIFICATION_ID_PERIOD = 3001
        const val NOTIFICATION_ID_OVULATION = 3002

        const val EXTRA_TITLE = "extra_notification_title"
        const val EXTRA_BODY = "extra_notification_body"
        const val EXTRA_DATE_JALALI = "extra_date_jalali"
        const val EXTRA_DAYS_LEFT = "extra_days_left"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d("CycleNotificationReceiver", "Received cycle notification broadcast: $action")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        createNotificationChannels(context, notificationManager)

        val titleExtra = intent.getStringExtra(EXTRA_TITLE)
        val bodyExtra = intent.getStringExtra(EXTRA_BODY)
        val dateJalali = intent.getStringExtra(EXTRA_DATE_JALALI) ?: ""
        val daysLeft = intent.getIntExtra(EXTRA_DAYS_LEFT, -1)

        when (action) {
            ACTION_PERIOD_REMINDER -> {
                sendPeriodNotification(context, notificationManager, titleExtra, bodyExtra, dateJalali, daysLeft)
            }
            ACTION_OVULATION_REMINDER -> {
                sendOvulationNotification(context, notificationManager, titleExtra, bodyExtra, dateJalali, daysLeft)
            }
        }
        
        // After sending the notification, enqueue a WorkManager job to recalculate and schedule the next alarm
        RescheduleWorker.runNow(context)
    }

    private fun createNotificationChannels(context: Context, notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            // Period Channel
            val periodChannel = NotificationChannel(
                CHANNEL_PERIOD_ID,
                "یادآورهای شروع قاعدگی و پریود",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "هشدارهای هوشمند و یادآورهای قبل از شروع دوره قاعدگی"
                enableLights(true)
                lightColor = Color.parseColor("#E91E63") // Warm pink
                enableVibration(true)
                setSound(defaultSoundUri, audioAttributes)
            }

            // Ovulation Channel
            val ovulationChannel = NotificationChannel(
                CHANNEL_OVULATION_ID,
                "یادآورهای تخمک‌گذاری و باروری",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "هشدارهای پنجره باروری و زمان دقیق تخمک‌گذاری"
                enableLights(true)
                lightColor = Color.parseColor("#9C27B0") // Purple
                enableVibration(true)
                setSound(defaultSoundUri, audioAttributes)
            }

            notificationManager.createNotificationChannel(periodChannel)
            notificationManager.createNotificationChannel(ovulationChannel)
        }
    }

    private fun sendPeriodNotification(
        context: Context,
        notificationManager: NotificationManager,
        customTitle: String?,
        customBody: String?,
        dateJalali: String,
        daysLeft: Int
    ) {
        val title = customTitle ?: "یادآور هوشمند شروع قاعدگی 🌸"
        val body = customBody ?: run {
            when {
                daysLeft == 0 -> "امروز روز پیش‌بینی شده برای شروع دوره شماست. مایعات گرم بنوشید و مراقبت‌های لازم را انجام دهید."
                daysLeft == 1 -> "فردا دوره احتمالی شما ($dateJalali) آغاز خواهد شد. لوازم بهداشتی را آماده داشته باشید."
                daysLeft > 1 -> "حدود $daysLeft روز تا آغاز دوره احتمالی شما ($dateJalali) باقی مانده است. به علائم PMS توجه کنید."
                dateJalali.isNotBlank() -> "دوره احتمالی بعدی شما برای $dateJalali پیش‌بینی شده است. برای ثبت وضعیت به اپلیکیشن سر بزنید."
                else -> "بر اساس چرخه شما، دوره جدید به زودی آغاز خواهد شد. مراقبت‌های لازم و استراحت کافی را در نظر بگیرید."
            }
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "calendar")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            101,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val logIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "log")
        }
        val logPendingIntent = PendingIntent.getActivity(
            context,
            102,
            logIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_PERIOD_ID).privateOnLockScreen(context, CHANNEL_PERIOD_ID)
            .setSmallIcon(com.example.R.drawable.ic_stat_hanita)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setColor(android.graphics.Color.parseColor("#E91E63"))
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_edit, "ثبت علائم امروز", logPendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID_PERIOD, notification)
    }

    private fun sendOvulationNotification(
        context: Context,
        notificationManager: NotificationManager,
        customTitle: String?,
        customBody: String?,
        dateJalali: String,
        daysLeft: Int
    ) {
        val title = customTitle ?: "یادآور هوشمند تخمک‌گذاری و پنجره باروری 🥚✨"
        val body = customBody ?: run {
            when {
                daysLeft == 0 -> "امروز روز طلایی تخمک‌گذاری است! شانس باروری در اوج قرار دارد و سطح انرژی بدن بالاست."
                daysLeft == 1 -> "فردا روز تخمک‌گذاری پیش‌بینی شده شما ($dateJalali) است. هم‌اکنون در پنجره باروری مطلوب هستید."
                daysLeft > 1 -> "حدود $daysLeft روز تا روز اوج تخمک‌گذاری ($dateJalali) باقی مانده است. وارد فاز باروری شدید."
                dateJalali.isNotBlank() -> "زمان تخمک‌گذاری و باروری شما حدود $dateJalali پیش‌بینی شده است."
                else -> "هم‌اکنون در پنجره طلایی تخمک‌گذاری و بیشترین احتمال باروری قرار دارید. برای ثبت دمای پایه و علائم وارد شوید."
            }
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "calendar")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            201,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val logIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "log")
        }
        val logPendingIntent = PendingIntent.getActivity(
            context,
            202,
            logIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_OVULATION_ID).privateOnLockScreen(context, CHANNEL_OVULATION_ID)
            .setSmallIcon(com.example.R.drawable.ic_stat_hanita)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setColor(android.graphics.Color.parseColor("#9C27B0"))
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_today, "مشاهده چرخه در تقویم", pendingIntent)
            .addAction(android.R.drawable.ic_menu_edit, "ثبت علائم", logPendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID_OVULATION, notification)
    }
}
