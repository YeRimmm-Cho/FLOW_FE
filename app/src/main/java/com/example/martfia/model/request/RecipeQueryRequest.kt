//package com.example.martfia.model.request
//
//data class RecipeQueryRequest(
//    val text: String? = null,
//    val current_step: Int
//)
package com.example.martfia.model.request

data class RecipeQueryRequest(
    val text: String = "",
    val current_step: Int
)