package com.example.martfia

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.martfia.model.request.RecipeQueryRequest
import com.example.martfia.model.response.RecipeQueryResponse
import com.example.martfia.service.MartfiaRetrofitClient
import com.example.martfia.service.CookingAssistantService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class CookingAssistantActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var currentStep = 1 // 초기 단계
    private lateinit var cookingStepMessage: TextView
    private lateinit var nextStepButton: Button
    private lateinit var backButton: ImageView
    private lateinit var tts: TextToSpeech
    private lateinit var speechRecognizer: SpeechRecognizer
    private var isSpeaking = false // TTS 진행 상태
    private var assistantMessage: String? = null // 이전 화면에서 전달된 메시지

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cooking_assistant)

        // UI 요소 초기화
        cookingStepMessage = findViewById(R.id.cookingStepMessage)
        nextStepButton = findViewById(R.id.nextStepButton)
        backButton = findViewById(R.id.backButton)

        // Intent로부터 초기 메시지 가져오기
        assistantMessage = intent.getStringExtra("assistant_message")

        // TTS 초기화
        tts = TextToSpeech(this, this)

        // 음성 인식기 초기화
        setupSpeechRecognizer()

        // "다음 단계" 버튼 클릭 리스너
        nextStepButton.setOnClickListener {
            if (!isSpeaking) {
                currentStep++
                fetchCookingStep(currentStep)
            } else {
                Toast.makeText(this, "현재 음성 출력 중입니다. 잠시만 기다려주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 뒤로 가기 버튼 클릭 리스너
        backButton.setOnClickListener {
            tts.stop()
            val intent = Intent(this, RecipeDetailActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun fetchCookingStep(step: Int) {
        val service = MartfiaRetrofitClient.createService(CookingAssistantService::class.java)

        val request = RecipeQueryRequest(
            text = null, // 선택적 필드
            current_step = step
        )

        service.queryRecipeStep(request).enqueue(object : Callback<RecipeQueryResponse> {
            override fun onResponse(call: Call<RecipeQueryResponse>, response: Response<RecipeQueryResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val responseData = response.body()!!
                    cookingStepMessage.text = responseData.text
                    speakText(responseData.text)
                } else {
                    Toast.makeText(this@CookingAssistantActivity, "단계를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RecipeQueryResponse>, t: Throwable) {
                Toast.makeText(this@CookingAssistantActivity, "서버와의 통신에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun speakText(text: String) {
        isSpeaking = true
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, currentStep.toString())
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.KOREAN)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "TTS에서 한국어를 지원하지 않습니다.", Toast.LENGTH_SHORT).show()
            } else {
                setupTTSListener()
                assistantMessage?.let { speakText(it) }
            }
        } else {
            Toast.makeText(this, "TTS 초기화 실패", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupTTSListener() {
        tts.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                isSpeaking = true
            }

            override fun onDone(utteranceId: String?) {
                isSpeaking = false
                startListening() // 음성 인식 시작
            }

            override fun onError(utteranceId: String?) {
                isSpeaking = false
            }
        })
    }

    private fun setupSpeechRecognizer() {
        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN)
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Toast.makeText(this@CookingAssistantActivity, "음성 인식을 시작합니다.", Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val userSpeech = matches?.firstOrNull() ?: ""
                if (userSpeech.contains("다음", ignoreCase = true)) {
                    currentStep++
                    fetchCookingStep(currentStep)
                } else {
                    Toast.makeText(this@CookingAssistantActivity, "다음 단계로 이동하려면 '다음'이라고 말해주세요.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(error: Int) {
                Log.e("CookingAssistantActivity", "음성 인식 오류: $error")
            }

            override fun onEndOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBeginningOfSpeech() {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun startListening() {
        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN)
        }
        speechRecognizer.startListening(speechIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
        speechRecognizer.destroy()
    }
}
