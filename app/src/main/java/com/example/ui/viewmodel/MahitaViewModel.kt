package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.DailyLog
import com.example.data.model.MedicineReminder
import com.example.data.repository.LogRepository
import com.example.utils.FirestoreManager
import com.example.utils.PeriodReminderHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class MahitaViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = LogRepository(database.dailyLogDao())
    private val prefsManager = com.example.data.preferences.UserPreferencesManager(application)
    private val firestoreManager = FirestoreManager(database.dailyLogDao())

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncStatusMessage = MutableStateFlow<String?>(null)
    val syncStatusMessage: StateFlow<String?> = _syncStatusMessage.asStateFlow()

    val userName: StateFlow<String> = prefsManager.userName
    val userAvatar: StateFlow<String> = prefsManager.userAvatar
    val isLoggedIn: StateFlow<Boolean> = prefsManager.isLoggedIn
    val isNotificationEnabled: StateFlow<Boolean> = prefsManager.isNotificationEnabled
    val daysBefore: StateFlow<Int> = prefsManager.daysBefore
    val medicationReminders: StateFlow<List<MedicineReminder>> = prefsManager.medicationReminders

    val allLogs: StateFlow<List<DailyLog>> = repository.allLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val averageCycleLength: StateFlow<Int> = prefsManager.averageCycleLength
    val appTheme: StateFlow<String> = prefsManager.appTheme
    val appFont: StateFlow<String> = prefsManager.appFont

    // A smart prediction algorithm state flow
    val predictionInfo: StateFlow<PredictionInfo> = combine(
        repository.periodDays,
        prefsManager.averageCycleLength
    ) { periodLogs, defaultCycleLength ->
        if (periodLogs.isEmpty()) {
            PredictionInfo(
                hasEnoughData = false,
                calculatedAverage = defaultCycleLength,
                lastPeriodStart = null,
                predictedNextStart = LocalDate.now().plusDays(defaultCycleLength.toLong()),
                daysRemaining = defaultCycleLength,
                totalCyclesCalculated = 0
            )
        } else {
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            val sortedDates = periodLogs.mapNotNull {
                try {
                    LocalDate.parse(it.dateStr, formatter)
                } catch (e: Exception) {
                    null
                }
            }.sorted()

            if (sortedDates.isEmpty()) {
                PredictionInfo(
                    hasEnoughData = false,
                    calculatedAverage = defaultCycleLength,
                    lastPeriodStart = null,
                    predictedNextStart = LocalDate.now().plusDays(defaultCycleLength.toLong()),
                    daysRemaining = defaultCycleLength,
                    totalCyclesCalculated = 0
                )
            } else {
                val periodStarts = mutableListOf<LocalDate>()
                periodStarts.add(sortedDates[0])
                for (i in 1 until sortedDates.size) {
                    val prev = sortedDates[i - 1]
                    val curr = sortedDates[i]
                    val gap = ChronoUnit.DAYS.between(prev, curr)
                    if (gap > 8) {
                        periodStarts.add(curr)
                    }
                }

                val lastPeriodStart = periodStarts.last()
                val cycleIntervals = mutableListOf<Int>()
                for (i in 0 until periodStarts.size - 1) {
                    val length = ChronoUnit.DAYS.between(periodStarts[i], periodStarts[i + 1]).toInt()
                    if (length in 15..50) {
                        cycleIntervals.add(length)
                    }
                }

                val hasEnoughData = cycleIntervals.isNotEmpty()
                val calculatedAverage = if (hasEnoughData) {
                    cycleIntervals.average().toInt().coerceIn(21, 45)
                } else {
                    defaultCycleLength
                }

                val predictedNextStart = lastPeriodStart.plusDays(calculatedAverage.toLong())
                val today = LocalDate.now()
                val daysRemaining = ChronoUnit.DAYS.between(today, predictedNextStart).toInt()

                PredictionInfo(
                    hasEnoughData = hasEnoughData,
                    calculatedAverage = calculatedAverage,
                    lastPeriodStart = lastPeriodStart,
                    predictedNextStart = predictedNextStart,
                    daysRemaining = daysRemaining,
                    totalCyclesCalculated = cycleIntervals.size
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PredictionInfo())

    // A simple derived state to calculate current cycle info
    val cycleInfo: StateFlow<CycleInfo> = combine(
        repository.periodDays,
        prefsManager.averageCycleLength
    ) { periodLogs, cycleLength ->
        if (periodLogs.isEmpty()) {
            CycleInfo(
                currentDay = 1,
                daysUntilNextPeriod = cycleLength,
                predictedPeriodStart = LocalDate.now().plusDays(cycleLength.toLong()),
                isPeriod = false,
                isFertile = false
            )
        } else {
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            val lastPeriodLog = periodLogs.maxByOrNull { it.dateStr }!!
            val lastPeriodDate = LocalDate.parse(lastPeriodLog.dateStr, formatter)
            val today = LocalDate.now()
            
            var daysSincePeriodStart = ChronoUnit.DAYS.between(lastPeriodDate, today).toInt() + 1
            if (daysSincePeriodStart < 1) daysSincePeriodStart = 1 
            else if (daysSincePeriodStart > cycleLength) {
                daysSincePeriodStart = (daysSincePeriodStart - 1) % cycleLength + 1
            }

            val daysUntilNext = cycleLength - daysSincePeriodStart
            val nextPeriod = today.plusDays(daysUntilNext.toLong())
            val isPeriod = daysSincePeriodStart <= 5
            val isFertile = daysSincePeriodStart in (cycleLength / 2 - 2)..(cycleLength / 2 + 2) // dynamic fertile window

            CycleInfo(
                currentDay = daysSincePeriodStart,
                daysUntilNextPeriod = daysUntilNext,
                predictedPeriodStart = nextPeriod,
                isPeriod = isPeriod,
                isFertile = isFertile
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CycleInfo())

    init {
        try {
            if (com.google.firebase.FirebaseApp.getApps(application).isEmpty()) {
                com.google.firebase.FirebaseApp.initializeApp(application)
            }
        } catch (e: Exception) {
            android.util.Log.e("MahitaViewModel", "FirebaseApp initialization failed: ${e.message}")
        }

        val flowCycleInfo = cycleInfo
        val flowNotifEnabled = isNotificationEnabled
        val flowDaysBefore = daysBefore
        val flowIsLoggedIn = isLoggedIn
        val flowUserName = userName

        // Periodically monitor cycleInfo and preferences to schedule notifications
        viewModelScope.launch {
            if (flowCycleInfo != null && flowNotifEnabled != null && flowDaysBefore != null) {
                combine(flowCycleInfo, flowNotifEnabled, flowDaysBefore) { cycle, notifEnabled, days ->
                    Triple(cycle, notifEnabled, days)
                }.collect { (cycle, notifEnabled, days) ->
                    if (notifEnabled && cycle.predictedPeriodStart != null) {
                        PeriodReminderHelper.scheduleReminder(
                            getApplication(),
                            cycle.predictedPeriodStart,
                            days
                        )
                    } else {
                        PeriodReminderHelper.cancelReminder(getApplication())
                    }
                }
            }
        }

        // Auto sync with cloud at start if logged in
        viewModelScope.launch {
            if (flowIsLoggedIn != null && flowUserName != null) {
                flowIsLoggedIn.collect { loggedIn ->
                    if (loggedIn && flowUserName.value.isNotBlank()) {
                        syncWithCloud()
                    }
                }
            }
        }
    }

    fun login(name: String, cycleLength: Int = 28, lastPeriodDateStr: String? = null) {
        prefsManager.login(name, cycleLength)
        viewModelScope.launch {
            if (lastPeriodDateStr != null) {
                val log = DailyLog(
                    dateStr = lastPeriodDateStr,
                    periodFlow = "MEDIUM",
                    symptoms = "",
                    moods = "",
                    notes = "ثبت اطلاعات اولیه دوره من در هانیتا"
                )
                repository.insertLog(log)
                try {
                    firestoreManager.uploadLog(name, log)
                } catch (e: Exception) {
                    android.util.Log.e("MahitaViewModel", "Error uploading initial log: ${e.message}")
                }
            }
            syncWithCloud()
        }
    }

    fun logout() {
        prefsManager.logout()
        PeriodReminderHelper.cancelReminder(getApplication())
    }

    fun setNotificationEnabled(enabled: Boolean) {
        prefsManager.setNotificationEnabled(enabled)
    }

    fun setDaysBefore(days: Int) {
        prefsManager.setDaysBefore(days)
    }

    fun setAverageCycleLength(length: Int) {
        prefsManager.setAverageCycleLength(length)
    }

    fun setAppTheme(theme: String) {
        prefsManager.setAppTheme(theme)
    }

    fun setAppFont(font: String) {
        prefsManager.setAppFont(font)
    }

    fun setUserAvatar(avatar: String) {
        prefsManager.setUserAvatar(avatar)
    }

    fun saveMedicationReminders(reminders: List<MedicineReminder>) {
        prefsManager.saveMedicationReminders(reminders)
    }

    fun saveLog(dateStr: String, flow: String, symptoms: List<String>, moods: List<String>, notes: String) {
        viewModelScope.launch {
            val log = DailyLog(
                dateStr = dateStr,
                periodFlow = flow,
                symptoms = symptoms.joinToString(","),
                moods = moods.joinToString(","),
                notes = notes
            )
            repository.insertLog(log)
            
            // Sync this single log immediately to Firestore
            val name = userName.value
            if (name.isNotBlank()) {
                firestoreManager.uploadLog(name, log)
            }
        }
    }

    fun syncWithCloud() {
        val name = userName.value
        if (name.isBlank()) return

        viewModelScope.launch {
            _isSyncing.value = true
            _syncStatusMessage.value = "در حال اتصال به سرور ابری..."
            try {
                // Step 1: Download new logs from Firestore and save locally
                val downloaded = firestoreManager.downloadAndSyncLogs(name)
                
                // Step 2: Upload all local logs to Firestore to fill missing ones there
                val localLogs = allLogs.value
                val uploaded = if (localLogs.isNotEmpty()) {
                    firestoreManager.uploadAllLogs(name, localLogs)
                } else {
                    true
                }

                if (downloaded || uploaded) {
                    _syncStatusMessage.value = "همگام‌سازی ابری با موفقیت انجام شد ✨"
                } else {
                    _syncStatusMessage.value = "داده‌ها قبلا همگام‌سازی شده‌اند"
                }
            } catch (e: Exception) {
                _syncStatusMessage.value = "خطا در همگام‌سازی"
            } finally {
                _isSyncing.value = false
            }
        }
    }
    
    fun clearSyncStatusMessage() {
        _syncStatusMessage.value = null
    }
}

data class CycleInfo(
    val currentDay: Int = 1,
    val daysUntilNextPeriod: Int = 28,
    val predictedPeriodStart: LocalDate = LocalDate.now().plusDays(28),
    val isPeriod: Boolean = false,
    val isFertile: Boolean = false
)

data class PredictionInfo(
    val hasEnoughData: Boolean = false,
    val calculatedAverage: Int = 28,
    val lastPeriodStart: LocalDate? = null,
    val predictedNextStart: LocalDate? = null,
    val daysRemaining: Int = 0,
    val totalCyclesCalculated: Int = 0
)
