package com.example.data.preferences

import androidx.core.content.edit
import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.MedicineReminder
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Single in-memory view of the user's settings. Use [get]: the ViewModel, the background worker
 * and the notification receivers must share one instance, otherwise a change made in one place is
 * invisible to the others until the process restarts.
 */
class UserPreferencesManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        const val PREFS_NAME = "user_prefs"

        @Volatile
        private var instance: UserPreferencesManager? = null

        fun get(context: Context): UserPreferencesManager =
            instance ?: synchronized(this) {
                instance ?: UserPreferencesManager(context.applicationContext).also { instance = it }
            }
    }

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val remindersType = Types.newParameterizedType(List::class.java, MedicineReminder::class.java)
    private val remindersAdapter = moshi.adapter<List<MedicineReminder>>(remindersType)

    private val _userName = MutableStateFlow(prefs.getString("user_name", "") ?: "")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userWeight = MutableStateFlow(prefs.getString("user_weight", "65") ?: "65")
    val userWeight: StateFlow<String> = _userWeight.asStateFlow()

    private val _lastPeriodDate = MutableStateFlow(prefs.getString("last_period_date", "") ?: "")
    val lastPeriodDate: StateFlow<String> = _lastPeriodDate.asStateFlow()

    private val _userAvatar = MutableStateFlow(
        com.example.utils.AvatarStore.migrateLegacy(context, prefs.getString("user_avatar", "avatar_default") ?: "avatar_default")
            .also { migrated ->
                if (migrated != prefs.getString("user_avatar", "avatar_default")) prefs.edit { putString("user_avatar", migrated) }
            }
    )
    val userAvatar: StateFlow<String> = _userAvatar.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean("is_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isNotificationEnabled = MutableStateFlow(prefs.getBoolean("is_notification_enabled", true))
    val isNotificationEnabled: StateFlow<Boolean> = _isNotificationEnabled.asStateFlow()

    private val _isAiRemindersEnabled = MutableStateFlow(prefs.getBoolean("is_ai_reminders_enabled", true))
    val isAiRemindersEnabled: StateFlow<Boolean> = _isAiRemindersEnabled.asStateFlow()

    private val _isOvulationRemindersEnabled = MutableStateFlow(prefs.getBoolean("is_ovulation_reminders_enabled", true))
    val isOvulationRemindersEnabled: StateFlow<Boolean> = _isOvulationRemindersEnabled.asStateFlow()

    private val _daysBefore = MutableStateFlow(prefs.getInt("days_before_notification", 1))
    val daysBefore: StateFlow<Int> = _daysBefore.asStateFlow()

    private val _ovulationDaysBefore = MutableStateFlow(prefs.getInt("ovulation_days_before_notification", 1))
    val ovulationDaysBefore: StateFlow<Int> = _ovulationDaysBefore.asStateFlow()

    private val _notificationHour = MutableStateFlow(prefs.getInt("notification_hour", 9))
    val notificationHour: StateFlow<Int> = _notificationHour.asStateFlow()

    private val _notificationMinute = MutableStateFlow(prefs.getInt("notification_minute", 0))
    val notificationMinute: StateFlow<Int> = _notificationMinute.asStateFlow()

    private val _averageCycleLength = MutableStateFlow(prefs.getInt("average_cycle_length", 28))
    val averageCycleLength: StateFlow<Int> = _averageCycleLength.asStateFlow()

    private val _appTheme = MutableStateFlow(prefs.getString("app_theme", "COSMIC_BLOSSOM") ?: "COSMIC_BLOSSOM")
    val appTheme: StateFlow<String> = _appTheme.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _appFont = MutableStateFlow(prefs.getString("app_font", "CLASSIC") ?: "CLASSIC")
    val appFont: StateFlow<String> = _appFont.asStateFlow()

    private val _medicationReminders = MutableStateFlow(getMedicationRemindersFromPrefs())
    val medicationReminders: StateFlow<List<MedicineReminder>> = _medicationReminders.asStateFlow()

    private val _favoriteTracks = MutableStateFlow(prefs.getStringSet("favorite_tracks", emptySet())?.toSet() ?: emptySet())
    /** Ids of the music tracks the user marked as favourite. */
    val favoriteTracks: StateFlow<Set<String>> = _favoriteTracks.asStateFlow()

    fun toggleFavoriteTrack(trackId: String) {
        val updated = _favoriteTracks.value.toMutableSet().apply { if (!add(trackId)) remove(trackId) }
        prefs.edit { putStringSet("favorite_tracks", updated) }
        _favoriteTracks.value = updated
    }

    private val _aiConsentGiven = MutableStateFlow(prefs.getBoolean("ai_data_consent", false))
    /** Whether the user agreed to send cycle data to the AI service (asked once, revocable). */
    val aiConsentGiven: StateFlow<Boolean> = _aiConsentGiven.asStateFlow()

    fun setAiConsentGiven(given: Boolean) {
        prefs.edit { putBoolean("ai_data_consent", given) }
        _aiConsentGiven.value = given
    }

    private val _customPhysicalSymptoms = MutableStateFlow(
        prefs.getStringSet("custom_physical_symptoms", emptySet())?.toList() ?: emptyList()
    )
    val customPhysicalSymptoms: StateFlow<List<String>> = _customPhysicalSymptoms.asStateFlow()

    private val _customMoodSymptoms = MutableStateFlow(
        prefs.getStringSet("custom_mood_symptoms", emptySet())?.toList() ?: emptyList()
    )
    val customMoodSymptoms: StateFlow<List<String>> = _customMoodSymptoms.asStateFlow()


    fun login(name: String, cycleLength: Int = 28) {
        prefs.edit {
            putString("user_name", name)
            putBoolean("is_logged_in", true)
            putInt("average_cycle_length", cycleLength)
        }
        _userName.value = name
        _isLoggedIn.value = true
        _averageCycleLength.value = cycleLength
    }

    fun logout() {
        prefs.edit {
            remove("user_name")
            putBoolean("is_logged_in", false)
        }
        _userName.value = ""
        _isLoggedIn.value = false
    }

    fun isFutureFlowCleanupDone(): Boolean = prefs.getBoolean("future_flow_cleanup_v1", false)

    fun markFutureFlowCleanupDone() = prefs.edit { putBoolean("future_flow_cleanup_v1", true) }

    fun isMedicationReminderActive(id: String): Boolean =
        _isLoggedIn.value && _medicationReminders.value.any { it.id == id && it.isEnabled }

    fun setNotificationEnabled(enabled: Boolean) {
        prefs.edit { putBoolean("is_notification_enabled", enabled) }
        _isNotificationEnabled.value = enabled
    }

    fun setAiRemindersEnabled(enabled: Boolean) {
        prefs.edit { putBoolean("is_ai_reminders_enabled", enabled) }
        _isAiRemindersEnabled.value = enabled
    }

    fun setOvulationRemindersEnabled(enabled: Boolean) {
        prefs.edit { putBoolean("is_ovulation_reminders_enabled", enabled) }
        _isOvulationRemindersEnabled.value = enabled
    }

    fun setDaysBefore(days: Int) {
        prefs.edit { putInt("days_before_notification", days) }
        _daysBefore.value = days
    }

    fun setOvulationDaysBefore(days: Int) {
        prefs.edit { putInt("ovulation_days_before_notification", days) }
        _ovulationDaysBefore.value = days
    }

    fun setNotificationTime(hour: Int, minute: Int) {
        prefs.edit {
            putInt("notification_hour", hour)
            putInt("notification_minute", minute)
        }
        _notificationHour.value = hour
        _notificationMinute.value = minute
    }

    fun setAverageCycleLength(length: Int) {
        prefs.edit { putInt("average_cycle_length", length) }
        _averageCycleLength.value = length
    }

    fun setAppTheme(theme: String) {
        prefs.edit { putString("app_theme", theme) }
        _appTheme.value = theme
    }

    fun setThemeMode(mode: String) {
        prefs.edit { putString("theme_mode", mode) }
        _themeMode.value = mode
    }

    fun setAppFont(font: String) {
        prefs.edit { putString("app_font", font) }
        _appFont.value = font
    }

    fun setUserAvatar(avatar: String) {
        prefs.edit { putString("user_avatar", avatar) }
        _userAvatar.value = avatar
    }

    fun setUserWeight(weight: String) {
        prefs.edit { putString("user_weight", weight) }
        _userWeight.value = weight
    }

    fun setLastPeriodDate(dateStr: String) {
        prefs.edit { putString("last_period_date", dateStr) }
        _lastPeriodDate.value = dateStr
    }

    private fun getMedicationRemindersFromPrefs(): List<MedicineReminder> {
        val json = prefs.getString("medication_reminders_json", null)
        if (json.isNullOrBlank()) {
            // Templates only: all disabled until the user turns one on.
            return listOf(
                MedicineReminder("ref1", "قرص آهن (سولفات آهن)", "21:00", "طبق دستور پزشک", false, "pill"),
                MedicineReminder("ref2", "مسکن (طبق تجویز پزشک)", "08:00", "طبق دستور پزشک", false, "capsule"),
                MedicineReminder("ref3", "منیزیم و ویتامین ب۶", "14:30", "طبق دستور پزشک", false, "powder")
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
            prefs.edit { putString("medication_reminders_json", json) }
            _medicationReminders.value = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCycleAnalysisReport(): String {
        return prefs.getString("cycle_analysis_report", "") ?: ""
    }

    fun saveCycleAnalysisReport(report: String) {
        prefs.edit { putString("cycle_analysis_report", report) }
    }


    fun addCustomPhysicalSymptom(symptom: String) {
        val clean = sanitizeLabel(symptom) ?: return
        val currentSet = prefs.getStringSet("custom_physical_symptoms", emptySet()) ?: emptySet()
        val newSet = currentSet.toMutableSet().apply { add(clean) }
        prefs.edit { putStringSet("custom_physical_symptoms", newSet) }
        _customPhysicalSymptoms.value = newSet.toList()
    }

    fun removeCustomPhysicalSymptom(symptom: String) {
        val currentSet = prefs.getStringSet("custom_physical_symptoms", emptySet()) ?: emptySet()
        val newSet = currentSet.toMutableSet().apply { remove(symptom) }
        prefs.edit { putStringSet("custom_physical_symptoms", newSet) }
        _customPhysicalSymptoms.value = newSet.toList()
    }

    fun addCustomMoodSymptom(symptom: String) {
        val clean = sanitizeLabel(symptom) ?: return
        val currentSet = prefs.getStringSet("custom_mood_symptoms", emptySet()) ?: emptySet()
        val newSet = currentSet.toMutableSet().apply { add(clean) }
        prefs.edit { putStringSet("custom_mood_symptoms", newSet) }
        _customMoodSymptoms.value = newSet.toList()
    }

    fun removeCustomMoodSymptom(symptom: String) {
        val currentSet = prefs.getStringSet("custom_mood_symptoms", emptySet()) ?: emptySet()
        val newSet = currentSet.toMutableSet().apply { remove(symptom) }
        prefs.edit { putStringSet("custom_mood_symptoms", newSet) }
        _customMoodSymptoms.value = newSet.toList()
    }

    /**
     * Symptom and mood labels are stored comma-separated in a log row, so a comma inside a
     * custom label would split it in two. Returns null for an empty label.
     */
    fun sanitizeLabel(raw: String): String? =
        raw.replace(Regex("[,،;\\n\\r]"), " ").replace(Regex("\\s+"), " ").trim().take(MAX_LABEL_LENGTH)
            .takeIf { it.isNotEmpty() }

    private val MAX_LABEL_LENGTH = 30
}
