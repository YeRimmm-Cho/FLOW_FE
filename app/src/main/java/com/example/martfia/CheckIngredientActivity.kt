package com.example.martfia

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.martfia.adapter.IngredientAdapter
import com.example.martfia.model.Ingredient
import com.example.martfia.model.request.RecommendedRecipeRequest
import com.example.martfia.model.response.RecommendedRecipeResponse
import com.example.martfia.service.RecommendedRecipeService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CheckIngredientActivity : AppCompatActivity() {

    private lateinit var recommendedRecipeService: RecommendedRecipeService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_ingredient)

        // RecyclerView 설정
        val ingredientRecyclerView = findViewById<RecyclerView>(R.id.ingredientRecyclerView)
        ingredientRecyclerView.layoutManager = LinearLayoutManager(this)

        // Intent로부터 재료 리스트 받기 (Ingredient 객체 리스트)
        val ingredients = intent.getParcelableArrayListExtra<Ingredient>("saved_ingredients") ?: arrayListOf()

        // 어댑터 설정
        val adapter = IngredientAdapter(ingredients)
        ingredientRecyclerView.adapter = adapter

        // "레시피 추천 받기" 버튼 설정
        val recommendRecipeButton = findViewById<Button>(R.id.recommendRecipeButton)
        recommendRecipeButton.setOnClickListener {
            // 레시피 추천 API 호출 (재료 리스트 넘김)
            getRecommendedRecipes(ingredients)
        }
    }

    private fun getRecommendedRecipes(ingredients: List<Ingredient>) {
        val ingredientNames = ingredients.map { it.name }

        if (ingredientNames.isEmpty()) {
            Toast.makeText(this, "재료가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = RecommendedRecipeRequest(
            ingredients = ingredientNames
        )

        recommendedRecipeService.getRecommendedRecipes(request).enqueue(object : Callback<RecommendedRecipeResponse> {
            override fun onResponse(call: Call<RecommendedRecipeResponse>, response: Response<RecommendedRecipeResponse>) {
                if (response.isSuccessful) {
                    val recipeList = response.body()?.recipes
                    if (!recipeList.isNullOrEmpty()) {
                        val intent = Intent(this@CheckIngredientActivity, RecommendedRecipeActivity::class.java)
                        intent.putParcelableArrayListExtra("recipe_list", ArrayList(recipeList)) // Parcelable로 리스트 전달
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@CheckIngredientActivity, "추천 레시피가 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@CheckIngredientActivity, "레시피 추천을 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RecommendedRecipeResponse>, t: Throwable) {
                Toast.makeText(this@CheckIngredientActivity, "API 요청에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
