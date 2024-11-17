package com.example.martfia

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.martfia.adapter.RecommendedRecipeAdapter
import com.example.martfia.model.RecommendedRecipe
import com.example.martfia.model.request.RecommendedRecipeRequest
import com.example.martfia.model.response.RecommendedRecipeResponse
import com.example.martfia.service.MartfiaRetrofitClient
import com.example.martfia.service.RecommendedRecipeService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecommendedRecipeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recommended_recipe)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        val recipeRecyclerView = findViewById<RecyclerView>(R.id.recommendedRecipeRecyclerView)
        recipeRecyclerView.layoutManager = LinearLayoutManager(this)

        val ingredients = intent.getStringArrayListExtra("ingredient_list") ?: arrayListOf()

        // API 호출하여 추천 레시피 가져오기
        val recommendedRecipeService = MartfiaRetrofitClient.createService(RecommendedRecipeService::class.java)
        val request = RecommendedRecipeRequest(ingredients)
        val call = recommendedRecipeService.getRecommendedRecipes(request)

        call.enqueue(object : Callback<RecommendedRecipeResponse> {
            override fun onResponse(call: Call<RecommendedRecipeResponse>, response: Response<RecommendedRecipeResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val recommendedRecipes = response.body()!!.recipes

                    // 어댑터 설정
                    val adapter = RecommendedRecipeAdapter(recommendedRecipes) { selectedRecipe ->
                        // TODO: 레시피 클릭 시 상세 화면으로 이동 (추후 구현 예정)
                        Toast.makeText(this@RecommendedRecipeActivity, "${selectedRecipe.name} 클릭됨", Toast.LENGTH_SHORT).show()
                    }
                    recipeRecyclerView.adapter = adapter
                } else {
                    Toast.makeText(this@RecommendedRecipeActivity, "추천 레시피를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RecommendedRecipeResponse>, t: Throwable) {
                Toast.makeText(this@RecommendedRecipeActivity, "서버와의 통신에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
