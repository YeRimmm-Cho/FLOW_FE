package com.example.martfia.service

import com.example.martfia.model.RecommendedRecipe
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RecommendedRecipeService {
    @GET("api/recipe")
    fun getRecommendedRecipes(
        @Query("photo") photo: String,          // 이미지 URL
        @Query("foodName") foodName: String,    // 재료 이름
        @Query("cookingTime") cookingTime: String // 요리 시간
    ): Call<List<RecommendedRecipe>>
}
