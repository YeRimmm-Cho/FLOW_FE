package com.example.martfia.service

import com.example.martfia.model.response.IngredientRecognitionResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface IngredientRecognitionService {
    @Multipart
    @POST("api/recognizeIngredients")
    fun uploadImage(
        @Part image: MultipartBody.Part
    ): Call<IngredientRecognitionResponse>
}
