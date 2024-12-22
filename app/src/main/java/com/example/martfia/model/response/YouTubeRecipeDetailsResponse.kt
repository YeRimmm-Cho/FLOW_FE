package com.example.martfia.model.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class YouTubeRecipeDetailsResponse(
    val foodName: String,             // 음식 이름
    val cookingTime: String,          // 조리 시간
    val image: String,                // 이미지 URL
    val instructions: List<Instruction> // 조리 단계
) : Parcelable

@Parcelize
data class Instruction(
    val step: String,                 // 단계 번호
    val description: String           // 단계 설명
) : Parcelable
