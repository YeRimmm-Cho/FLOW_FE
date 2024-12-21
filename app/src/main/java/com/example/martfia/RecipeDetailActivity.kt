package com.example.martfia

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.martfia.adapter.CookingStepAdapter
import com.example.martfia.model.response.CookingAssistantResponse
import com.example.martfia.model.response.YouTubeRecipeDetailsResponse
import com.example.martfia.service.MartfiaRetrofitClient
import com.example.martfia.service.RecommendedRecipeService

class RecipeDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        // UI 요소 초기화
        val backButton = findViewById<ImageView>(R.id.backButton)
        val recipeImageView = findViewById<ImageView>(R.id.recipeImageView)
        val recipeNameTextView = findViewById<TextView>(R.id.recipeNameTextView)
        val recipeTimeTextView = findViewById<TextView>(R.id.recipeTimeTextView)
        val recipeStepsRecyclerView = findViewById<RecyclerView>(R.id.recipeStepsRecyclerView)
        val assistantButton = findViewById<LinearLayout>(R.id.assistantButton)

        // 뒤로 가기 버튼 클릭 리스너
        backButton.setOnClickListener {
            finish() // 이전 화면으로 이동
        }

        // Intent에서 YouTubeRecipeDetailsResponse 데이터 가져오기
        val recipeDetails = intent.getParcelableExtra<YouTubeRecipeDetailsResponse>("recipeDetails")
        if (recipeDetails != null) {
            val recipe = recipeDetails.recipe

            // UI 업데이트
            recipeNameTextView.text = recipe.foodName
            recipeTimeTextView.text = recipe.cookingTime

            Glide.with(this)
                .load(recipe.image)
                .centerCrop()
                .placeholder(R.drawable.img_cooking)
                .into(recipeImageView)

            // RecyclerView 설정
            recipeStepsRecyclerView.layoutManager = LinearLayoutManager(this)
            recipeStepsRecyclerView.adapter = CookingStepAdapter(recipe.instructions)

        } else {
            Toast.makeText(this, "레시피 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }

        // 조리 Assistant 버튼 동작 연결
        assistantButton.setOnClickListener {
            startCookingAssistantSession()
        }
    }

    private fun startCookingAssistantSession() {
        val service = MartfiaRetrofitClient.createService(RecommendedRecipeService::class.java)

        service.startCookingAssistant().enqueue(object : retrofit2.Callback<CookingAssistantResponse> {
            override fun onResponse(
                call: retrofit2.Call<CookingAssistantResponse>,
                response: retrofit2.Response<CookingAssistantResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val responseData = response.body()!!
                    val intent = Intent(this@RecipeDetailActivity, CookingAssistantActivity::class.java)
                    intent.putExtra("welcome_message", responseData.message)
                    intent.putExtra("audio_url", responseData.audio_url)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@RecipeDetailActivity, "조리 어시스턴트를 시작할 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<CookingAssistantResponse>, t: Throwable) {
                Toast.makeText(this@RecipeDetailActivity, "서버와의 통신에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
