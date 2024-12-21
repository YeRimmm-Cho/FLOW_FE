package com.example.martfia.model.response

data class RecipeQueryResponse(
    val current_step: Int,           // 현재 단계
    val text: String                 // 안내 텍스트
)
