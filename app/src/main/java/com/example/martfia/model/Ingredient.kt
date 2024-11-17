package com.example.martfia.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Ingredient(
    val ingredientsName: String
) : Parcelable
