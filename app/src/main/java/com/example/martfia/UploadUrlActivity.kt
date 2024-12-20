package com.example.martfia

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class UploadUrlActivity : AppCompatActivity() {

    private lateinit var urlEditText: EditText
    private lateinit var detectUrlButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_url)

        // UI 요소 초기화
        urlEditText = findViewById(R.id.urlEditText)
        detectUrlButton = findViewById(R.id.detectUrlButton)

        // 버튼 클릭 리스너
        detectUrlButton.setOnClickListener {
            val url = urlEditText.text.toString().trim()
            if (url.isEmpty()) {
                Toast.makeText(this, "URL을 입력해주세요", Toast.LENGTH_SHORT).show()
            } else {
                // TODO: API를 통해 서버로 URL 전송
                // 현재는 RecipeDetailActivity로 이동만 구현
                moveToRecipeDetailActivity()
            }
        }
    }

    private fun moveToRecipeDetailActivity() {
        val intent = Intent(this, RecipeDetailActivity::class.java)
        // TODO: 서버 응답 데이터를 intent에 추가 (예: intent.putExtra("detailRecipe", recipeData))
        startActivity(intent)
    }
}
