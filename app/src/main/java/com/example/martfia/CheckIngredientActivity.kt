package com.example.martfia

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.martfia.adapter.IngredientAdapter
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

        // Intent로부터 재료 리스트 받기
        val ingredients = intent.getStringArrayListExtra("ingredient_list") ?: arrayListOf()

        // 어댑터 설정
        val adapter = IngredientAdapter(ingredients)
        ingredientRecyclerView.adapter = adapter

        // "레시피 추천 받기" 버튼 설정
        val recommendRecipeButton = findViewById<Button>(R.id.recommendRecipeButton)
        recommendRecipeButton.setOnClickListener {
            // 레시피 추천 API 호출
            getRecommendedRecipes(ingredients)
        }
    }

    private fun getRecommendedRecipes(ingredients: List<String>) {
        // 사진 URL, 재료 이름, 요리 시간 설정 (여기서는 임의 값 사용)
        val photoUrl = "samplePhotoUrl" // 실제 이미지 URL로 대체
        val foodNames = ingredients.joinToString(", ")
        val cookingTime = "30" // 실제 요리 시간 값으로 변경

        // Retrofit을 이용한 API 호출
        recommendedRecipeService.getRecommendedRecipes(
            photo = photoUrl,
            foodName = foodNames,
            cookingTime = cookingTime
        ).enqueue(object : Callback<RecommendedRecipeResponse> {
            override fun onResponse(
                call: Call<RecommendedRecipeResponse>,
                response: Response<RecommendedRecipeResponse>
            ) {
                if (response.isSuccessful) {
                    val recipeList = response.body()?.recipes
                    // 결과 처리: 추천 레시피 화면으로 이동
                    val intent = Intent(this@CheckIngredientActivity, RecommendedRecipeActivity::class.java)
                    intent.putExtra("recipe_list", ArrayList(recipeList)) // 레시피 리스트 전달
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@CheckIngredientActivity,
                        "레시피 추천을 실패했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<RecommendedRecipeResponse>, t: Throwable) {
                Toast.makeText(
                    this@CheckIngredientActivity,
                    "API 요청에 실패했습니다. 다시 시도해주세요.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
