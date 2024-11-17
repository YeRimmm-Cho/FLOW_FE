package com.example.martfia

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import io.livekit.android.LiveKit
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import io.livekit.android.room.track.AudioTrack
import kotlinx.coroutines.launch
import org.json.JSONObject

class TestActivity : AppCompatActivity() {

    private lateinit var room: Room
    private lateinit var statusText: TextView
    private lateinit var tokenTestButton: Button
    private lateinit var connectButton: Button
    private lateinit var audioManager: AudioManager

    companion object {
        private const val REQUEST_PERMISSIONS = 1001
        private const val TAG = "LiveKit"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        // UI 요소 초기화
        statusText = findViewById(R.id.statusText)
        tokenTestButton = findViewById(R.id.tokenTestButton)
        connectButton = findViewById(R.id.connectButton)

        // AudioManager 초기화
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // 권한 요청
        checkAndRequestPermissions()

        // LiveKit 연결 버튼 클릭 이벤트
        tokenTestButton.setOnClickListener {
            if (arePermissionsGranted()) {
                Log.d(TAG, "권한 승인됨. LiveKit 연결 시도 시작")
                statusText.text = "LiveKit에 연결 중입니다..."
                connectToLiveKitRoom()
            } else {
                Log.d(TAG, "권한이 부족합니다.")
                statusText.text = "권한이 필요합니다. 설정에서 권한을 허용해주세요."
                showPermissionDeniedDialog()
            }
        }

        // LiveKit 룸 입장 버튼 클릭 이벤트
        connectButton.setOnClickListener {
            statusText.text = "LiveKit 룸에 입장 중..."
            connectToLiveKitRoom() // LiveKit에 연결
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET
        )

        if (!arePermissionsGranted()) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS)
        }
    }

    private fun arePermissionsGranted(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET
        )

        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            val deniedPermissions = permissions.filterIndexed { index, _ ->
                grantResults[index] != PackageManager.PERMISSION_GRANTED
            }

            if (deniedPermissions.isNotEmpty()) {
                Log.d(TAG, "다음 권한이 거부되었습니다: ${deniedPermissions.joinToString()}")
                showPermissionDeniedDialog()
            } else {
                Log.d(TAG, "모든 권한 승인됨.")
                statusText.text = "모든 권한이 승인되었습니다. 연결을 시도하세요."
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("권한 필요")
            .setMessage("앱의 원활한 작동을 위해 권한이 필요합니다. 설정에서 권한을 활성화해주세요.")
            .setPositiveButton("설정으로 이동") { _, _ ->
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("취소") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun connectToLiveKitRoom() {
        val wsUrl = "wss://donggukmartfia-4jdthdl3.livekit.cloud"
        val tokenResponse = """{ "room":"default-room","token":"토큰" }"""

        lifecycleScope.launch {
            try {
                val jsonResponse = JSONObject(tokenResponse)
                val token = jsonResponse.getString("token")
                val roomName = jsonResponse.getString("room")

                Log.d(TAG, "LiveKit 연결 시작 - Room: $roomName, Token: $token")
                room = LiveKit.create(applicationContext)
                Log.d(TAG, "LiveKit 객체 생성 완료")

                // LiveKit 이벤트 처리
                room.events.collect { event ->
                    try {
                        Log.d(TAG, "LiveKit 이벤트 발생: $event")

                        when (event) {
                            is RoomEvent.Connected -> {
                                Log.d(TAG, "LiveKit 방에 성공적으로 연결되었습니다: ${event.room}")
                                statusText.text = "방에 성공적으로 연결되었습니다: ${event.room}"
                            }
                            is RoomEvent.Disconnected -> {
                                Log.e(TAG, "LiveKit 방 연결 해제: ${event.reason}")
                                statusText.text = "방 연결 해제: ${event.reason}"
                            }
                            is RoomEvent.TrackSubscribed -> {
                                Log.d(TAG, "오디오 트랙 구독됨")
                                statusText.text = "오디오 트랙이 활성화되었습니다."
                                handleAudioTrack(event.track as AudioTrack)
                            }
                            else -> {
                                Log.d(TAG, "Unhandled 이벤트: $event")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "LiveKit 이벤트 처리 중 오류 발생: ${e.message}", e)
                    }
                }

                room.connect(wsUrl, token)
                Log.d(TAG, "LiveKit connect() 호출 완료")
                statusText.text = "방에 연결 시도 중..."
            } catch (e: Exception) {
                Log.e(TAG, "LiveKit 연결 실패: ${e.message}", e)
                statusText.text = "연결 실패: ${e.message}"
            }
        }
    }

    private fun handleAudioTrack(audioTrack: AudioTrack) {
        try {
            audioTrack.start()
            setSpeakerMode(true)
            Log.d(TAG, "오디오 트랙 시작됨.")
        } catch (e: Exception) {
            Log.e(TAG, "오디오 트랙 처리 중 오류 발생: ${e.message}", e)
        }
    }

    private fun setSpeakerMode(enabled: Boolean) {
        try {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.isSpeakerphoneOn = enabled
            Log.d(TAG, "스피커 모드 설정됨: $enabled")
        } catch (e: Exception) {
            Log.e(TAG, "스피커 모드 설정 중 오류 발생: ${e.message}", e)
        }
    }
}
