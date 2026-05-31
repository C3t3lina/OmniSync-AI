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

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val role: String? = null,
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val maxOutputTokens: Int? = null,
    val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null,
    val finishReason: String? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
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

object GeminiRepo {
    suspend fun generateResponse(prompt: String, systemPrompt: String? = null, previousChatHistory: List<com.example.data.ChatMessage> = emptyList()): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY") {
            return "Error: Por favor, introduce tu API Key de Gemini en el panel de Secrets de AI Studio. (API Key no configurada correctamente)"
        }

        // Map ChatMessage history to Gemini Content structure, excluding custom status messages
        val mappedContents = mutableListOf<Content>()
        
        // Add chat history if present limit to last 20 messages to prevent token bloat
        val historyToUse = previousChatHistory.takeLast(20)
        for (msg in historyToUse) {
            val roleName = if (msg.role == "user") "user" else "model"
            // Filter out system or error prefix messages to keep chat clean
            if (!msg.content.startsWith("Error:") && !msg.content.startsWith("Aura:")) {
                mappedContents.add(Content(role = roleName, parts = listOf(Part(text = msg.content))))
            }
        }

        // Add current user prompt
        mappedContents.add(Content(role = "user", parts = listOf(Part(text = prompt))))

        val request = GenerateContentRequest(
            contents = mappedContents,
            generationConfig = GenerationConfig(temperature = 0.7f),
            systemInstruction = systemPrompt?.let { Content(parts = listOf(Part(text = it))) }
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "No se ha recibido una respuesta válida o el contenido fue bloqueado."
        } catch (e: Exception) {
            "Error al consultar al agente personal de IA: ${e.message ?: "Conexión interrumpida"}"
        }
    }
}
