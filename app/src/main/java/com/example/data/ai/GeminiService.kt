package com.example.data.ai

import com.example.BuildConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null
)

@Serializable
data class GenerationConfig(
    val temperature: Float? = null,
    val thinkingConfig: ThinkingConfig? = null
)

@Serializable
data class ThinkingConfig(
    val thinkingLevel: String
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate>
)

@Serializable
data class Candidate(
    val content: Content
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
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

class GeminiService {
    suspend fun analyzeFinances(transactionsSummary: String, userQuestion: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Gemini API key is not configured. Please add it to the Secrets panel."
        }
        
        val prompt = "Here is my financial summary:\n$transactionsSummary\n\nQuestion: $userQuestion"
        
        val request = GenerateContentRequest(
            contents = listOf(Content(
                parts = listOf(Part(text = prompt))
            )),
            generationConfig = GenerationConfig(
                thinkingConfig = ThinkingConfig(thinkingLevel = "HIGH")
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are a financial advisor for Hanif Hisab Khata (হিসাব খাতা). Provide clear, concise, and helpful advice. You can speak in English or Bangla depending on the user's question."))
            )
        )
        
        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No advice generated."
        } catch (e: Exception) {
            "Error analyzing finances: ${e.message}"
        }
    }
}
