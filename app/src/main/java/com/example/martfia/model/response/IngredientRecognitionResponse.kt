package com.example.martfia.model.response

// 서버 응답 매핑
data class IngredientRecognitionResponse(
    val ingredients: List<String>             // 인식된 재료 리스트
)
