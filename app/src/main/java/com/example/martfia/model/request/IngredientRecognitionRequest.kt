package com.example.martfia.model.request

import okhttp3.MultipartBody
import okhttp3.RequestBody

// 이미지 데이터 및 생성 시간을 서버로 전송
data class IngredientRecognitionRequest(
    val photo: String,           // 이미지 파일
    val createdAt: okhttp3.RequestBody                // 생성 시간
)
