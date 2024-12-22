package com.example.martfia.service

import com.example.martfia.model.response.YouTubeRecipeDetailsResponse
import retrofit2.Call
import retrofit2.http.GET

interface YouTubeRecipeService {
    @GET("api/recipe/details")
    fun getRecipeDetails(): Call<YouTubeRecipeDetailsResponse>
}
