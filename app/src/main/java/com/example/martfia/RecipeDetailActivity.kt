package com.example.martfia

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.martfia.adapter.CookingStepAdapter
import com.example.martfia.model.CookingStep
import com.example.martfia.model.response.RecommendedRecipeDetailResponse
import com.example.martfia.service.MartfiaRetrofitClient
import com.example.martfia.service.RecommendedRecipeService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipeDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        val backButton = findViewById<ImageView>(R.id.backButton)
        val recipeImageView = findViewById<ImageView>(R.id.recipeImageView)
        val recipeNameTextView = findViewById<TextView>(R.id.recipeNameTextView)
        val recipeTimeTextView = findViewById<TextView>(R.id.recipeTimeTextView)
        val recipeStepsRecyclerView = findViewById<RecyclerView>(R.id.recipeStepsRecyclerView)

        backButton.setOnClickListener {
            val intent = Intent(this, RecommendedRecipeActivity::class.java)
            startActivity(intent)
            finish()
        }

        val recipeId = intent.getIntExtra("recipe_id", -1)
        if (recipeId == -1) {
            Toast.makeText(this, "잘못된 레시피 ID입니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val service = MartfiaRetrofitClient.createService(RecommendedRecipeService::class.java)
        service.getRecipeDetails(recipeId).enqueue(object : Callback<RecommendedRecipeDetailResponse> {
            override fun onResponse(
                call: Call<RecommendedRecipeDetailResponse>,
                response: Response<RecommendedRecipeDetailResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val recipeDetail = response.body()!!
                    recipeNameTextView.text = recipeDetail.foodName
                    recipeTimeTextView.text = recipeDetail.cookingTime

                    Glide.with(this@RecipeDetailActivity)
                        .load(recipeDetail.image)
                        .centerCrop()
                        .placeholder(R.drawable.img_cooking)
                        .into(recipeImageView)

                    // API로 가져온 조리 방법 설정
                    val cookingSteps = recipeDetail.steps.mapIndexed { index, step ->
                        CookingStep(stepNumber = index + 1, description = step)
                    }

                    // RecyclerView 설정
                    recipeStepsRecyclerView.layoutManager = LinearLayoutManager(this@RecipeDetailActivity)
                    recipeStepsRecyclerView.adapter = CookingStepAdapter(cookingSteps)
                } else {
                    Toast.makeText(this@RecipeDetailActivity, "레시피 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RecommendedRecipeDetailResponse>, t: Throwable) {
                Toast.makeText(this@RecipeDetailActivity, "서버와의 통신에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
