package com.theoriongd.reqstrata.data.remote.gemini

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit Service interface for communicating with Google Gemini Generative Language API.
 */
interface GeminiRetrofitService {

    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiGenerateRequest
    ): Response<GeminiGenerateResponse>
}
