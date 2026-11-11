package com.hanita.partner

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.hanita.core.partner.PartnerContent
import com.hanita.core.partner.PartnerDay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * One daily alarm at the time the partner chose. When it fires, today's events are worked out
 * from the stored card and a discreet notification is shown. Inexact alarms need no special
 * permission and survive Doze (they may arrive a few minutes late).
 */
object Reminders {
    private const val CHANNEL_ID = "partner_reminders"
    private const val NOTIFICATION_ID = 7101
    private const val REQUEST_CODE = 7100

    fun reschedule(context: Context) {
        val store = PartnerStore(context)
        val alarm = context.getSystemService(AlarmManager::class.java) ?: return
        val pending = pendingIntent(context)
        alarm.cancel(pending)
        if (!store.remindersEnabled || store.pairing == null) return
        val minute = store.reminderMinuteOfDay
        val time = LocalTime.of(minute / 60, minute % 60)
        val now = LocalDateTime.now()
        var next = LocalDate.now().atTime(time)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val millis = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pending)
    }

    private fun pendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context, REQUEST_CODE,
            Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    /** The text to show today, or null when there is nothing to remind. */
    fun textForToday(store: PartnerStore, today: LocalDate): String? {
        val card = store.card ?: return null
        val events = PartnerDay.eventsOn(card, today, store.reminderEvents, store.daysBeforePeriod)
        if (events.isNotEmpty()) {
            // Several events on one day: the most important one (period first).
            val event = listOf(
                PartnerDay.Event.PERIOD_START, PartnerDay.Event.PERIOD_SOON,
                PartnerDay.Event.PMS_START, PartnerDay.Event.FERTILE_START
            ).first { it in events }
            return PartnerContent.notificationText(event, card.ownerName, store.daysBeforePeriod)
        }
        if (store.dailyTip && card.hasCycleData) {
            val phase = PartnerDay.today(card, today)
            return "💡 " + PartnerContent.dailyTipText(phase.phase, phase.fertile, today.dayOfYear)
        }
        return null
    }

    fun notify(context: Context, text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "یادآوری‌های همراه", NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "یادآوری روزهای خاص و توصیه روزانه"
            context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
        val open = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // On the lock screen only a neutral line is visible.
        val publicVersion = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Gentleman")
            .setContentText("یک یادآوری تازه داری")
            .build()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Gentleman")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(open)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicVersion)
            .build()
        try {
            manager.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Permission revoked between the check and the call.
        }
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val store = PartnerStore(context)
        if (store.remindersEnabled) {
            Reminders.textForToday(store, LocalDate.now())?.let { Reminders.notify(context, it) }
        }
        Reminders.reschedule(context)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            Reminders.reschedule(context)
        }
    }
}
