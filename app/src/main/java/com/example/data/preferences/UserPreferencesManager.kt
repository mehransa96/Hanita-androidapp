package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.MedicineReminder
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val remindersType = Types.newParameterizedType(List::class.java, MedicineReminder::class.java)
    private val remindersAdapter = moshi.adapter<List<MedicineReminder>>(remindersType)

    private val _userName = MutableStateFlow(prefs.getString("user_name", "") ?: "")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userAvatar = MutableStateFlow(prefs.getString("user_avatar", "avatar_default") ?: "avatar_default")
    val userAvatar: StateFlow<String> = _userAvatar.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean("is_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isNotificationEnabled = MutableStateFlow(prefs.getBoolean("is_notification_enabled", true))
    val isNotificationEnabled: StateFlow<Boolean> = _isNotificationEnabled.asStateFlow()

    private val _daysBefore = MutableStateFlow(prefs.getInt("days_before_notification", 1))
    val daysBefore: StateFlow<Int> = _daysBefore.asStateFlow()

    private val _averageCycleLength = MutableStateFlow(prefs.getInt("average_cycle_length", 28))
    val averageCycleLength: StateFlow<Int> = _averageCycleLength.asStateFlow()

    private val _appTheme = MutableStateFlow(prefs.getString("app_theme", "COSMIC_BLOSSOM") ?: "COSMIC_BLOSSOM")
    val appTheme: StateFlow<String> = _appTheme.asStateFlow()

    private val _appFont = MutableStateFlow(prefs.getString("app_font", "CLASSIC") ?: "CLASSIC")
    val appFont: StateFlow<String> = _appFont.asStateFlow()

    private val _medicationReminders = MutableStateFlow(getMedicationRemindersFromPrefs())
    val medicationReminders: StateFlow<List<MedicineReminder>> = _medicationReminders.asStateFlow()

    fun login(name: String, cycleLength: Int = 28) {
        prefs.edit().apply {
            putString("user_name", name)
            putBoolean("is_logged_in", true)
            putInt("average_cycle_length", cycleLength)
            apply()
        }
        _userName.value = name
        _isLoggedIn.value = true
        _averageCycleLength.value = cycleLength
    }

    fun logout() {
        prefs.edit().apply {
            remove("user_name")
            putBoolean("is_logged_in", false)
            apply()
        }
        _userName.value = ""
        _isLoggedIn.value = false
    }

    fun setNotificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("is_notification_enabled", enabled).apply()
        _isNotificationEnabled.value = enabled
    }

    fun setDaysBefore(days: Int) {
        prefs.edit().putInt("days_before_notification", days).apply()
        _daysBefore.value = days
    }

    fun setAverageCycleLength(length: Int) {
        prefs.edit().putInt("average_cycle_length", length).apply()
        _averageCycleLength.value = length
    }

    fun setAppTheme(theme: String) {
        prefs.edit().putString("app_theme", theme).apply()
        _appTheme.value = theme
    }

    fun setAppFont(font: String) {
        prefs.edit().putString("app_font", font).apply()
        _appFont.value = font
    }

    fun setUserAvatar(avatar: String) {
        prefs.edit().putString("user_avatar", avatar).apply()
        _userAvatar.value = avatar
    }

    private fun getMedicationRemindersFromPrefs(): List<MedicineReminder> {
        val json = prefs.getString("medication_reminders_json", null)
        if (json.isNullOrBlank()) {
            return listOf(
                MedicineReminder("ref1", "قرص آهن (سولفات آهن)", "21:00", "۱ عدد شبانه", true, "pill"),
                MedicineReminder("ref2", "ایبوپروفن (مسکن مسکن قاعدگی)", "08:00", "در صورت بروز دردهای شکمی شدید", false, "capsule"),
                MedicineReminder("ref3", "منیزیم و ویتامین ب۶", "14:30", "۱ ساشه بعد از وعده ناهار برای تسکین عضلانی", true, "powder")
            )
        }
        return try {
            remindersAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveMedicationReminders(list: List<MedicineReminder>) {
        try {
            val json = remindersAdapter.toJson(list)
            prefs.edit().putString("medication_reminders_json", json).apply()
            _medicationReminders.value = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
