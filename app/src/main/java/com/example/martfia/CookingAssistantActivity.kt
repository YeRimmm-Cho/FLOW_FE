package com.example.martfia

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.martfia.model.request.RecipeQueryRequest
import com.example.martfia.model.response.RecipeQueryResponse
import com.example.martfia.service.CookingAssistantService
import com.example.martfia.service.MartfiaRetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class CookingAssistantActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var currentStep = 1
    private var hasStartedGuide = false
    private lateinit var assistantMessage: String

    private lateinit var cookingStepMessage: TextView
    private lateinit var backButton: ImageView
    private lateinit var tts: TextToSpeech
    private var speechRecognizer: SpeechRecognizer? = null
    private val RECORD_AUDIO_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cooking_assistant)

        cookingStepMessage = findViewById(R.id.cookingStepMessage)
        backButton = findViewById(R.id.backButton)

        tts = TextToSpeech(this, this)
        assistantMessage = intent.getStringExtra("assistant_message") ?: "안내를 시작합니다."

        backButton.setOnClickListener {
            tts.stop()
            speechRecognizer?.destroy()
            finish()
        }

        checkMicrophonePermission()
    }

    private fun checkMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_CODE
            )
        } else {
            setupSpeechRecognizer()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupSpeechRecognizer()
            } else {
                Toast.makeText(this, "마이크 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Toast.makeText(this@CookingAssistantActivity, "음성 인식을 시작합니다.", Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val userSpeech = matches?.firstOrNull() ?: ""
                Log.d("CookingAssistant", "User speech: $userSpeech")

                when {
                    userSpeech.contains("다음", ignoreCase = true) -> moveToNextStep(userSpeech)
                    userSpeech.contains("다시", ignoreCase = true) -> repeatCurrentStep()
                    else -> processUserQuestion(userSpeech)
                }
            }

            override fun onError(error: Int) {
                Log.e("CookingAssistantActivity", "음성 인식 오류: $error")
            }

            override fun onEndOfSpeech() {
                startListening()
            }

            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBeginningOfSpeech() {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun restartSpeechRecognizer() {
        speechRecognizer?.destroy()
        setupSpeechRecognizer()
        startListening()
    }

    private fun startListening() {
        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN)
        }
        try {
            speechRecognizer?.startListening(speechIntent)
        } catch (e: Exception) {
            Log.e("CookingAssistantActivity", "startListening error: ${e.message}")
            runOnUiThread {
                Toast.makeText(this, "음성 인식 시작 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun moveToNextStep(userSpeech: String?) {
        if (userSpeech.isNullOrBlank()) {
            Toast.makeText(this, "음성 입력이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val service = MartfiaRetrofitClient.createService(CookingAssistantService::class.java)
        val request = RecipeQueryRequest(
            text = userSpeech,
            current_step = currentStep - 1
        )

        Log.d("CookingAssistantActivity", "Request: text=${request.text}, current_step=${request.current_step}")

        service.queryRecipeStep(request).enqueue(object : Callback<RecipeQueryResponse> {
            override fun onResponse(call: Call<RecipeQueryResponse>, response: Response<RecipeQueryResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val responseData = response.body()!!
                    currentStep++
                    cookingStepMessage.text = responseData.text
                    speakText(responseData.text)
                } else {
                    Log.e("CookingAssistantActivity", "Response Error: ${response.code()} - ${response.errorBody()?.string()}")
                    Toast.makeText(this@CookingAssistantActivity, "단계를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RecipeQueryResponse>, t: Throwable) {
                Log.e("CookingAssistantActivity", "Request Failed: ${t.message}")
                Toast.makeText(this@CookingAssistantActivity, "서버 요청 실패.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun repeatCurrentStep() {
        val service = MartfiaRetrofitClient.createService(CookingAssistantService::class.java)
        val request = RecipeQueryRequest(
            text = "다시 안내해주세요",
            current_step = currentStep
        )

        Log.d("CookingAssistantActivity", "Repeat Request: text=${request.text}, current_step=${request.current_step}")

        service.queryRecipeStep(request).enqueue(object : Callback<RecipeQueryResponse> {
            override fun onResponse(call: Call<RecipeQueryResponse>, response: Response<RecipeQueryResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val responseData = response.body()!!
                    cookingStepMessage.text = responseData.text
                    speakText(responseData.text)
                } else {
                    Log.e("CookingAssistantActivity", "Repeat Response Error: ${response.code()} - ${response.errorBody()?.string()}")
                    Toast.makeText(this@CookingAssistantActivity, "현재 단계를 다시 안내할 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RecipeQueryResponse>, t: Throwable) {
                Log.e("CookingAssistantActivity", "Repeat Request Failed: ${t.message}")
                Toast.makeText(this@CookingAssistantActivity, "서버 요청 실패.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun processUserQuestion(userSpeech: String?) {
        if (userSpeech.isNullOrBlank()) {
            Toast.makeText(this, "질문 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val service = MartfiaRetrofitClient.createService(CookingAssistantService::class.java)
        val request = RecipeQueryRequest(
            text = userSpeech,
            current_step = currentStep
        )

        Log.d("CookingAssistantActivity", "Question Request: text=${request.text}, current_step=${request.current_step}")

        service.queryRecipeStep(request).enqueue(object : Callback<RecipeQueryResponse> {
            override fun onResponse(call: Call<RecipeQueryResponse>, response: Response<RecipeQueryResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val responseData = response.body()!!
                    cookingStepMessage.text = responseData.text
                    speakText(responseData.text)
                } else {
                    Log.e("CookingAssistantActivity", "Question Response Error: ${response.code()} - ${response.errorBody()?.string()}")
                    Toast.makeText(this@CookingAssistantActivity, "질문에 대한 응답을 받을 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RecipeQueryResponse>, t: Throwable) {
                Log.e("CookingAssistantActivity", "Question Request Failed: ${t.message}")
                Toast.makeText(this@CookingAssistantActivity, "서버 요청 실패.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun speakText(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, currentStep.toString())
        tts.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.d("CookingAssistantActivity", "TTS 음성 출력 시작")
            }

            override fun onDone(utteranceId: String?) {
                Log.d("CookingAssistantActivity", "TTS 음성 출력 완료")
                runOnUiThread {
                    startListening()
                }
            }

            override fun onError(utteranceId: String?) {
                Log.e("CookingAssistantActivity", "TTS 오류 발생")
                runOnUiThread {
                    Toast.makeText(this@CookingAssistantActivity, "TTS 오류 발생", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.KOREAN
            speakText(assistantMessage)
        } else {
            Toast.makeText(this, "TTS 초기화 실패", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
        speechRecognizer?.destroy()
    }
}
