package com.example.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object CycleNotificationHelper {
    private const val TAG = "CycleNotificationHelper"

    private const val REQUEST_CODE_PERIOD = 7001
    private const val REQUEST_CODE_OVULATION = 7002

    fun scheduleAllReminders(
        context: Context,
        lastPeriodStart: LocalDate,
        cycleLength: Int,
        isPeriodEnabled: Boolean,
        daysBeforePeriod: Int,
        isOvulationEnabled: Boolean,
        daysBeforeOvulation: Int,
        hour: Int = 9,
        minute: Int = 0
    ) {
        val safeCycleLength = if (cycleLength in 20..45) cycleLength else 28
        val today = LocalDate.now()

        // 1. Calculate future period start
        var projectedPeriod = lastPeriodStart
        while (!projectedPeriod.isAfter(today)) {
            projectedPeriod = projectedPeriod.plusDays(safeCycleLength.toLong())
        }

        // Schedule or cancel Period Reminder
        if (isPeriodEnabled) {
            var targetPeriodDate = projectedPeriod.minusDays(daysBeforePeriod.toLong())
            var targetPeriodStart = projectedPeriod

            // If scheduled date/time is already past for this upcoming period, schedule for the next cycle
            val nowTime = LocalTime.now()
            if (targetPeriodDate.isBefore(today) || (targetPeriodDate.isEqual(today) && nowTime.isAfter(LocalTime.of(hour, minute)))) {
                targetPeriodStart = projectedPeriod.plusDays(safeCycleLength.toLong())
                targetPeriodDate = targetPeriodStart.minusDays(daysBeforePeriod.toLong())
            }

            val jalaliDate = JalaliCalendar.gregorianToJalali(targetPeriodStart).formatShort()
            val daysUntil = ChronoUnit.DAYS.between(today, targetPeriodStart).toInt().coerceAtLeast(0)

            scheduleAlarm(
                context = context,
                action = CycleNotificationReceiver.ACTION_PERIOD_REMINDER,
                requestCode = REQUEST_CODE_PERIOD,
                targetDate = targetPeriodDate,
                hour = hour,
                minute = minute,
                title = "یادآور هوشمند شروع قاعدگی 🌸",
                body = "دوره جدید طبق محاسبات چرخه شما برای $jalaliDate پیش‌بینی شده است. برای آرامش بیشتر و ثبت علائم به هانیتا سر بزنید.",
                jalaliDate = jalaliDate,
                daysLeft = daysUntil
            )
            Log.d(TAG, "Scheduled Period Reminder on $targetPeriodDate at $hour:$minute for period starting $targetPeriodStart")
        } else {
            cancelReminder(context, CycleNotificationReceiver.ACTION_PERIOD_REMINDER, REQUEST_CODE_PERIOD)
        }

        // 2. Schedule or cancel Ovulation Reminder
        if (isOvulationEnabled) {
            val ovulationDayInCycle = (safeCycleLength - 14).coerceAtLeast(10)
            var currentCycleStart = projectedPeriod.minusDays(safeCycleLength.toLong())
            var projectedOvulation = currentCycleStart.plusDays(ovulationDayInCycle.toLong())

            // If this cycle's ovulation is already past, project to next cycle
            if (projectedOvulation.isBefore(today)) {
                projectedOvulation = projectedPeriod.plusDays(ovulationDayInCycle.toLong())
            }

            var targetOvulationDate = projectedOvulation.minusDays(daysBeforeOvulation.toLong())
            var targetOvulationDay = projectedOvulation

            val nowTime = LocalTime.now()
            if (targetOvulationDate.isBefore(today) || (targetOvulationDate.isEqual(today) && nowTime.isAfter(LocalTime.of(hour, minute)))) {
                targetOvulationDay = projectedPeriod.plusDays(safeCycleLength.toLong()).plusDays(ovulationDayInCycle.toLong())
                targetOvulationDate = targetOvulationDay.minusDays(daysBeforeOvulation.toLong())
            }

            val jalaliOvulationDate = JalaliCalendar.gregorianToJalali(targetOvulationDay).formatShort()
            val daysUntilOvulation = ChronoUnit.DAYS.between(today, targetOvulationDay).toInt().coerceAtLeast(0)

            scheduleAlarm(
                context = context,
                action = CycleNotificationReceiver.ACTION_OVULATION_REMINDER,
                requestCode = REQUEST_CODE_OVULATION,
                targetDate = targetOvulationDate,
                hour = hour,
                minute = minute,
                title = "یادآور تخمک‌گذاری و پنجره باروری 🥚✨",
                body = "زمان تخمک‌گذاری شما برای $jalaliOvulationDate تخمین زده شده است. روزهای اوج انرژی و بالاترین احتمال باروری در انتظار شماست.",
                jalaliDate = jalaliOvulationDate,
                daysLeft = daysUntilOvulation
            )
            Log.d(TAG, "Scheduled Ovulation Reminder on $targetOvulationDate at $hour:$minute for ovulation at $targetOvulationDay")
        } else {
            cancelReminder(context, CycleNotificationReceiver.ACTION_OVULATION_REMINDER, REQUEST_CODE_OVULATION)
        }
    }

    private fun scheduleAlarm(
        context: Context,
        action: String,
        requestCode: Int,
        targetDate: LocalDate,
        hour: Int,
        minute: Int,
        title: String,
        body: String,
        jalaliDate: String,
        daysLeft: Int
    ) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, CycleNotificationReceiver::class.java).apply {
                this.action = action
                putExtra(CycleNotificationReceiver.EXTRA_TITLE, title)
                putExtra(CycleNotificationReceiver.EXTRA_BODY, body)
                putExtra(CycleNotificationReceiver.EXTRA_DATE_JALALI, jalaliDate)
                putExtra(CycleNotificationReceiver.EXTRA_DAYS_LEFT, daysLeft)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val reminderDateTime = LocalDateTime.of(targetDate, LocalTime.of(hour, minute))
            val epochMillis = reminderDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            alarmManager.cancel(pendingIntent)

            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, epochMillis, pendingIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling alarm for action $action: ${e.message}")
        }
    }

    private fun cancelReminder(context: Context, action: String, requestCode: Int) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, CycleNotificationReceiver::class.java).apply {
                this.action = action
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "Cancelled reminder for $action")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling alarm $action: ${e.message}")
        }
    }

    fun cancelAll(context: Context) {
        cancelReminder(context, CycleNotificationReceiver.ACTION_PERIOD_REMINDER, REQUEST_CODE_PERIOD)
        cancelReminder(context, CycleNotificationReceiver.ACTION_OVULATION_REMINDER, REQUEST_CODE_OVULATION)
    }

    // Trigger immediate notifications for testing or manual preview
    fun sendImmediateNotification(
        context: Context,
        isOvulation: Boolean,
        customTitle: String? = null,
        customBody: String? = null,
        dateJalali: String = "",
        daysLeft: Int = 1
    ) {
        val intent = Intent(context, CycleNotificationReceiver::class.java).apply {
            action = if (isOvulation) {
                CycleNotificationReceiver.ACTION_OVULATION_REMINDER
            } else {
                CycleNotificationReceiver.ACTION_PERIOD_REMINDER
            }
            putExtra(CycleNotificationReceiver.EXTRA_TITLE, customTitle)
            putExtra(CycleNotificationReceiver.EXTRA_BODY, customBody)
            putExtra(CycleNotificationReceiver.EXTRA_DATE_JALALI, dateJalali)
            putExtra(CycleNotificationReceiver.EXTRA_DAYS_LEFT, daysLeft)
        }
        context.sendBroadcast(intent)
    }
}
