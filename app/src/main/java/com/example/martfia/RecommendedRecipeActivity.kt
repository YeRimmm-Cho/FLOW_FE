package com.example.martfia

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.martfia.adapter.RecommendedRecipeAdapter
import com.example.martfia.model.RecommendedRecipe
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

        // Intent로부터 재료 리스트 받기
        val ingredients = intent.getStringArrayListExtra("ingredient_list") ?: arrayListOf()

        // 기본적인 레시피 정보 설정
        val foodName = "Sample Food Name" // 예시 음식
        val cookingTime = "30" // 예시로 30분으로 설정 (실제 시간으로 수정 가능)
        val photo = "samplePhotoUrl" // 예시 URL (실제 URL로 수정 가능)

        // API 호출하여 추천 레시피 가져오기
        val recommendedRecipeService = MartfiaRetrofitClient.createService(RecommendedRecipeService::class.java)

        // API 호출
        recommendedRecipeService.getRecommendedRecipes(
            photo = photo,
            foodName = foodName,
            cookingTime = cookingTime
        ).enqueue(object : Callback<List<RecommendedRecipe>> {
            override fun onResponse(
                call: Call<List<RecommendedRecipe>>,
                response: Response<List<RecommendedRecipe>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val recommendedRecipes = response.body()!!

                    // 어댑터 설정
                    val adapter = RecommendedRecipeAdapter(recommendedRecipes) { selectedRecipe ->
                        // TODO: 레시피 클릭 시 상세 화면으로 이동 (추후 구현 예정)
                        Toast.makeText(this@RecommendedRecipeActivity, "${selectedRecipe.foodName} 클릭됨", Toast.LENGTH_SHORT).show()
                    }
                    recipeRecyclerView.adapter = adapter
                } else {
                    Toast.makeText(this@RecommendedRecipeActivity, "추천 레시피를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<RecommendedRecipe>>, t: Throwable) {
                Toast.makeText(this@RecommendedRecipeActivity, "서버와의 통신에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
