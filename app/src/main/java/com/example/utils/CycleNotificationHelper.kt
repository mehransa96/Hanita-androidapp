package com.example.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.domain.ReminderPlanner
import java.time.LocalDate
import java.time.ZoneId

/** Schedules the period, ovulation and late-period alarms decided by [ReminderPlanner]. */
object CycleNotificationHelper {
    private const val TAG = "CycleNotificationHelper"

    private const val REQUEST_CODE_PERIOD = 7001
    private const val REQUEST_CODE_OVULATION = 7002
    private const val REQUEST_CODE_LATE = 7003

    fun apply(context: Context, plan: ReminderPlanner.Plan) {
        set(context, CycleNotificationReceiver.ACTION_PERIOD_REMINDER, REQUEST_CODE_PERIOD, plan.period)
        set(context, CycleNotificationReceiver.ACTION_OVULATION_REMINDER, REQUEST_CODE_OVULATION, plan.ovulation)
        set(context, CycleNotificationReceiver.ACTION_LATE_CHECK, REQUEST_CODE_LATE, plan.lateCheck)
    }

    fun cancelAll(context: Context) = apply(context, ReminderPlanner.Plan.EMPTY)

    private fun set(context: Context, action: String, requestCode: Int, reminder: ReminderPlanner.Reminder?) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val pendingIntent = pendingIntent(context, action, requestCode, reminder?.eventDate)
            alarmManager.cancel(pendingIntent)
            if (reminder == null) return
            val millis = reminder.fireAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pendingIntent)
            Log.d(TAG, "Scheduled $action at ${reminder.fireAt} for ${reminder.eventDate}")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling $action: ${e.message}")
        }
    }

    private fun pendingIntent(context: Context, action: String, requestCode: Int, eventDate: LocalDate?): PendingIntent {
        val intent = Intent(context, CycleNotificationReceiver::class.java).apply {
            this.action = action
            if (eventDate != null) putExtra(CycleNotificationReceiver.EXTRA_EVENT_DATE, eventDate.toString())
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** Shows a preview of a reminder right away (from the reminders screen). */
    fun sendPreview(context: Context, action: String, eventDate: LocalDate) {
        val intent = Intent(context, CycleNotificationReceiver::class.java).apply {
            this.action = action
            putExtra(CycleNotificationReceiver.EXTRA_EVENT_DATE, eventDate.toString())
            putExtra(CycleNotificationReceiver.EXTRA_PREVIEW, true)
        }
        context.sendBroadcast(intent)
    }
}
