package com.example.martfia

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import io.livekit.android.LiveKit
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class TestActivity : AppCompatActivity() {
    private lateinit var room: Room  // Room 인스턴스 변수
    private lateinit var statusText: TextView  // 연결 상태 텍스트를 표시하는 변수
    private lateinit var tokenTestButton: Button  // 토큰 테스트 버튼 변수
    private lateinit var audioManager: AudioManager  // AudioManager 인스턴스
    private lateinit var mediaPlayer: MediaPlayer  // MediaPlayer 인스턴스
    private lateinit var client: OkHttpClient  // WebSocket 클라이언트
    private var audioFocusRequest: AudioFocusRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        // UI 요소 초기화
        statusText = findViewById(R.id.statusText)
        tokenTestButton = findViewById(R.id.tokenTestButton)

        // 오디오 매니저 초기화
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // WebSocket 클라이언트 초기화
        client = OkHttpClient()

        // 권한 요청
        requestPermissions()

        // 토큰을 이용해 LiveKit에 연결하는 버튼 클릭 리스너
        tokenTestButton.setOnClickListener {
            val tokenResponse =
                """{ "room":"default-room","token":"" }"""
            val wsUrl = "wss://donggukmartfia-4jdthdl3.livekit.cloud"
            connectToRoom(wsUrl, tokenResponse)  // 방에 연결
            startWebSocket(wsUrl)  // WebSocket 연결 시작
        }
    }

    // 권한 요청 함수 (오디오 및 인터넷 권한 필요)
    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET
        )
        val permissionGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, permissions, 0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            val audioPermissionGranted =
                grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (!audioPermissionGranted) {
                statusText.text = "Audio permission is required for LiveKit connection"
            }
        }
    }

    // LiveKit에 연결하는 함수
    private fun connectToRoom(wsUrl: String, tokenResponse: String) {
        lifecycleScope.launch {
            try {
                val jsonResponse = JSONObject(tokenResponse)
                val token = jsonResponse.getString("token")
                val roomName = jsonResponse.getString("room")

                room = LiveKit.create(applicationContext)
                launch {
                    room.events.collect { event ->
                        when (event) {
                            is RoomEvent.Connected -> {
                                Log.d("LiveKit", "방에 성공적으로 연결됨: $roomName")
                                statusText.text = "Connected to room: $roomName"
                            }

                            is RoomEvent.Disconnected -> {
                                Log.e("LiveKit", "방에서 연결 끊김: $roomName")
                                statusText.text = "Disconnected from room: $roomName"
                                // 재연결 시도
                                delay(3000)  // 3초 대기 후 재연결 시도
                                room.connect(wsUrl, token)
                            }

                            is RoomEvent.TrackSubscribed -> {
                                val track = event.track
                                if (track is io.livekit.android.room.track.AudioTrack) {
                                    Log.d("LiveKit", "오디오 트랙이 구독되었습니다.")
                                    statusText.text = "Audio track subscribed and playing"
                                    track.start()  // 오디오 트랙 수동 시작
                                    setSpeakerMode(true)  // 스피커 모드 활성화
                                }
                            }

                            else -> Log.d("LiveKit", "이벤트 수신됨: $event")
                        }
                    }
                }

                room.connect(wsUrl, token)
                statusText.text = "Attempting to connect to room: $roomName"

            } catch (e: Exception) {
                Log.e("LiveKit", "연결 실패: ${e.message}", e)
                statusText.text = "Failed: ${e.message}"
            }
        }
    }

    // 오디오 포커스 요청 함수
    private fun requestAudioFocus(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).apply {
                setAudioAttributes(AudioAttributes.Builder().apply {
                    setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                }.build())
                setAcceptsDelayedFocusGain(true)
                setOnAudioFocusChangeListener { }
            }.build().also {
                val result =
                    audioManager.requestAudioFocus(it) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
                Log.d("AudioFocus", "Audio focus request result: $result")
                return result
            }
        }
        val result = audioManager.requestAudioFocus(
            null,
            AudioManager.STREAM_VOICE_CALL,
            AudioManager.AUDIOFOCUS_GAIN
        ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        Log.d("AudioFocus", "Audio focus request result: $result")
        return result
    }

    // 스피커 모드 설정 함수
    private fun setSpeakerMode(enabled: Boolean) {
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = enabled
    }

    // WebSocket을 통해 서버와 연결하는 함수
    private fun startWebSocket(wsUrl: String) {
        val request = Request.Builder().url(wsUrl).build()
        val webSocketListener = object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                // 수신한 바이너리 데이터 재생
                playAudio(bytes.toByteArray())
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "WebSocket error: ${t.message}")
            }
        }
        client.newWebSocket(request, webSocketListener)
    }

    // 수신한 오디오 데이터를 재생하는 함수
    private fun playAudio(audioData: ByteArray) {
        try {
            val tempFile = File.createTempFile("audio", "tmp", cacheDir)
            val fos = FileOutputStream(tempFile)
            fos.write(audioData)
            fos.close()

            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(tempFile.absolutePath)
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (e: IOException) {
            Log.e("AudioPlay", "Audio playback error: ${e.message}")
        }
    }
}
