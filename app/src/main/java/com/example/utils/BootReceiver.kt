package com.example.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.d("BootReceiver", "Boot completed received. Restoring reminders...")
            // The cleanest way to restore is via WorkManager to fetch from DB/Preferences and reschedule.
            // But since our state is in MahitaViewModel and DataStore, we might need a worker to trigger it.
            // For now, we will create a RescheduleWorker.
            val request = OneTimeWorkRequestBuilder<RescheduleWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
