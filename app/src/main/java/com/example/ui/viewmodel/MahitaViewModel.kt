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
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

class MahitaViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = LogRepository(database.dailyLogDao())
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
    val isLoggedIn: StateFlow<Boolean> = prefsManager.isLoggedIn
    val isNotificationEnabled: StateFlow<Boolean> = prefsManager.isNotificationEnabled
    val isAiRemindersEnabled: StateFlow<Boolean> = prefsManager.isAiRemindersEnabled
    val isOvulationRemindersEnabled: StateFlow<Boolean> = prefsManager.isOvulationRemindersEnabled
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
                isFertile = false,
                isPms = false
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

    fun setAiRemindersEnabled(enabled: Boolean) {
        prefsManager.setAiRemindersEnabled(enabled)
    }

    fun setOvulationRemindersEnabled(enabled: Boolean) {
        prefsManager.setOvulationRemindersEnabled(enabled)
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
data class GemImageConfig(val aspectRatio: String? = null, val imageSize: String? = null)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class GemGenerationConfig(
    val imageConfig: GemImageConfig? = null,
    val responseModalities: List<String>? = null
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
