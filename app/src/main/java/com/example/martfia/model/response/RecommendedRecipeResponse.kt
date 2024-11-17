package com.example.martfia.model.response

import com.example.martfia.model.RecommendedRecipe

data class RecommendedRecipeResponse(
    val recipes: List<RecommendedRecipe>
)
