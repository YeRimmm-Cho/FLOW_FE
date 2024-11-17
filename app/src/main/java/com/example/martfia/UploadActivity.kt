package com.example.martfia

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.martfia.service.MartfiaRetrofitClient
import com.example.martfia.service.IngredientRecognitionService
import com.example.martfia.model.response.IngredientRecognitionResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class UploadActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_IMAGE_PICK = 2
        const val REQUEST_PERMISSIONS = 3
    }

    private var selectedImageUri: Uri? = null
    private var uploadType: String? = null // "food (재료)" 또는 "receipt (온라인 영수증)" 값 저장

    private val ingredientService: IngredientRecognitionService by lazy {
        MartfiaRetrofitClient.createService(IngredientRecognitionService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_upload)

        // 전달받은 업로드 타입 설정
        uploadType = intent.getStringExtra("uploadType")
        Log.d("UploadActivity", "Upload type: $uploadType")

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        val uploadContainer = findViewById<ImageView>(R.id.uploadContainer)
        uploadContainer.setOnClickListener {
            Log.d("UploadActivity", "UploadContainer clicked")
            checkPermissions()
        }

        val recognizeButton = findViewById<Button>(R.id.recognizeButton)
        recognizeButton.setOnClickListener {
            if (selectedImageUri != null) {
                Log.d("UploadActivity", "Recognizing ingredients...")
                recognizeIngredients() // 재료 인식 API (인식된 재료 업데이트) 호출
            } else {
                Toast.makeText(this, "먼저 이미지를 업로드하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissions() {
        val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            Log.d("UploadActivity", "Permissions already granted")
            showImagePickerDialog()
        } else {
            Log.d("UploadActivity", "Requesting permissions")
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS)
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("사진 찍기", "앨범에서 선택")
        android.app.AlertDialog.Builder(this)
            .setTitle("사진 업로드")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } else {
            Toast.makeText(this, "카메라를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE, REQUEST_IMAGE_PICK -> {
                    selectedImageUri = data?.data
                    Log.d("UploadActivity", "Image selected: $selectedImageUri")
                    findViewById<ImageView>(R.id.uploadContainer).setImageURI(selectedImageUri)
                }
            }
        }
    }

    private fun recognizeIngredients() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "이미지를 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val filePath = selectedImageUri?.let { uriToFilePath(it) }
        if (filePath == null) {
            Toast.makeText(this, "이미지 경로를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val imageFile = File(filePath)
        val photo = MultipartBody.Part.createFormData(
            "photo",
            imageFile.name,
            RequestBody.create("image/*".toMediaTypeOrNull(), imageFile)
        )

        // 인식된 재료 업데이트 API 호출
        ingredientService.recognizeIngredients(photo).enqueue(object : Callback<IngredientRecognitionResponse> {
            override fun onResponse(call: Call<IngredientRecognitionResponse>, response: Response<IngredientRecognitionResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val ingredients = ArrayList(response.body()!!.ingredients)
                    Log.d("UploadActivity", "Ingredients recognized: $ingredients")

                    // 재료 리스트를 전달하며 다음 화면인 CheckIngredientActivity로 이동
                    val intent = Intent(this@UploadActivity, CheckIngredientActivity::class.java)
                    intent.putStringArrayListExtra("ingredient_list", ingredients)
                    startActivity(intent)
                } else {
                    Log.d("UploadActivity", "Failed to recognize ingredients: ${response.errorBody()?.string()}")
                    Toast.makeText(this@UploadActivity, "재료 인식에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<IngredientRecognitionResponse>, t: Throwable) {
                Log.e("UploadActivity", "Server communication failed", t)
                Toast.makeText(this@UploadActivity, "서버와 통신에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun uriToFilePath(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
            }
        }
        return null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            Log.d("UploadActivity", "Permissions granted")
            showImagePickerDialog()
        } else {
            Log.d("UploadActivity", "Permissions denied")
            Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }
}
