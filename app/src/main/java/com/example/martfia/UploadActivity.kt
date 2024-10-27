package com.example.martfia

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class UploadActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_IMAGE_PICK = 2
        const val REQUEST_PERMISSIONS = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_upload)

        // 뒤로 가기 버튼 설정
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        // 업로드 컨테이너 클릭 시 사진 찍기 또는 앨범 접근
        val uploadContainer = findViewById<ImageView>(R.id.uploadContainer)
        uploadContainer.setOnClickListener {
            checkPermissions() // 권한 체크 후 실행
        }
    }

    // 권한이 부여되었는지 확인
    private fun checkPermissions() {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val audioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)

        if (cameraPermission == PackageManager.PERMISSION_GRANTED && audioPermission == PackageManager.PERMISSION_GRANTED) {
            showImagePickerDialog()
        } else {
            // 권한이 없다면 요청하기
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                REQUEST_PERMISSIONS)
        }
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                showImagePickerDialog()
            } else {
                Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 이미지 선택 다이얼로그 표시
    private fun showImagePickerDialog() {
        val options = arrayOf("사진 찍기", "앨범에서 선택")
        android.app.AlertDialog.Builder(this)
            .setTitle("사진 업로드")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera() // 사진 찍기
                    1 -> openGallery() // 앨범에서 선택
                }
            }
            .show()
    }

    // 카메라 열기
    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } else {
            Toast.makeText(this, "카메라를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 갤러리 열기
    private fun openGallery() {
        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    // 사진을 찍은 경우 처리
                    val imageBitmap = data?.extras?.get("data") as? android.graphics.Bitmap
                    findViewById<ImageView>(R.id.uploadContainer).setImageBitmap(imageBitmap)
                }
                REQUEST_IMAGE_PICK -> {
                    // 앨범에서 선택한 경우 처리
                    val selectedImageUri = data?.data
                    findViewById<ImageView>(R.id.uploadContainer).setImageURI(selectedImageUri)
                }
            }
        }
    }
}
