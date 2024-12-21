package com.example.martfia.service

import com.example.martfia.model.response.YoutubeAssistantStartResponse
import retrofit2.Call
import retrofit2.http.POST

interface YoutubeAssistantService {
    @POST("api/recipe/start")
    fun startCookingAssistant(): Call<YoutubeAssistantStartResponse>
}
