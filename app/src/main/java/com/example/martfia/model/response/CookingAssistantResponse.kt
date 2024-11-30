package com.example.martfia.model.response

data class CookingAssistantResponse(
    val message: String,
    val audio_url: String,
    val recipe_id: Int
)
