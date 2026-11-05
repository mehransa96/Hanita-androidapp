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
import com.example.utils.CycleNotificationHelper
import com.example.utils.JalaliCalendar
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

class MahitaViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = LogRepository(database.dailyLogDao(), database.physicalSymptomDao())
    private val prefsManager = com.example.data.preferences.UserPreferencesManager(application)
    private val firestoreManager = FirestoreManager(database.dailyLogDao(), database.cycleRecordDao())

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncStatusMessage = MutableStateFlow<String?>(null)
    val syncStatusMessage: StateFlow<String?> = _syncStatusMessage.asStateFlow()

    val userName: StateFlow<String> = prefsManager.userName
    val userWeight: StateFlow<String> = prefsManager.userWeight
    val lastPeriodDate: StateFlow<String> = prefsManager.lastPeriodDate
    val userAvatar: StateFlow<String> = prefsManager.userAvatar

    fun incrementWater() {
        viewModelScope.launch {
            val todayStr = LocalDate.now().toString()
            val existing = repository.getLogByDate(todayStr)
            val current = existing?.waterIntake ?: 0
            val newAmount = (current + 1).coerceAtMost(16)
            
            val newLog = existing?.copy(waterIntake = newAmount) ?: DailyLog(
                dateStr = todayStr,
                periodFlow = "NONE",
                symptoms = "",
                moods = "",
                notes = "",
                waterIntake = newAmount
            )
            repository.insertLog(newLog)
        }
    }

    fun decrementWater() {
    
        viewModelScope.launch {
            val todayStr = LocalDate.now().toString()
            val existing = repository.getLogByDate(todayStr)
            val current = existing?.waterIntake ?: 0
            val newAmount = (current - 1).coerceAtLeast(0)
            
            val newLog = existing?.copy(waterIntake = newAmount) ?: DailyLog(
                dateStr = todayStr,
                periodFlow = "NONE",
                symptoms = "",
                moods = "",
                notes = "",
                waterIntake = newAmount
            )
            repository.insertLog(newLog)
        }
    }




    fun logQuickMood(mood: String) {
        viewModelScope.launch {
            val todayStr = LocalDate.now().toString()
            val existing = repository.getLogByDate(todayStr)
            if (existing != null) {
                val currentMoods = if (existing.moods.isBlank()) mutableListOf() else existing.moods.split(",").map { it.trim() }.toMutableList()
                if (!currentMoods.contains(mood)) {
                    currentMoods.add(mood)
                }
                repository.insertLog(existing.copy(moods = currentMoods.joinToString(",")))
            } else {
                repository.insertLog(
                    DailyLog(
                        dateStr = todayStr,
                        periodFlow = "NONE",
                        symptoms = "",
                        moods = mood,
                        notes = "ثبت سریع از صفحه اصلی"
                    )
                )
            }
        }
    }
    val isLoggedIn: StateFlow<Boolean> = prefsManager.isLoggedIn
    val isNotificationEnabled: StateFlow<Boolean> = prefsManager.isNotificationEnabled
    val isAiRemindersEnabled: StateFlow<Boolean> = prefsManager.isAiRemindersEnabled
    val isOvulationRemindersEnabled: StateFlow<Boolean> = prefsManager.isOvulationRemindersEnabled
    val daysBefore: StateFlow<Int> = prefsManager.daysBefore
    val ovulationDaysBefore: StateFlow<Int> = prefsManager.ovulationDaysBefore
    val notificationHour: StateFlow<Int> = prefsManager.notificationHour
    val notificationMinute: StateFlow<Int> = prefsManager.notificationMinute
    val medicationReminders: StateFlow<List<MedicineReminder>> = prefsManager.medicationReminders
    val customPhysicalSymptoms: StateFlow<List<String>> = prefsManager.customPhysicalSymptoms
    val customMoodSymptoms: StateFlow<List<String>> = prefsManager.customMoodSymptoms

    val allLogs: StateFlow<List<DailyLog>> = repository.allLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val waterGlasses: StateFlow<Int> = allLogs.map { logs -> logs.find { it.dateStr == LocalDate.now().toString() }?.waterIntake ?: 0 }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val allPhysicalSymptoms: StateFlow<List<com.example.data.model.PhysicalSymptomLog>> = (repository.allPhysicalSymptoms ?: kotlinx.coroutines.flow.flowOf(emptyList())).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun getPhysicalSymptomsForDate(dateStr: String): Flow<List<com.example.data.model.PhysicalSymptomLog>> {
        return repository.getPhysicalSymptomsForDate(dateStr)
    }

    suspend fun getPhysicalSymptomsListForDate(dateStr: String): List<com.example.data.model.PhysicalSymptomLog> {
        return repository.getPhysicalSymptomsListForDate(dateStr)
    }

    val allCycleRecords: StateFlow<List<com.example.data.model.CycleRecord>> = database.cycleRecordDao().getAllCycleRecords().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateCycleRecord(cycleRecord: com.example.data.model.CycleRecord) {
        viewModelScope.launch {
            database.cycleRecordDao().insertCycleRecord(cycleRecord)
            val name = userName.value
            if (name.isNotBlank()) {
                firestoreManager.uploadCycleRecord(name, cycleRecord)
            }
        }
    }

    fun deleteCycleRecord(cycleId: String) {
        viewModelScope.launch {
            database.cycleRecordDao().deleteCycleRecord(cycleId)
            val name = userName.value
            if (name.isNotBlank()) {
                firestoreManager.deleteCycleRecord(name, cycleId)
            }
        }
    }

    val averageCycleLength: StateFlow<Int> = prefsManager.averageCycleLength
    val appTheme: StateFlow<String> = prefsManager.appTheme
    val themeMode: StateFlow<String> = prefsManager.themeMode
    val appFont: StateFlow<String> = prefsManager.appFont

    private val _aiDailyTip = MutableStateFlow<String?>(null)
    val aiDailyTip: StateFlow<String?> = _aiDailyTip.asStateFlow()

    private val _isFetchingAiTip = MutableStateFlow(false)
    val isFetchingAiTip: StateFlow<Boolean> = _isFetchingAiTip.asStateFlow()

    fun fetchAiDailyTip(phase: String, currentDay: Int, avgCycleLength: Float) {
        if (_aiDailyTip.value != null || _isFetchingAiTip.value) return // Don't fetch if we already have it or are fetching
        _isFetchingAiTip.value = true
        viewModelScope.launch {
            val tip = com.example.api.generateCycleTips(phase, currentDay, avgCycleLength)
            _aiDailyTip.value = tip
            _isFetchingAiTip.value = false
        }
    }

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
        predictionInfo
    ) { periodLogs, predInfo ->
        val cycleLength = predInfo.calculatedAverage
        if (periodLogs.isEmpty() || predInfo.lastPeriodStart == null) {
            CycleInfo(
                currentDay = 1,
                daysUntilNextPeriod = cycleLength,
                predictedPeriodStart = LocalDate.now().plusDays(cycleLength.toLong()),
                isPeriod = false,
                isFertile = false,
                isPms = false
            )
        } else {
            val today = LocalDate.now()
            var daysSincePeriodStart = ChronoUnit.DAYS.between(predInfo.lastPeriodStart, today).toInt() + 1
            if (daysSincePeriodStart < 1) daysSincePeriodStart = 1 
            else if (daysSincePeriodStart > cycleLength) {
                daysSincePeriodStart = (daysSincePeriodStart - 1) % cycleLength + 1
            }

            val daysUntilNext = cycleLength - daysSincePeriodStart
            val nextPeriod = today.plusDays(daysUntilNext.toLong())
            
            val todayStr = today.toString()
            val isActualPeriodDay = periodLogs.any { it.dateStr == todayStr }
            val isPeriod = isActualPeriodDay || (daysSincePeriodStart <= 5 && daysSincePeriodStart > 0)
            
            val isFertile = daysSincePeriodStart in (cycleLength / 2 - 2)..(cycleLength / 2 + 2)
            val isPms = daysSincePeriodStart in (cycleLength - 5)..cycleLength && !isPeriod && !isFertile

            CycleInfo(
                currentDay = daysSincePeriodStart,
                daysUntilNextPeriod = daysUntilNext,
                predictedPeriodStart = nextPeriod,
                isPeriod = isPeriod,
                isFertile = isFertile,
                isPms = isPms
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

        // Periodically monitor cycleInfo and preferences to schedule intelligent notifications
        viewModelScope.launch {
            combine(
                predictionInfo,
                cycleInfo,
                isNotificationEnabled,
                isOvulationRemindersEnabled,
                daysBefore,
                ovulationDaysBefore,
                notificationHour,
                notificationMinute
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                val pred = values[0] as PredictionInfo
                val cycle = values[1] as CycleInfo
                val pEnabled = values[2] as Boolean
                val oEnabled = values[3] as Boolean
                val pDays = values[4] as Int
                val oDays = values[5] as Int
                val h = values[6] as Int
                val m = values[7] as Int

                val lastStart = pred.lastPeriodStart
                    ?: cycle.predictedPeriodStart?.minusDays(pred.calculatedAverage.toLong())
                    ?: LocalDate.now().minusDays(14)
                val cLength = pred.calculatedAverage

                CycleNotificationHelper.scheduleAllReminders(
                    context = getApplication(),
                    lastPeriodStart = lastStart,
                    cycleLength = cLength,
                    isPeriodEnabled = pEnabled,
                    daysBeforePeriod = pDays,
                    isOvulationEnabled = oEnabled,
                    daysBeforeOvulation = oDays,
                    hour = h,
                    minute = m
                )
            }.collect()
        }

        viewModelScope.launch {
            combine(predictionInfo, flowNotifEnabled) { prediction, notifEnabled ->
                Pair(prediction, notifEnabled)
            }.collect { (prediction, notifEnabled) ->
                if (notifEnabled && prediction.lastPeriodStart != null) {
                    val cycleLength = prediction.calculatedAverage
                    val lastStart = prediction.lastPeriodStart
                    val ovulationDay = cycleLength - 14

                    // Calculate phase start dates based on last start
                    val follicularStart = lastStart.plusDays(5)
                    val ovulationStart = lastStart.plusDays((ovulationDay - 2).toLong())
                    val lutealStart = lastStart.plusDays((ovulationDay + 2).toLong())
                    val nextPeriodStart = lastStart.plusDays(cycleLength.toLong())

                    val context = getApplication<Application>()
                    com.example.utils.PhaseReminderHelper.schedulePhaseReminder(
                        context, follicularStart, "فاز فولیکولی", "انرژی در حال افزایش است. زمان خوبی برای برنامه‌های جدید و فعالیت است.", 0, cycleLength
                    )
                    com.example.utils.PhaseReminderHelper.schedulePhaseReminder(
                        context, ovulationStart, "فاز تخمک‌گذاری", "اوج انرژی و تمرکز! زمان طلایی برای خلاقیت و ارتباطات.", 1, cycleLength
                    )
                    com.example.utils.PhaseReminderHelper.schedulePhaseReminder(
                        context, lutealStart, "فاز لوتئال (PMS)", "به بدن خود استراحت دهید و به آرامش ذهنی توجه کنید.", 2, cycleLength
                    )
                    com.example.utils.PhaseReminderHelper.schedulePhaseReminder(
                        context, nextPeriodStart, "فاز قاعدگی", "زمان استراحت و بازتوانی است. مصرف مایعات گرم را فراموش نکنید.", 3, cycleLength
                    )
                } else {
                    com.example.utils.PhaseReminderHelper.cancelAllPhaseReminders(getApplication())
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
        CycleNotificationHelper.cancelAll(getApplication())
    }

    fun setNotificationEnabled(enabled: Boolean) {
        prefsManager.setNotificationEnabled(enabled)
    }

    fun setAiRemindersEnabled(enabled: Boolean) {
        prefsManager.setAiRemindersEnabled(enabled)
    }

    fun setOvulationRemindersEnabled(enabled: Boolean) {
        prefsManager.setOvulationRemindersEnabled(enabled)
    }

    fun setDaysBefore(days: Int) {
        prefsManager.setDaysBefore(days)
    }

    fun setOvulationDaysBefore(days: Int) {
        prefsManager.setOvulationDaysBefore(days)
    }

    fun setNotificationTime(hour: Int, minute: Int) {
        prefsManager.setNotificationTime(hour, minute)
    }

    fun sendTestPeriodNotification() {
        val today = LocalDate.now()
        val cycle = cycleInfo.value
        val nextPeriod = cycle.predictedPeriodStart ?: today.plusDays(averageCycleLength.value.toLong())
        val jalaliDate = JalaliCalendar.gregorianToJalali(nextPeriod).formatShort()
        val days = daysBefore.value
        CycleNotificationHelper.sendImmediateNotification(
            context = getApplication(),
            isOvulation = false,
            customTitle = "یادآور هوشمند شروع قاعدگی 🌸",
            customBody = "دوره قاعدگی بعدی شما برای تاریخ $jalaliDate پیش‌بینی شده است. ملزومات بهداشتی را همراه داشته باشید و به استراحت و مصرف مایعات گرم توجه فرمایید.",
            dateJalali = jalaliDate,
            daysLeft = days
        )
    }

    fun sendTestOvulationNotification() {
        val today = LocalDate.now()
        val cycle = cycleInfo.value
        val cycleLength = averageCycleLength.value
        val nextPeriod = cycle.predictedPeriodStart ?: today.plusDays(cycleLength.toLong())
        val ovulationDate = nextPeriod.minusDays(14)
        val jalaliOvulation = JalaliCalendar.gregorianToJalali(ovulationDate).formatShort()
        val days = ovulationDaysBefore.value
        CycleNotificationHelper.sendImmediateNotification(
            context = getApplication(),
            isOvulation = true,
            customTitle = "یادآور تخمک‌گذاری و پنجره باروری 🥚✨",
            customBody = "امروز وارد پنجره طلایی تخمک‌گذاری و بالاترین احتمال باروری شدید (تاریخ تخمینی: $jalaliOvulation). سطح انرژی، شادابی و انگیزه بدن در اوج است!",
            dateJalali = jalaliOvulation,
            daysLeft = days
        )
    }

    fun setAverageCycleLength(length: Int) {
        prefsManager.setAverageCycleLength(length)
    }

    fun setAppTheme(theme: String) {
        prefsManager.setAppTheme(theme)
    }

    fun setThemeMode(mode: String) {
        prefsManager.setThemeMode(mode)
    }

    fun toggleThemeMode() {
        val current = prefsManager.themeMode.value
        val next = when (current) {
            "LIGHT" -> "DARK"
            "DARK" -> "SYSTEM"
            else -> "LIGHT"
        }
        prefsManager.setThemeMode(next)
    }

    fun setAppFont(font: String) {
        prefsManager.setAppFont(font)
    }

    fun setUserAvatar(avatar: String) {
        prefsManager.setUserAvatar(avatar)
    }

    fun setWeight(weight: String) {
        viewModelScope.launch {
            prefsManager.setUserWeight(weight)
            val uName = userName.value
            if (uName.isNotBlank()) {
                firestoreManager.uploadWeight(uName, weight)
            }
        }
    }

    fun updateCycleSettings(startDate: String, duration: Int) {
        viewModelScope.launch {
            prefsManager.setLastPeriodDate(startDate)
            prefsManager.setAverageCycleLength(duration)
            
            if (startDate.isNotBlank()) {
                val log = DailyLog(
                    dateStr = startDate,
                    periodFlow = "MEDIUM",
                    symptoms = "",
                    moods = "",
                    notes = "تغییر تاریخ شروع سیکل قاعدگی"
                )
                repository.insertLog(log)
                
                val uName = userName.value
                if (uName.isNotBlank()) {
                    firestoreManager.uploadLog(uName, log)
                    firestoreManager.uploadCycleData(uName, startDate, duration)
                }
            } else {
                val uName = userName.value
                if (uName.isNotBlank()) {
                    firestoreManager.uploadCycleData(uName, "", duration)
                }
            }
        }
    }

    fun savePeriodRange(startDate: LocalDate, endDate: LocalDate, flow: String = "MEDIUM", notes: String = "") {
        viewModelScope.launch {
            val start = if (startDate.isAfter(endDate)) endDate else startDate
            val end = if (startDate.isAfter(endDate)) startDate else endDate

            val recordId = "cycle_${start}"
            val record = com.example.data.model.CycleRecord(
                id = recordId,
                startDate = start.toString(),
                endDate = end.toString(),
                notes = notes.ifBlank { "ثبت شده از طریق تقویم تعاملی هانیتا" }
            )
            database.cycleRecordDao().insertCycleRecord(record)

            var curr = start
            while (!curr.isAfter(end)) {
                val dateStr = curr.toString()
                val existing = repository.getLogByDate(dateStr)
                val updated = existing?.copy(
                    periodFlow = flow,
                    notes = if (existing.notes.isBlank()) notes else existing.notes
                ) ?: com.example.data.model.DailyLog(
                    dateStr = dateStr,
                    periodFlow = flow,
                    symptoms = "",
                    moods = "",
                    notes = notes
                )
                repository.insertLog(updated)
                curr = curr.plusDays(1)
            }

            val avgCycle = averageCycleLength.value
            prefsManager.setLastPeriodDate(start.toString())

            val uName = userName.value
            if (uName.isNotBlank()) {
                firestoreManager.uploadCycleRecord(uName, record)
                firestoreManager.uploadCycleData(uName, start.toString(), avgCycle)
            }
        }
    }

    fun clearPeriodRange(startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            val start = if (startDate.isAfter(endDate)) endDate else startDate
            val end = if (startDate.isAfter(endDate)) startDate else endDate

            database.cycleRecordDao().deleteCycleRecord("cycle_${start}")

            var curr = start
            while (!curr.isAfter(end)) {
                val dateStr = curr.toString()
                val existing = repository.getLogByDate(dateStr)
                if (existing != null) {
                    repository.insertLog(existing.copy(periodFlow = "NONE"))
                }
                curr = curr.plusDays(1)
            }
        }
    }

    fun updateFullCycleRecord(
        oldRecordId: String?,
        oldStartDate: LocalDate?,
        oldEndDate: LocalDate?,
        newStartDate: LocalDate,
        newEndDate: LocalDate,
        cycleLength: Int,
        flow: String,
        notes: String
    ) {
        viewModelScope.launch {
            var actualOldRecordId = oldRecordId
            var actualOldEndDate = oldEndDate

            if (oldStartDate != null && oldEndDate == null) {
                val existingRecord = database.cycleRecordDao().getCycleRecordById(oldRecordId ?: "cycle_${oldStartDate}")
                if (existingRecord != null) {
                    actualOldRecordId = existingRecord.id
                    actualOldEndDate = runCatching { LocalDate.parse(existingRecord.endDate) }.getOrNull()
                } else {
                    actualOldEndDate = oldStartDate.plusDays(7)
                }
            }

            if (oldStartDate != null && actualOldEndDate != null && (oldStartDate != newStartDate || actualOldEndDate != newEndDate)) {
                if (actualOldRecordId != null && actualOldRecordId != "cycle_${newStartDate}") {
                    database.cycleRecordDao().deleteCycleRecord(actualOldRecordId)
                    val name = userName.value
                    if (name.isNotBlank()) {
                        firestoreManager.deleteCycleRecord(name, actualOldRecordId)
                    }
                }
                var oldCurr: LocalDate = oldStartDate
                val safeOldEnd: LocalDate = actualOldEndDate
                while (!oldCurr.isAfter(safeOldEnd)) {
                    if (oldCurr.isBefore(newStartDate) || oldCurr.isAfter(newEndDate)) {
                        val dateStr = oldCurr.toString()
                        val existing = repository.getLogByDate(dateStr)
                        if (existing != null && existing.periodFlow != "NONE") {
                            repository.insertLog(existing.copy(periodFlow = "NONE"))
                        }
                    }
                    oldCurr = oldCurr.plusDays(1)
                }
            }

            savePeriodRange(newStartDate, newEndDate, flow, notes)

            if (cycleLength in 21..45) {
                prefsManager.setAverageCycleLength(cycleLength)
            }
        }
    }

    fun deleteFullCycleRecord(record: com.example.data.model.CycleRecord) {
        viewModelScope.launch {
            deleteCycleRecord(record.id)
            val sDate = runCatching { LocalDate.parse(record.startDate) }.getOrNull()
            val eDate = runCatching { LocalDate.parse(record.endDate) }.getOrNull() ?: sDate
            if (sDate != null && eDate != null) {
                clearPeriodRange(sDate, eDate)
            }
        }
    }

    fun updateMedicationReminder(updated: MedicineReminder) {
        val currentList = medicationReminders.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            currentList[index] = updated
        } else {
            currentList.add(updated)
        }
        saveMedicationReminders(currentList)
    }

    fun saveMedicationReminders(reminders: List<MedicineReminder>) {
        prefsManager.saveMedicationReminders(reminders)
        com.example.utils.MedicineReminderHelper.scheduleReminders(getApplication(), reminders)
    }

    fun addCustomPhysicalSymptom(symptom: String) {
        prefsManager.addCustomPhysicalSymptom(symptom)
    }
    fun removeCustomPhysicalSymptom(symptom: String) {
        prefsManager.removeCustomPhysicalSymptom(symptom)
    }
    fun addCustomMoodSymptom(symptom: String) {
        prefsManager.addCustomMoodSymptom(symptom)
    }
    fun removeCustomMoodSymptom(symptom: String) {
        prefsManager.removeCustomMoodSymptom(symptom)
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

    private val _analysisState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Idle)
    val analysisState: StateFlow<AnalysisUiState> = _analysisState.asStateFlow()

    fun loadCachedAnalysis() {
        val cachedReport = prefsManager.getCycleAnalysisReport()
        if (cachedReport.isNotBlank()) {
            try {
                val moshiAdapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                    .adapter(CycleAnalysisResult::class.java)
                val parsed = moshiAdapter.fromJson(cachedReport)
                if (parsed != null) {
                    _analysisState.value = AnalysisUiState.Success(parsed)
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun getPersonalizedAiReminderMessage(): Pair<String, String> {
        val cachedReport = prefsManager.getCycleAnalysisReport()
        if (cachedReport.isNotBlank()) {
            try {
                val moshiAdapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                    .adapter(CycleAnalysisResult::class.java)
                val parsed = moshiAdapter.fromJson(cachedReport)
                if (parsed != null && parsed.predictedNextStartJalali.isNotBlank()) {
                    val title = "تحلیل خودمراقبتی هوشمند هانیتا 🌸"
                    val nutritionTip = if (parsed.nutritionRecommend.isNotBlank()) {
                        "توصیه تغذیه: " + parsed.nutritionRecommend.take(100) + "..."
                    } else "مایعات کافی بنوشید و فیبر را به وعده‌های خود بیفزایید."
                    val body = "شروع دوره بعدی حدود ${parsed.predictedNextStartJalali} پیش‌بینی شده است. $nutritionTip"
                    return Pair(title, body)
                }
            } catch (e: Exception) {
                // fallback
            }
        }
        val title = "یادآور هوش مصنوعی هانیتا ✨"
        val body = "طبق محاسبات سلامت، چرخه طبیعی شما دقیق و تحت کنترل است. مصرف مایعات و رژیم غذایی سالم فراموش نشود."
        return Pair(title, body)
    }

    fun getOvulationReminderMessage(): Pair<String, String> {
        val cachedReport = prefsManager.getCycleAnalysisReport()
        if (cachedReport.isNotBlank()) {
            try {
                val moshiAdapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                    .adapter(CycleAnalysisResult::class.java)
                val parsed = moshiAdapter.fromJson(cachedReport)
                if (parsed != null && parsed.exerciseRecommend.isNotBlank()) {
                    val title = "زنگ هوشمند باروری و تخمک‌گذاری 🌸"
                    val sportTip = "ورزش توصیه شده: " + parsed.exerciseRecommend.take(100) + "..."
                    val body = "شما اکنون در پنجره طلایی تخمک‌گذاری و زمان طلایی انرژی قرار دارید! $sportTip"
                    return Pair(title, body)
                }
            } catch (e: Exception) {
                // fallback
            }
        }
        val title = "زمان تخمک‌گذاری و اوج باروری 🌸"
        val body = "امروز روزهای طلایی و متمایز چرخه شماست. شادابی و انگیزه خود را با ورزش سبک و مصرف سبزیجات تازه افزایش دهید."
        return Pair(title, body)
    }

    fun analyzeCycleWithGemini() {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
            _analysisState.value = AnalysisUiState.Error("کلید لایسنس Gemini در بخش تنظیمات هانیتا وارد نشده است. لطفاً برای دریافت تحلیل، ابتدا کلید فعال‌سازی معتبر را ثبت نمایید.")
            return
        }

        viewModelScope.launch {
            _analysisState.value = AnalysisUiState.Loading
            try {
                val localLogs = allLogs.value
                val username = userName.value.ifBlank { "کاربر عزیز" }
                val startPeriod = lastPeriodDate.value
                val cycleL = averageCycleLength.value

                val logsSummaryText = if (localLogs.isEmpty()) {
                    "سابقه ثبت روزانه علائم فعال: ندارد. اطلاعات تکی ورودی اولیه: تاریخ شروع آخرین پریود: $startPeriod و فواصل فرضی معمول چرخه: $cycleL روز است."
                } else {
                    localLogs.take(25).joinToString("\n") { log ->
                        "تاریخ: ${log.dateStr} | سطح خونریزی: ${log.periodFlow} | علائم بدنی: ${log.symptoms} | نوسانات روحی: ${log.moods} | یادداشت کاربر: ${log.notes}"
                    }
                }

                val prompt = """
                    لطفا تمام مشخصات و سابقه فعالیت ثبت شده دوره زیر مربوط به اپلیکیشن قاعدگی هانیتا را بررسی نموده و یک تحلیل سلامت کاملا حرفه‌ای و شخصی‌سازی شده به زبان فارسی تهیه کنید:
                    نام کاربر محترم: $username
                    اطلاعات سوابق و کارهای ثبت شده:
                    $logsSummaryText

                    وظایفه شما:
                    یک گزارش تحلیلی فوق‌العاده کاربردی، با حس بسیار گرم، همدردانه و علمی ارائه کنید. در این تحلیل باید حلی برای پیش‌بینی تاریخ چرخه بعدی، تشخیص منظم بودن چرخه، الگوها و عاداتی که بر فعالیت بدنی و سلامت روان تأثیرگذارند به همراه پیشنهاداتی برای تغذیه و ورزش ارائه دهید.

                    مهم: خروجی شما باید دقیقاً و بدون هیچ توضیح اضافه یا نشانه‌گذاری با فرمت ```json، یک رشته معتبر JSON با این فیلدها باشد:
                    {
                      "predictedNextStartJalali": "تاریخ تخمینی دوره بعدی به تقویم شمسی (مثال: ۱۵ تیر ۱۴۰۵)",
                      "predictionReasoning": "تحلیل علمی محاسباتی و زمان‌بندی علت انتخاب این تاریخ به فارسی",
                      "regularityStatus": "تحلیل کیفیت و فواصل منظم بودن چرخه بر اساس داده‌ها",
                      "commonPatterns": ["فهرست الگوها و علائم تکرارشونده و نوسانات خلقی شناسایی شده (الگوی ۱)", "فهرست الگوها و علائم تکرارشونده دیگر (الگوی ۲)"],
                      "nutritionRecommend": "توصیه‌های کاربردی و عملی تغذیه مناسب برای این کاربر بر اساس علائم",
                      "exerciseRecommend": "فعالیت‌های بدنی و ورزشی ویژه تسکین‌دهنده و سلامت برای فازهای مختلف",
                      "mentalRecommend": "توصیه‌های ویژه مهار استرس، مدیریت PMS و سلامت روانی هانیتا",
                      "overallSummary": "جمله مثبت اندیشانه و محبت‌آمیز پایان‌بخش هانیتا"
                    }
                """.trimIndent()

                val apiRequest = GemAnalysisRequest(
                    contents = listOf(
                        GemAnalysisContent(
                            role = "user",
                            parts = listOf(GemAnalysisPart(text = prompt))
                        )
                    ),
                    systemInstruction = GemAnalysisContent(
                        role = "system",
                        parts = listOf(GemAnalysisPart(text = "You are a professional, highly knowledgeable and empathetic womens gynecologist AI named هانیتا (Hanita). Always return strictly a JSON format response in Persian language."))
                    )
                )

                val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    GeminiAnalysisClient.service.generateContent(apiKey, apiRequest)
                }

                val botRawJson = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (botRawJson != null) {
                    val cleanJson = botRawJson.trim()
                        .removePrefix("```json")
                        .removeSuffix("```")
                        .trim()

                    val moshiAdapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                        .adapter(CycleAnalysisResult::class.java)
                    val parsedResult = moshiAdapter.fromJson(cleanJson)

                    if (parsedResult != null) {
                        prefsManager.saveCycleAnalysisReport(cleanJson)
                        _analysisState.value = AnalysisUiState.Success(parsedResult)
                    } else {
                        _analysisState.value = AnalysisUiState.Error("پاسخ دریافتی مغایرت قالبی دارد. لطفاً دوباره امتحان کنید.")
                    }
                } else {
                    _analysisState.value = AnalysisUiState.Error("پاسخی از سرور تحلیل هانیتا دریافت نشد.")
                }
            } catch (e: Exception) {
                _analysisState.value = AnalysisUiState.Error("ناموفق در برقراری ارتباط با پلتفرم سلامت: ${e.localizedMessage}")
            }
        }
    }

    suspend fun generateAvatarImageWithAI(prompt: String, resolution: String): String? {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                
                val contents = listOf(
                    GemAnalysisContent(role = "user", parts = listOf(GemAnalysisPart(text = "Generate a portrait fantasy avatar. Style and details: $prompt")))
                )
                
                val request = GemAnalysisRequest(
                    contents = contents,
                    generationConfig = GemGenerationConfig(
                        imageConfig = GemImageConfig(aspectRatio = "1:1", imageSize = resolution),
                        responseModalities = listOf("TEXT", "IMAGE")
                    )
                )
                
                val response = GeminiAnalysisClient.service.generateImage(apiKey, request)
                val base64Data = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull { it.inlineData != null }?.inlineData?.data
                
                base64Data
            } catch (e: Exception) {
                android.util.Log.e("MahitaViewModel", "Error generating image: ${e.message}")
                null
            }
        }
    }

    suspend fun createOrEditImageFlash(prompt: String, resolution: String): String? {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                val request = GemAnalysisRequest(
                    contents = listOf(GemAnalysisContent(role = "user", parts = listOf(GemAnalysisPart(text = "Modify or create image: $prompt")))),
                    generationConfig = GemGenerationConfig(
                        imageConfig = GemImageConfig(imageSize = resolution),
                        responseModalities = listOf("IMAGE")
                    )
                )
                val response = GeminiAnalysisClient.service.generateImageEdit(apiKey, request)
                response.candidates?.firstOrNull()?.content?.parts?.firstOrNull { it.inlineData != null }?.inlineData?.data
            } catch (e: Exception) {
                android.util.Log.e("MahitaViewModel", "Error in createOrEditImageFlash: ${e.message}")
                null
            }
        }
    }

    enum class CyclePhase {
        MENSTRUAL,
        FOLLICULAR,
        OVULATION,
        LUTEAL
    }

    /**
     * Helper utility that calculates the predicted cycle phase for a given date
     * based on user-logged historical data (calculated average and last period start).
     */
    fun predictCyclePhase(targetDate: LocalDate = LocalDate.now()): CyclePhase {
        val prediction = predictionInfo.value
        val cycleLength = prediction.calculatedAverage
        val lastStart = prediction.lastPeriodStart ?: LocalDate.now()

        // Calculate days since the start of the current or most recent cycle
        var daysSinceStart = ChronoUnit.DAYS.between(lastStart, targetDate).toInt() + 1
        
        if (daysSinceStart < 1) {
             daysSinceStart = (daysSinceStart % cycleLength + cycleLength) % cycleLength
             if (daysSinceStart == 0) daysSinceStart = cycleLength
        } else if (daysSinceStart > cycleLength) {
             daysSinceStart = ((daysSinceStart - 1) % cycleLength) + 1
        }

        // Luteal phase is consistently ~14 days before the next period
        val ovulationDay = cycleLength - 14
        
        return when {
            daysSinceStart <= 5 -> CyclePhase.MENSTRUAL
            daysSinceStart < ovulationDay - 2 -> CyclePhase.FOLLICULAR
            daysSinceStart in (ovulationDay - 2)..(ovulationDay + 1) -> CyclePhase.OVULATION
            else -> CyclePhase.LUTEAL
        }
    }

    suspend fun generateMusicClip(prompt: String): String? {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                val request = GemAnalysisRequest(
                    contents = listOf(GemAnalysisContent(role = "user", parts = listOf(GemAnalysisPart(text = "Compose music: $prompt"))))
                )
                val response = GeminiAnalysisClient.service.generateMusic(apiKey, request)
                response.candidates?.firstOrNull()?.content?.parts?.firstOrNull { it.inlineData != null }?.inlineData?.data
            } catch (e: Exception) {
                android.util.Log.e("MahitaViewModel", "Error generating music: ${e.message}")
                null
            }
        }
    }
    fun toggleMedicationTaken(dateStr: String, medId: String, taken: Boolean) {
        viewModelScope.launch {
            val existingLog = allLogs.value.find { it.dateStr == dateStr }
            val currentMeds = existingLog?.takenMeds?.split(",")?.filter { it.isNotBlank() }?.toMutableList() ?: mutableListOf()
            if (taken) {
                if (!currentMeds.contains(medId)) currentMeds.add(medId)
            } else {
                currentMeds.remove(medId)
            }
            val newMedsStr = currentMeds.joinToString(",")
            
            val newLog = existingLog?.copy(takenMeds = newMedsStr) ?: com.example.data.model.DailyLog(
                dateStr = dateStr,
                periodFlow = "NONE",
                symptoms = "",
                moods = "",
                notes = "",
                emoji = "",
                takenMeds = newMedsStr
            )
            repository.insertLog(newLog)
        }
    }
    


    fun savePhysicalSymptomForm(
        dateStr: String,
        symptoms: List<com.example.data.model.PhysicalSymptomLog>,
        flow: String = "NONE",
        moods: List<String> = emptyList(),
        overallNotes: String = "",
        emoji: String = "",
        waterIntake: Int = 0,
        bbt: Float? = null,
        weight: Float? = null
    ) {
        viewModelScope.launch {
            // 1. Delete previous physical symptoms for this date in Room to replace with updated state
            repository.deletePhysicalSymptomsForDate(dateStr)
            // 2. Insert new physical symptoms in Room database
            if (symptoms.isNotEmpty()) {
                repository.insertPhysicalSymptoms(symptoms)
            }
            // 3. Update DailyLog in Room for backwards compatibility with calendar & analytics
            val existing = repository.getLogByDate(dateStr)
            val symptomNames = symptoms.map { it.symptomName }.distinct().joinToString(",")
            val log = DailyLog(
                dateStr = dateStr,
                periodFlow = if (flow.isNotBlank()) flow else (existing?.periodFlow ?: "NONE"),
                symptoms = symptomNames,
                moods = if (moods.isNotEmpty()) moods.joinToString(",") else (existing?.moods ?: ""),
                notes = if (overallNotes.isNotBlank()) overallNotes else (existing?.notes ?: ""),
                emoji = if (emoji.isNotBlank()) emoji else (existing?.emoji ?: ""),
                takenMeds = existing?.takenMeds ?: "",
                waterIntake = if (waterIntake != 0) waterIntake else (existing?.waterIntake ?: 0),
                bbt = bbt ?: existing?.bbt,
                weight = weight ?: existing?.weight
            )
            repository.insertLog(log)

            // 4. Upload to Firestore if logged in
            val name = userName.value
            if (name.isNotBlank()) {
                try {
                    firestoreManager.uploadLog(name, log)
                } catch (e: Exception) {
                    android.util.Log.e("MahitaViewModel", "Error uploading log to firestore: ${e.message}")
                }
            }
        }
    }

    fun deletePhysicalSymptomById(id: Long, dateStr: String? = null) {
        viewModelScope.launch {
            repository.deletePhysicalSymptomById(id)
            if (dateStr != null) {
                // Refresh symptoms in DailyLog
                val remaining = repository.getPhysicalSymptomsListForDate(dateStr)
                val remainingNames = remaining.map { it.symptomName }.distinct().joinToString(",")
                val existing = repository.getLogByDate(dateStr)
                if (existing != null) {
                    val updated = existing.copy(symptoms = remainingNames)
                    repository.insertLog(updated)
                }
            }
        }
    }

    fun saveQuickLog(dateStr: String, mood: String, energy: String, sleep: String, pain: String) {
        viewModelScope.launch {
            val existing = repository.getLogByDate(dateStr)
            
            // Collect new moods/symptoms
            val currentMoods = existing?.moods?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }?.toMutableSet() ?: mutableSetOf()
            if (mood.isNotBlank()) currentMoods.add(mood)
            if (energy.isNotBlank()) currentMoods.add("انرژی: $energy")
            if (sleep.isNotBlank()) currentMoods.add("خواب: $sleep")
            
            val currentSymptoms = existing?.symptoms?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }?.toMutableSet() ?: mutableSetOf()
            if (pain.isNotBlank() && pain != "ندارم") {
                currentSymptoms.add(pain)
                // Also add to physical logs
                repository.insertPhysicalSymptoms(listOf(
                    com.example.data.model.PhysicalSymptomLog(
                        dateStr = dateStr,
                        symptomName = pain,
                        severity = "MODERATE",
                        severityScore = 2,
                        category = "ثبت سریع"
                    )
                ))
            }
            
            val log = DailyLog(
                dateStr = dateStr,
                periodFlow = existing?.periodFlow ?: "NONE",
                symptoms = currentSymptoms.joinToString(","),
                moods = currentMoods.joinToString(","),
                notes = existing?.notes ?: "",
                emoji = if (mood.isNotBlank()) getEmojiForMood(mood) else (existing?.emoji ?: ""),
                takenMeds = existing?.takenMeds ?: "",
                waterIntake = existing?.waterIntake ?: 0,
                bbt = existing?.bbt,
                weight = existing?.weight
            )
            repository.insertLog(log)
        }
    }

    private fun getEmojiForMood(mood: String): String {
        return when (mood) {
            "عالی" -> "😄"
            "خوب" -> "🙂"
            "معمولی" -> "😐"
            "غمگین" -> "😢"
            "عصبی" -> "😠"
            else -> ""
        }
    }

    fun saveLog(dateStr: String, flow: String, symptoms: List<String>, moods: List<String>, notes: String, emoji: String = "", takenMeds: String = "") {

        viewModelScope.launch {
            val existing = repository.getLogByDate(dateStr)
            val log = DailyLog(
                dateStr = dateStr,
                periodFlow = flow,
                symptoms = symptoms.joinToString(","),
                moods = moods.joinToString(","),
                notes = notes,
                emoji = emoji,
                takenMeds = takenMeds,
                waterIntake = existing?.waterIntake ?: 0
            )
            repository.insertLog(log)

            // Also synchronize with physical_symptom_logs table in Room
            if (symptoms.isNotEmpty()) {
                repository.deletePhysicalSymptomsForDate(dateStr)
                val physicalLogs = symptoms.map { symName ->
                    val category = when {
                        symName in listOf("سردرد", "خستگی", "سرگیجه", "بی‌خوابی", "عدم تمرکز") -> "سر و اعصاب"
                        symName in listOf("درد شکم", "کمردرد", "نفخ", "حساسیت سینه") -> "شکم و لگن"
                        symName in listOf("حالت تهوع", "هوس غذایی", "بی‌اشتهایی") -> "گوارش و متابولیسم"
                        else -> "عضلات و فیزیک"
                    }
                    com.example.data.model.PhysicalSymptomLog(
                        dateStr = dateStr,
                        symptomName = symName,
                        severity = "MODERATE",
                        severityScore = 2,
                        category = category,
                        notes = notes
                    )
                }
                repository.insertPhysicalSymptoms(physicalLogs)
            }
            
            // Sync this single log immediately to Firestore
            val name = userName.value
            if (name.isNotBlank()) {
                firestoreManager.uploadLog(name, log)
            }
        }
    }

    fun setWaterGlasses(count: Int) {
        viewModelScope.launch {
            val todayStr = LocalDate.now().toString()
            val existing = repository.getLogByDate(todayStr)
            val newLog = existing?.copy(waterIntake = count) ?: DailyLog(
                dateStr = todayStr,
                periodFlow = "NONE",
                symptoms = "",
                moods = "",
                notes = "",
                waterIntake = count
            )
            repository.insertLog(newLog)
        }
    }
}

data class CycleAnalysisResult(
    val predictedNextStartJalali: String = "",
    val predictionReasoning: String = "",
    val regularityStatus: String = "",
    val commonPatterns: List<String> = emptyList(),
    val nutritionRecommend: String = "",
    val exerciseRecommend: String = "",
    val mentalRecommend: String = "",
    val overallSummary: String = ""
)

sealed interface AnalysisUiState {
    object Idle : AnalysisUiState
    object Loading : AnalysisUiState
    data class Success(val result: CycleAnalysisResult) : AnalysisUiState
    data class Error(val message: String) : AnalysisUiState
    

}

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class GemInlineData(val mimeType: String, val data: String)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class GemAnalysisPart(val text: String? = null, val inlineData: GemInlineData? = null)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class GemAnalysisContent(val role: String?, val parts: List<GemAnalysisPart>)

@com.squareup.moshi.JsonClass(generateAdapter = true)
class GemGoogleSearchTool

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class GemTool(val googleSearch: GemGoogleSearchTool? = null)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class GemThinkingConfig(val thinkingLevel: String)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class GemImageConfig(val aspectRatio: String? = null, val imageSize: String? = null)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class GemGenerationConfig(
    val imageConfig: GemImageConfig? = null,
    val responseModalities: List<String>? = null,
    val thinkingConfig: GemThinkingConfig? = null
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class GemAnalysisRequest(
    val contents: List<GemAnalysisContent>,
    val systemInstruction: GemAnalysisContent? = null,
    val tools: List<GemTool>? = null,
    val generationConfig: GemGenerationConfig? = null
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class GemAnalysisResponse(val candidates: List<GemAnalysisCandidate>?)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class GemAnalysisCandidate(val content: GemAnalysisContent?)

interface GeminiAnalysisService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GemAnalysisRequest
    ): GemAnalysisResponse

    @POST("v1beta/models/gemini-3.1-pro-preview:generateContent")
    suspend fun generateContentPro(
        @Query("key") apiKey: String,
        @Body request: GemAnalysisRequest
    ): GemAnalysisResponse

    @POST("v1beta/models/lyria-3-clip-preview:generateContent")
    suspend fun generateMusic(
        @Query("key") apiKey: String,
        @Body request: GemAnalysisRequest
    ): GemAnalysisResponse
    
    @POST("v1beta/models/gemini-3.1-flash-image-preview:generateContent")
    suspend fun generateImageEdit(
        @Query("key") apiKey: String,
        @Body request: GemAnalysisRequest
    ): GemAnalysisResponse

    @POST("v1beta/models/gemini-3-pro-image-preview:generateContent")
    suspend fun generateImage(
        @Query("key") apiKey: String,
        @Body request: GemAnalysisRequest
    ): GemAnalysisResponse
    

}

object GeminiAnalysisClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = okhttp3.OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiAnalysisService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiAnalysisService::class.java)
    }
    

}

data class CycleInfo(
    val currentDay: Int = 1,
    val daysUntilNextPeriod: Int = 28,
    val predictedPeriodStart: LocalDate = LocalDate.now().plusDays(28),
    val isPeriod: Boolean = false,
    val isFertile: Boolean = false,
    val isPms: Boolean = false
)

data class PredictionInfo(
    val hasEnoughData: Boolean = false,
    val calculatedAverage: Int = 28,
    val lastPeriodStart: LocalDate? = null,
    val predictedNextStart: LocalDate? = null,
    val daysRemaining: Int = 0,
    val totalCyclesCalculated: Int = 0
)


