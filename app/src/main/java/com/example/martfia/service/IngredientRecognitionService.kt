package com.example.martfia.service

import com.example.martfia.model.response.IngredientRecognitionResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface IngredientRecognitionService {

    // 재료 이미지 업로드 API
    @Multipart
    @POST("api/image/food")
    fun uploadImage(
        @Part photo: MultipartBody.Part,           // 이미지 파일
        @Part("createdAt") createdAt: RequestBody  // 생성 시간
    ): Call<IngredientRecognitionResponse>

    // 온라인 영수증 이미지 업로드 API
    @Multipart
    @POST("api/image/receipt")
    fun uploadReceipt(
        @Part photo: MultipartBody.Part,           // 이미지 파일
        @Part("createdAt") createdAt: RequestBody  // 생성 시간
    ): Call<IngredientRecognitionResponse>

    // 인식된 재료 업데이트 API
    @Multipart
    @POST("api/ingredients")
    fun recognizeIngredients(
        @Part photo: MultipartBody.Part
    ): Call<IngredientRecognitionResponse> // 서버에서 인식된 재료 리스트 반환

}
