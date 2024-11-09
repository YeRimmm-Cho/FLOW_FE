package com.example.martfia

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.martfia.R
import io.livekit.android.LiveKit
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import io.livekit.android.room.track.AudioTrack
import kotlinx.coroutines.launch
import org.json.JSONObject

class TestActivity : AppCompatActivity() {
    private lateinit var room: Room  // Room 인스턴스 변수
    private lateinit var statusText: TextView  // 연결 상태 텍스트를 표시하는 변수
    private lateinit var tokenTestButton: Button  // 토큰 테스트 버튼 변수
    private lateinit var recordButton: Button  // 녹음 버튼 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        // UI 요소 초기화
        statusText = findViewById(R.id.statusText)
        tokenTestButton = findViewById(R.id.tokenTestButton)
        recordButton = findViewById(R.id.recordButton)

        // 권한 요청
        requestPermissions()

        // 토큰을 이용해 LiveKit에 연결하는 버튼 클릭 리스너
        tokenTestButton.setOnClickListener {
            val tokenResponse = """{ "room":"","token":"" }"""
            val wsUrl = "wss://donggukmartfia-4jdthdl3.livekit.cloud"
            connectToRoom(wsUrl, tokenResponse)  // 방에 연결
        }

        // 녹음 시작 버튼 클릭 시 마이크를 활성화하는 리스너
        recordButton.setOnClickListener {
            startRecording()
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
                            }
                            is RoomEvent.TrackSubscribed -> {
                                val track = event.track
                                if (track is AudioTrack) {
                                    Log.d("LiveKit", "오디오 트랙이 구독되었습니다.")
                                    statusText.text = "Audio track subscribed and playing"
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

    // 녹음 시작 함수 - 마이크 활성화
    private fun startRecording() {
        lifecycleScope.launch {
            try {
                room.localParticipant.setMicrophoneEnabled(true)
                statusText.text = "Recording audio..."
            } catch (e: Exception) {
                Log.e("LiveKit", "마이크 활성화 실패: ${e.message}", e)
                statusText.text = "Microphone error: ${e.message}"
            }
        }
    }
}
