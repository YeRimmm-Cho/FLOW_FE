package com.example.martfia.model.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class YouTubeRecipeDetailsResponse(
    val recipe: YouTubeRecipe? // Nullable로 변경
) : Parcelable

@Parcelize
data class YouTubeRecipe(
    val foodName: String,             // 음식 이름
    val cookingTime: String,          // 조리 시간
    val image: String,                // 이미지 URL
    val instructions: Map<String, String> // 조리 단계
) : Parcelable
