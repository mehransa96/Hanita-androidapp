package com.example.utils

import android.content.Context
import androidx.core.app.NotificationCompat

/**
 * Cycle and medication reminders are sensitive health information. On a locked screen only a
 * neutral text is shown; the real content appears after the device is unlocked.
 */
fun NotificationCompat.Builder.privateOnLockScreen(context: Context, channelId: String): NotificationCompat.Builder {
    val publicVersion = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_popup_reminder)
        .setContentTitle("هانیتا")
        .setContentText("یک یادآوری جدید دارید")
        .build()
    return setVisibility(NotificationCompat.VISIBILITY_PRIVATE).setPublicVersion(publicVersion)
}
