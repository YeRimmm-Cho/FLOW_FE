package com.example.martfia.model.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecommendedRecipeDetailResponse(
    val id: Int,              // 레시피 ID
    val foodName: String,     // 요리 이름
    val cookingTime: String,  // 요리 시간
    val image: String         // 이미지 URL
) : Parcelable
