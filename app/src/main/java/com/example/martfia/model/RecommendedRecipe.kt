package com.example.martfia.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecommendedRecipe(
    val name: String,
    val imageUrl: String,
    val time: String,
    val description: String // 조리 방법
) : Parcelable
