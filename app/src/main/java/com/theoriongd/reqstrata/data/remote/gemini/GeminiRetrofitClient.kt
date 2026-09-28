package com.theoriongd.reqstrata.data.remote.gemini

import android.util.Log
import com.theoriongd.reqstrata.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit client to communicate with Gemini API for generating requirement documentation
 * and system design suggestions.
 */
object GeminiRetrofitClient {
    private const val TAG = "GeminiRetrofitClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    const val MODEL_FLASH = "gemini-3.5-flash"
    const val MODEL_PRO = "gemini-3.1-pro-preview"
    const val MODEL_FLASH_LITE = "gemini-3.1-flash-lite-preview"

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiRetrofitService = retrofit.create(GeminiRetrofitService::class.java)

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Throwable) {
            ""
        }
    }

    /**
     * Generate content with Gemini using Retrofit service
     */
    suspend fun generateContent(
        prompt: String,
        model: String = MODEL_FLASH,
        systemInstruction: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "No valid Gemini API key configured in BuildConfig.")
            return@withContext Result.failure(IllegalStateException("API key missing or placeholder"))
        }

        try {
            val request = GeminiGenerateRequest(
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = prompt))
                    )
                ),
                systemInstruction = systemInstruction?.let {
                    GeminiContent(
                        role = "system",
                        parts = listOf(GeminiPart(text = it))
                    )
                },
                generationConfig = GeminiGenerationConfig()
            )

            val response = service.generateContent(
                model = model,
                apiKey = apiKey,
                request = request
            )

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string() ?: "HTTP ${response.code()}"
                Log.e(TAG, "Gemini Retrofit request failed: $errorBody")
                return@withContext Result.failure(Exception("Gemini error (${response.code()}): $errorBody"))
            }

            val body = response.body()
            val text = body?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                Result.failure(Exception("Empty candidate response from Gemini API"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini Retrofit call", e)
            Result.failure(e)
        }
    }

    /**
     * Generate comprehensive requirement documentation (SRS) based on user input
     */
    suspend fun generateRequirementDocumentation(
        userInput: String,
        domain: String = "Enterprise Software",
        projectType: String = "Cloud Platform"
    ): Result<String> {
        val prompt = """
            Generate formal, enterprise-grade software requirements documentation (IEEE 830 compliant) based on the user requirement description below:
            
            User Requirement Description:
            $userInput
            
            Domain: $domain
            System Type: $projectType
            
            Please provide a structured response containing:
            1. Executive Summary & Purpose
            2. High-Level Functional Requirements (FR-001, FR-002, etc. with Given-When-Then acceptance criteria)
            3. Non-Functional Quality Requirements (Performance, Security, Reliability, Usability)
            4. User Persona & Use Cases
            5. Architectural Recommendations & Data Persistence Strategy
        """.trimIndent()

        val systemInstruction = "You are a Principal Software Requirements Engineer and Systems Architect specialized in formal IEEE 830 requirement specifications."
        return generateContent(prompt, model = MODEL_PRO, systemInstruction = systemInstruction)
    }

    /**
     * Generate system design suggestions based on project requirement descriptions
     */
    suspend fun generateSystemDesignSuggestions(
        requirementText: String,
        projectContext: String = ""
    ): Result<String> {
        val prompt = """
            Analyze the following requirement description and generate comprehensive system design suggestions:
            
            Requirement Description:
            $requirementText
            
            Project Context:
            $projectContext
            
            Please provide:
            1. Recommended System Architecture Pattern (e.g. Clean Architecture, Event-Driven, Microservices) with rationale.
            2. Core Architectural Components & Modules (Presentation, Application/Domain, Infrastructure, Gateway).
            3. Relational Database Schema Suggestion (Entities, primary keys, foreign keys, indexes).
            4. RESTful API Contracts (HTTP Methods, paths, JSON request/response structures).
            5. Key Architectural Decision Records (ADR) and Tradeoffs.
        """.trimIndent()

        val systemInstruction = "You are a Principal Systems Architect. Provide actionable, robust, production-grade system designs with concrete schemas and API specifications."
        return generateContent(prompt, model = MODEL_PRO, systemInstruction = systemInstruction)
    }
}
