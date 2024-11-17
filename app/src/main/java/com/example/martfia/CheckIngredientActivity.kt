package com.example.martfia

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.martfia.adapter.IngredientAdapter

class CheckIngredientActivity : AppCompatActivity() {

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
            // 재료 목록을 전달하면서 RecommendedRecipeActivity로 전환
            val intent = Intent(this, RecommendedRecipeActivity::class.java)
            intent.putStringArrayListExtra("ingredient_list", ingredients)
            startActivity(intent)
        }
    }
}
