package com.example.martfia.model.request

import okhttp3.MultipartBody

// 이미지 데이터를 서버로 전송
data class IngredientRecognitionRequest(
    val image: MultipartBody.Part
)
