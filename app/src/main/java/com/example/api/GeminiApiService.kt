package com.example.api

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.1-pro-preview:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

suspend fun generateCycleTips(phase: String, currentDay: Int, averageCycleLength: Float): String = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY
    if (apiKey.isEmpty() || apiKey == "YOUR_API_KEY_HERE" || apiKey.contains("placeholder")) {
        return@withContext "Gemini API key is not configured. Please add it to your settings or environment variables."
    }

    val prompt = """
        The user is currently on day ${currentDay} of their cycle. Their average cycle length is ${averageCycleLength.toInt()} days.
        They are in the $phase phase.
        Provide 3 short, personalized health and wellness tips specifically tailored to this phase.
        Keep the tone supportive, empathetic, and strictly medically safe. Format the output as a few short bullet points. Do not include a lengthy introduction.
        Please respond in Persian (Farsi) as the app's language is Persian.
    """.trimIndent()

    val request = GenerateContentRequest(
        contents = listOf(
            Content(parts = listOf(Part(text = prompt)))
        ),
        systemInstruction = Content(
            parts = listOf(Part(text = "You are a professional women's health and wellness assistant. Provide short, practical, and empathetic tips based on the menstrual cycle phase. Always reply in Persian."))
        )
    )

    try {
        val response = RetrofitClient.service.generateContent(apiKey, request)
        response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "نکته‌ای برای نمایش یافت نشد."
    } catch (e: Exception) {
        "متأسفانه در دریافت اطلاعات خطایی رخ داد: ${e.localizedMessage}"
    }
}
