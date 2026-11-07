package com.example.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.graphics.toColorInt
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.preferences.UserPreferencesManager
import com.example.domain.ReminderPlanner
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Shows the period / ovulation / late-period notifications. The text is built when the alarm
 * fires (not when it was scheduled), so it always reflects today's date and the latest settings.
 */
class CycleNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_PERIOD_REMINDER = "com.example.hanita.ACTION_PERIOD_REMINDER"
        const val ACTION_OVULATION_REMINDER = "com.example.hanita.ACTION_OVULATION_REMINDER"
        const val ACTION_LATE_CHECK = "com.example.hanita.ACTION_LATE_CHECK"

        const val CHANNEL_PERIOD_ID = "channel_period_reminders_v2"
        const val CHANNEL_OVULATION_ID = "channel_ovulation_reminders_v2"

        private const val NOTIFICATION_ID_PERIOD = 3001
        private const val NOTIFICATION_ID_OVULATION = 3002
        private const val NOTIFICATION_ID_LATE = 3003

        const val EXTRA_EVENT_DATE = "extra_event_date"
        const val EXTRA_PREVIEW = "extra_preview"

        private val PINK = "#E91E63".toColorInt()
        private val PURPLE = "#9C27B0".toColorInt()
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val eventDate = intent.getStringExtra(EXTRA_EVENT_DATE)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        val isPreview = intent.getBooleanExtra(EXTRA_PREVIEW, false)
        val prefs = UserPreferencesManager.get(context)

        // A real alarm re-checks the current settings: the user may have turned it off meanwhile.
        val allowed = isPreview || (prefs.isLoggedIn.value && when (action) {
            ACTION_OVULATION_REMINDER -> prefs.isOvulationRemindersEnabled.value
            else -> prefs.isNotificationEnabled.value
        })
        if (allowed && eventDate != null) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (notificationManager != null) {
                createChannels(notificationManager)
                when (action) {
                    ACTION_PERIOD_REMINDER -> notifyPeriod(context, notificationManager, eventDate, prefs)
                    ACTION_OVULATION_REMINDER -> notifyOvulation(context, notificationManager, eventDate)
                    ACTION_LATE_CHECK -> notifyLate(context, notificationManager, eventDate)
                }
            }
        }
        if (!isPreview) {
            // Schedule the next occurrence from the latest data.
            RescheduleWorker.runNow(context)
        }
    }

    private fun createChannels(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val periodChannel = NotificationChannel(
            CHANNEL_PERIOD_ID,
            "یادآورهای شروع قاعدگی",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "یادآور قبل از شروع قاعدگی و پیام تأخیر پریود"
            enableLights(true)
            lightColor = PINK
            enableVibration(true)
        }
        val ovulationChannel = NotificationChannel(
            CHANNEL_OVULATION_ID,
            "یادآورهای تخمک‌گذاری و باروری",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "یادآور پنجره باروری و روز تخمینی تخمک‌گذاری"
            enableLights(true)
            lightColor = PURPLE
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(periodChannel)
        notificationManager.createNotificationChannel(ovulationChannel)
    }

    private fun daysLeftText(today: LocalDate, date: LocalDate, todayText: String, tomorrowText: String): String {
        val days = ChronoUnit.DAYS.between(today, date)
        return when {
            days <= 0 -> todayText
            days == 1L -> tomorrowText
            else -> "${PersianNumbers.toPersianDigits(days.toInt())} روز دیگر"
        }
    }

    private fun notifyPeriod(context: Context, nm: NotificationManager, date: LocalDate, prefs: UserPreferencesManager) {
        val jalali = PersianDates.long(date)
        val whenText = daysLeftText(LocalDate.now(), date, "از امروز", "از فردا")
        var body = "قاعدگی بعدی شما $whenText ($jalali) پیش‌بینی می‌شود. لوازم بهداشتی را همراه داشته باشید و با شروع خونریزی آن را ثبت کنید."
        if (prefs.isAiRemindersEnabled.value) {
            AiReminderText.nutritionTip(prefs)?.let { body += "\n$it" }
        }
        val notification = baseBuilder(context, CHANNEL_PERIOD_ID, "یادآور شروع قاعدگی 🌸", body, PINK)
            .setContentIntent(openApp(context, "calendar", 101))
            .addAction(android.R.drawable.ic_menu_edit, "ثبت امروز", openApp(context, "log", 102))
            .build()
        nm.notify(NOTIFICATION_ID_PERIOD, notification)
    }

    private fun notifyOvulation(context: Context, nm: NotificationManager, date: LocalDate) {
        val jalali = PersianDates.long(date)
        val whenText = daysLeftText(LocalDate.now(), date, "امروز", "فردا")
        val body = "روز تخمینی تخمک‌گذاری $whenText ($jalali) است و پنجره باروری از چند روز قبل آن شروع می‌شود. این تخمین است و جایگزین روش پیشگیری از بارداری نیست."
        val notification = baseBuilder(context, CHANNEL_OVULATION_ID, "یادآور تخمک‌گذاری و پنجره باروری 🌸", body, PURPLE)
            .setContentIntent(openApp(context, "calendar", 201))
            .addAction(android.R.drawable.ic_menu_edit, "ثبت علائم", openApp(context, "log", 202))
            .build()
        nm.notify(NOTIFICATION_ID_OVULATION, notification)
    }

    private fun notifyLate(context: Context, nm: NotificationManager, expectedDate: LocalDate) {
        val days = ChronoUnit.DAYS.between(expectedDate, LocalDate.now()).coerceAtLeast(1)
        val body = "${PersianNumbers.toPersianDigits(days.toInt())} روز از تاریخ پیش‌بینی‌شده (${PersianDates.long(expectedDate)}) گذشته است. " +
            "اگر پریود شروع شده آن را ثبت کنید. استرس، خواب و تغییر وزن می‌توانند باعث تأخیر شوند؛ اگر احتمال بارداری هست تست بدهید."
        val notification = baseBuilder(context, CHANNEL_PERIOD_ID, "پریود شما کمی دیر کرده است 🌷", body, PINK)
            .setContentIntent(openApp(context, "log", 301))
            .build()
        nm.notify(NOTIFICATION_ID_LATE, notification)
    }

    private fun baseBuilder(context: Context, channel: String, title: String, body: String, color: Int) =
        NotificationCompat.Builder(context, channel).privateOnLockScreen(context, channel)
            .setSmallIcon(com.example.R.drawable.ic_stat_hanita)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setColor(color)

    private fun openApp(context: Context, route: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(MainActivity.EXTRA_NAVIGATE_TO, route)
        }
        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}

/** Personalised line for the period reminder, taken from the last AI cycle analysis (if any). */
object AiReminderText {
    fun nutritionTip(prefs: UserPreferencesManager): String? = try {
        val report = prefs.getCycleAnalysisReport()
        if (report.isBlank()) null
        else {
            val adapter = com.squareup.moshi.Moshi.Builder()
                .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()
                .adapter(com.example.ui.viewmodel.CycleAnalysisResult::class.java)
            adapter.fromJson(report)?.nutritionRecommend?.trim()?.takeIf { it.isNotEmpty() }
                ?.let { "توصیه تغذیه هانیتا: " + if (it.length > 140) it.take(140).trimEnd() + "…" else it }
        }
    } catch (e: Exception) {
        Log.w("AiReminderText", "Cached analysis unreadable")
        null
    }
}
