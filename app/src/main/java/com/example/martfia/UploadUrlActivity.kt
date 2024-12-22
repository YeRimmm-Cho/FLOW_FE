package com.example.martfia

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.martfia.model.request.YouTubeRequest
import com.example.martfia.model.response.YouTubeRecipeDetailsResponse
import com.example.martfia.service.MartfiaRetrofitClient
import com.example.martfia.service.YouTubeService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log

class UploadUrlActivity : AppCompatActivity() {

    private lateinit var urlEditText: EditText
    private lateinit var detectUrlButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var backButton: ImageView

    private val youTubeService: YouTubeService by lazy {
        MartfiaRetrofitClient.createService(YouTubeService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_url)

        // UI 요소 초기화
        urlEditText = findViewById(R.id.urlEditText)
        detectUrlButton = findViewById(R.id.detectUrlButton)
        progressBar = findViewById(R.id.progressBar)
        backButton = findViewById(R.id.backButton)

        // 뒤로 가기 버튼 클릭 리스너
        backButton.setOnClickListener {
            navigateToChooseImageActivity()
        }

        // URL 인식 버튼 클릭 리스너
        detectUrlButton.setOnClickListener {
            val url = urlEditText.text.toString().trim()
            if (url.isEmpty()) {
                Toast.makeText(this, "URL을 입력해주세요", Toast.LENGTH_SHORT).show()
            } else {
                progressBar.visibility = View.VISIBLE
                uploadYouTubeUrl(url)
            }
        }
    }

    private fun uploadYouTubeUrl(url: String) {
        val request = YouTubeRequest(url)

        youTubeService.uploadYouTubeUrl(request).enqueue(object : Callback<YouTubeRecipeDetailsResponse> {
            override fun onResponse(
                call: Call<YouTubeRecipeDetailsResponse>,
                response: Response<YouTubeRecipeDetailsResponse>
            ) {
                progressBar.visibility = View.GONE // ProgressBar 숨기기
                if (response.isSuccessful) {
                    val recipeDetails = response.body()
                    Log.d("UploadUrlActivity", "Response Body: $recipeDetails")
                    Log.d("UploadUrlActivity", "Raw JSON: ${response.raw()}")
                    if (recipeDetails != null) {
                        // instructions 필드가 비어있는지 확인
                        if (recipeDetails.instructions.isNullOrEmpty()) {
                            Log.w("UploadUrlActivity", "Instructions field is null or empty")
                            Toast.makeText(
                                this@UploadUrlActivity,
                                "조리 단계 정보가 없습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            moveToRecipeDetailActivity(recipeDetails)
                        }
                    } else {
                        Log.e("UploadUrlActivity", "RecipeDetails is null")
                        Toast.makeText(
                            this@UploadUrlActivity,
                            "레시피 정보를 받을 수 없습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.e("UploadUrlActivity", "Response Code: ${response.code()}")
                    Log.e("UploadUrlActivity", "Error Body: ${response.errorBody()?.string()}") // Error Body 출력
                    Toast.makeText(
                        this@UploadUrlActivity,
                        "업로드 실패: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<YouTubeRecipeDetailsResponse>, t: Throwable) {
                progressBar.visibility = View.GONE // ProgressBar 숨기기
                Log.e("UploadUrlActivity", "Request failed: ${t.message}")
                Toast.makeText(
                    this@UploadUrlActivity,
                    "에러 발생: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun moveToRecipeDetailActivity(recipeDetails: YouTubeRecipeDetailsResponse) {
        val intent = Intent(this, RecipeDetailActivity::class.java)
        intent.putExtra("recipeDetails", recipeDetails) // 데이터 전달
        startActivity(intent)
    }

    private fun navigateToChooseImageActivity() {
        val intent = Intent(this, ChooseImageActivity::class.java)
        startActivity(intent)
        finish() // 현재 액티비티 종료
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // 뒤로 가기 버튼 누르면 ChooseImageActivity로 이동
        navigateToChooseImageActivity()
    }
}
