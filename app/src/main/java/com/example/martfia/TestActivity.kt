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
        connectButton = findViewById(R.id.connectButton)

        // AudioManager 초기화
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // 권한 요청
        checkAndRequestPermissions()

        // CONNECT 버튼 클릭 이벤트
        connectButton.setOnClickListener {
            lifecycleScope.launch {
                if (arePermissionsGranted()) {
                    connectToLiveKitRoom()
                } else {
                    statusText.text = "권한이 필요합니다. 설정에서 권한을 허용해주세요."
                    showPermissionDeniedDialog()
                }
            }
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

    private suspend fun connectToLiveKitRoom() {
        val wsUrl = "wss://donggukmartfia-4jdthdl3.livekit.cloud"
        val tokenResponse = """{ "room":"","token":"" }"""

        try {
            val jsonResponse = JSONObject(tokenResponse)
            val token = jsonResponse.getString("token")
            val roomName = jsonResponse.getString("room")

            Log.d(TAG, "룸 연결 시작 - Room: $roomName")

            if (!::room.isInitialized) {
                room = LiveKit.create(applicationContext)
                Log.d(TAG, "LiveKit 객체 생성 완료")
            }

            room.connect(wsUrl, token)
            Log.d(TAG, "룸 연결 요청 완료.")
            statusText.text = "룸 연결 시도 중..."

            room.events.collect { event ->
                when (event) {
                    is RoomEvent.Connected -> {
                        Log.d(TAG, "룸 연결 성공: ${event.room.name}")
                        configureAudioChannelAndMicrophone() // 오디오 채널 및 마이크 설정
                        publishLocalAudioTrack() // 로컬 마이크 트랙 퍼블리싱
                    }
                    is RoomEvent.TrackSubscribed -> {
                        if (event.track is AudioTrack) {
                            handleSubscribedAudioTrack(event.track as AudioTrack)
                        }
                    }
                    else -> Log.d(TAG, "Unhandled 이벤트 발생: $event")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "룸 연결 실패: ${e.message}", e)
            statusText.text = "룸 연결 실패: ${e.message}"
        }
    }

    private fun configureAudioChannelAndMicrophone() {
        try {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.isMicrophoneMute = false
            audioManager.isSpeakerphoneOn = true
        } catch (e: Exception) {
            Log.e(TAG, "오디오 채널 및 마이크 설정 중 오류 발생: ${e.message}", e)
        }
    }

    private suspend fun publishLocalAudioTrack() {
        try {
            // 기본 오디오 트랙 퍼블리싱
            room.localParticipant?.setMicrophoneEnabled(true)
            Log.d(TAG, "로컬 마이크 트랙 퍼블리싱 성공")
        } catch (e: Exception) {
            Log.e(TAG, "로컬 마이크 트랙 퍼블리싱 중 오류 발생: ${e.message}", e)
        }
    }

    private fun handleSubscribedAudioTrack(audioTrack: AudioTrack) {
        try {
            audioTrack.start()
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.isSpeakerphoneOn = true
            Log.d(TAG, "구독된 오디오 트랙 활성화됨")
        } catch (e: Exception) {
            Log.e(TAG, "구독된 오디오 트랙 처리 중 오류 발생: ${e.message}", e)
        }
    }
}
