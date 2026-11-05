package com.example.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.time.LocalDate

class RescheduleWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("RescheduleWorker", "Rescheduling all notifications after boot...")
        try {
            val prefs = applicationContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            
            // 1. Reschedule medications
            val moshi = com.squareup.moshi.Moshi.Builder()
                .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()
            val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, com.example.data.model.MedicineReminder::class.java)
            val adapter = moshi.adapter<List<com.example.data.model.MedicineReminder>>(listType)
            val json = prefs.getString("medication_reminders_json", null)
            val meds = if (json != null) {
                try {
                    adapter.fromJson(json) ?: emptyList()
                } catch(e: Exception) { emptyList() }
            } else {
                listOf(
                    com.example.data.model.MedicineReminder("ref1", "قرص آهن (سولفات آهن)", "21:00", "۱ عدد شبانه", true, "pill"),
                    com.example.data.model.MedicineReminder("ref2", "ایبوپروفن (مسکن مسکن قاعدگی)", "08:00", "در صورت بروز دردهای شکمی شدید", false, "capsule"),
                    com.example.data.model.MedicineReminder("ref3", "منیزیم و ویتامین ب۶", "14:30", "۱ ساشه بعد از وعده ناهار برای تسکین عضلانی", true, "powder")
                )
            }
            MedicineReminderHelper.scheduleReminders(applicationContext, meds)
            
            // 2. Reschedule Cycle Reminders
            val pEnabled = prefs.getBoolean("is_notification_enabled", true)
            val oEnabled = prefs.getBoolean("is_ovulation_reminders_enabled", true)
            val pDays = prefs.getInt("days_before_notification", 1)
            val oDays = prefs.getInt("ovulation_days_before_notification", 1)
            val h = prefs.getInt("notification_hour", 9)
            val m = prefs.getInt("notification_minute", 0)
            val cycleLength = prefs.getInt("average_cycle_length", 28)
            val lastPeriodDateStr = prefs.getString("last_period_date", "") ?: ""
            
            if (lastPeriodDateStr.isNotBlank()) {
                try {
                    val lastStart = LocalDate.parse(lastPeriodDateStr)
                    CycleNotificationHelper.scheduleAllReminders(
                        context = applicationContext,
                        lastPeriodStart = lastStart,
                        cycleLength = cycleLength,
                        isPeriodEnabled = pEnabled,
                        daysBeforePeriod = pDays,
                        isOvulationEnabled = oEnabled,
                        daysBeforeOvulation = oDays,
                        hour = h,
                        minute = m
                    )
                } catch (e: Exception) {
                    Log.e("RescheduleWorker", "Error parsing date: ${e.message}")
                }
            }
            return Result.success()
        } catch (e: Exception) {
            Log.e("RescheduleWorker", "Error rescheduling: ${e.message}")
            return Result.failure()
        }
    }
}
