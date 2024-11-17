package com.example.martfia.service

import com.example.martfia.model.request.RecommendedRecipeRequest
import com.example.martfia.model.response.RecommendedRecipeResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface RecommendedRecipeService {
    @POST("api/recommendRecipes")
    fun getRecommendedRecipes(
        @Body request: RecommendedRecipeRequest
    ): Call<RecommendedRecipeResponse>
}
