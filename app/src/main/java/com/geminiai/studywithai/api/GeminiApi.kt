package com.geminiai.studywithai.api

import com.geminiai.studywithai.models.GeminiResponse
import com.geminiai.studywithai.models.TextRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


interface GeminiApi {
    @POST("getAnswer")
    fun getAnswer(@Body textRequest: TextRequest?): Call<GeminiResponse?>?
}