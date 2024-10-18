package com.example.martfia

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.martfia.adapter.RecipeAdapter
import com.example.martfia.adapter.SeasonalIngredientAdapter
import com.example.martfia.model.Recipe
import com.example.martfia.model.SeasonalIngredient

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // 레시피 더미 데이터
        val recipeList = listOf(
            Recipe("샐러드 파스타", "https://via.placeholder.com/150", "10 minutes"),
            Recipe("참치 볶음밥", "https://via.placeholder.com/150", "15 minutes"),
            Recipe("연어 포케", "https://via.placeholder.com/150", "20 minutes"),
            Recipe("김치찌개", "https://via.placeholder.com/150", "30 minutes"),
            Recipe("김밥", "https://via.placeholder.com/150", "25 minutes")
        )

        // SeasonalIngredient 더미 데이터
        val seasonalIngredientList = listOf(
            SeasonalIngredient("늙은 호박", "https://via.placeholder.com/150", "10월-12월"),
            SeasonalIngredient("대하", "https://via.placeholder.com/150", "9월-12월"),
            SeasonalIngredient("삼치", "https://via.placeholder.com/150", "10월-2월"),
            SeasonalIngredient("배", "https://via.placeholder.com/150", "9월-11월"),
        )

        // RecyclerView
        val recyclerView_Recipe = findViewById<RecyclerView>(R.id.todayRecipeList)
        val recyclerView_Ingredient = findViewById<RecyclerView>(R.id.seasonalIngredientsList)

        // LayoutManager - 한 줄 수평 스크롤
        val recipeLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val ingredientLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        recyclerView_Recipe.layoutManager = recipeLayoutManager
        recyclerView_Ingredient.layoutManager = ingredientLayoutManager

        // Adapter
        recyclerView_Recipe.adapter = RecipeAdapter(recipeList)
        recyclerView_Ingredient.adapter=SeasonalIngredientAdapter(seasonalIngredientList)



        // 추천 버튼 클릭 시 ChooseImageActivity로 이동
//        val recommendButton: Button = findViewById(R.id.recommendButton)
//        recommendButton.setOnClickListener {
//            val intent = Intent(this, ChooseImageActivity::class.java)
//            startActivity(intent)
//        }

        //  레이아웃이 시스템 영역과 겹치지 않도록 패딩 설정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}