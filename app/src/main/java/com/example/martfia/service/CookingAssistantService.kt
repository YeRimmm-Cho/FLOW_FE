package com.example.martfia.service

import com.example.martfia.model.response.RecipeQueryResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface CookingAssistantService {
    @Multipart
    @POST("api/recipe/{recipe_id}/query")
    fun queryRecipeStep(
        @Path("recipe_id") recipeId: Int,
        @Part audio: MultipartBody.Part? = null,
        @Part("text") text: String? = null,
        @Part("current_step") currentStep: Int
    ): Call<RecipeQueryResponse>
}
