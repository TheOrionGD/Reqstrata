package com.theoriongd.reqstrata.data.remote.gemini

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiGenerateRequest(
    @param:Json(name = "contents") val contents: List<GeminiContent>,
    @param:Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null,
    @param:Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @param:Json(name = "role") val role: String? = "user",
    @param:Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @param:Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @param:Json(name = "temperature") val temperature: Double? = 0.4,
    @param:Json(name = "topP") val topP: Double? = 0.95,
    @param:Json(name = "topK") val topK: Int? = 40,
    @param:Json(name = "maxOutputTokens") val maxOutputTokens: Int? = 8192,
    @param:Json(name = "responseMimeType") val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerateResponse(
    @param:Json(name = "candidates") val candidates: List<GeminiCandidate>? = null,
    @param:Json(name = "promptFeedback") val promptFeedback: GeminiPromptFeedback? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @param:Json(name = "content") val content: GeminiContentResponse? = null,
    @param:Json(name = "finishReason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContentResponse(
    @param:Json(name = "parts") val parts: List<GeminiPartResponse>? = null,
    @param:Json(name = "role") val role: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiPartResponse(
    @param:Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiPromptFeedback(
    @param:Json(name = "blockReason") val blockReason: String? = null
)
