package com.example.martfia

import android.content.Intent // Intent import 추가
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.martfia.model.response.RecipeQueryResponse
import com.example.martfia.service.MartfiaRetrofitClient
import com.example.martfia.service.CookingAssistantService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class CookingAssistantActivity : AppCompatActivity() {

    private var currentStep = 1 // 초기 단계
    private lateinit var cookingStepMessage: TextView
    private lateinit var nextStepButton: Button
    private lateinit var backButton: ImageView
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var speechRecognizer: SpeechRecognizer // 음성 인식기

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cooking_assistant)

        // 뷰 초기화
        cookingStepMessage = findViewById(R.id.cookingStepMessage)
        nextStepButton = findViewById(R.id.nextStepButton)
        backButton = findViewById(R.id.backButton)

        val recipeId = intent.getIntExtra("recipe_id", -1) // 전달받은 recipe_id
        if (recipeId == -1) {
            Toast.makeText(this, "잘못된 레시피 ID입니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // SpeechRecognizer 초기화
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        setupSpeechRecognizer(recipeId)

        // 초기 단계 호출
        fetchCookingStep(recipeId, currentStep)

        // "다음 단계" 버튼: 선택적으로 사용
        nextStepButton.setOnClickListener {
            currentStep++
            fetchCookingStep(recipeId, currentStep)
        }

        // 뒤로 가기 버튼
        backButton.setOnClickListener {
            stopAudio()
            finish()
        }
    }

    private fun fetchCookingStep(recipeId: Int, step: Int) {
        val service = MartfiaRetrofitClient.createService(CookingAssistantService::class.java)

        service.queryRecipeStep(recipeId, null, null, step).enqueue(object : Callback<RecipeQueryResponse> {
            override fun onResponse(
                call: Call<RecipeQueryResponse>,
                response: Response<RecipeQueryResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val responseData = response.body()!!
                    // 안내 메시지 업데이트
                    cookingStepMessage.text = responseData.text
                    playAudio(responseData.audio_url) // 음성 응답 재생
                } else {
                    Toast.makeText(this@CookingAssistantActivity, "조리 단계를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RecipeQueryResponse>, t: Throwable) {
                Toast.makeText(this@CookingAssistantActivity, "서버와의 통신에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun playAudio(audioUrl: String) {
        Log.d("CookingAssistantActivity", "Attempting to play audio from: $audioUrl")
        stopAudio()
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(audioUrl)
                setOnPreparedListener { start() }
                setOnCompletionListener {
                    Log.d("CookingAssistantActivity", "Audio playback completed.")
                }
                prepareAsync()
            } catch (e: Exception) {
                Log.e("CookingAssistantActivity", "Error playing audio: $e")
                Toast.makeText(this@CookingAssistantActivity, "오디오를 재생할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun stopAudio() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
                reset()
                release()
            }
        }
        mediaPlayer = null
    }

    private fun setupSpeechRecognizer(recipeId: Int) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Toast.makeText(this@CookingAssistantActivity, "음성 인식을 시작합니다.", Toast.LENGTH_SHORT).show()
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                Toast.makeText(this@CookingAssistantActivity, "음성 인식에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                val voiceResults = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val userSpeech = voiceResults?.get(0) ?: ""

                if (userSpeech.contains("다음", true)) {
                    currentStep++
                    fetchCookingStep(recipeId, currentStep)
                } else {
                    Toast.makeText(this@CookingAssistantActivity, "다음 단계 요청이 인식되지 않았습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        // 음성 인식 시작
        speechRecognizer.startListening(intent)
    }


    override fun onDestroy() {
        super.onDestroy()
        stopAudio()
        speechRecognizer.destroy()
    }
}
