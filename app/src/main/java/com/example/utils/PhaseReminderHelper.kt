package com.example.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.domain.ReminderPlanner
import java.time.ZoneId

object PhaseReminderHelper {
    private const val BASE_REQUEST_CODE = 5000

    fun title(kind: ReminderPlanner.PhaseKind): String = when (kind) {
        ReminderPlanner.PhaseKind.MENSTRUAL -> "فاز قاعدگی"
        ReminderPlanner.PhaseKind.FOLLICULAR -> "فاز فولیکولی"
        ReminderPlanner.PhaseKind.OVULATION -> "فاز تخمک‌گذاری"
        ReminderPlanner.PhaseKind.LUTEAL -> "فاز لوتئال"
    }

    fun message(kind: ReminderPlanner.PhaseKind): String = when (kind) {
        ReminderPlanner.PhaseKind.MENSTRUAL -> "طبق پیش‌بینی، قاعدگی از امروز شروع می‌شود. اگر شروع شد آن را ثبت کنید؛ استراحت و مایعات گرم کمک می‌کند."
        ReminderPlanner.PhaseKind.FOLLICULAR -> "انرژی در حال افزایش است. زمان خوبی برای برنامه‌های جدید و فعالیت بدنی است."
        ReminderPlanner.PhaseKind.OVULATION -> "روزهای اوج انرژی و احتمال باروری است. این تخمین است و جایگزین روش پیشگیری نیست."
        ReminderPlanner.PhaseKind.LUTEAL -> "به خواب کافی، آرامش ذهنی و تغذیه سبک توجه کنید؛ ممکن است علائم پیش از قاعدگی شروع شود."
    }

    fun apply(context: Context, phases: List<ReminderPlanner.PhaseReminder>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val planned = phases.associateBy { it.kind }
        ReminderPlanner.PhaseKind.entries.forEach { kind ->
            val pendingIntent = pendingIntent(context, kind)
            alarmManager.cancel(pendingIntent)
            val reminder = planned[kind] ?: return@forEach
            try {
                val millis = reminder.fireAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pendingIntent)
            } catch (e: Exception) {
                Log.e("PhaseReminderHelper", "Failed to schedule ${kind.name}: ${e.message}")
            }
        }
    }

    private fun pendingIntent(context: Context, kind: ReminderPlanner.PhaseKind): PendingIntent {
        val intent = Intent(context, PhaseReminderReceiver::class.java).apply {
            putExtra(PhaseReminderReceiver.EXTRA_PHASE, kind.name)
        }
        return PendingIntent.getBroadcast(
            context,
            BASE_REQUEST_CODE + kind.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun cancelAllPhaseReminders(context: Context) = apply(context, emptyList())
}
