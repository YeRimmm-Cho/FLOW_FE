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
import com.example.martfia.model.request.ImageUploadRequest
import com.example.martfia.model.request.ReceiptUploadRequest
import com.example.martfia.model.response.ImageUploadResponse
import com.example.martfia.model.Ingredient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UploadActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_IMAGE_PICK = 2
        const val REQUEST_PERMISSIONS = 3
    }

    private var selectedImageUri: Uri? = null
    private var uploadType: String? = null // "food" 또는 "receipt"
    private val ingredientService: IngredientRecognitionService by lazy {
        MartfiaRetrofitClient.createService(IngredientRecognitionService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_upload)

        uploadType = intent.getStringExtra("uploadType")
        Log.d("UploadActivity", "Upload type: $uploadType")

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        val uploadContainer = findViewById<ImageView>(R.id.uploadContainer)
        uploadContainer.setOnClickListener {
            checkPermissions()
        }

        val recognizeButton = findViewById<Button>(R.id.recognizeButton)
        recognizeButton.setOnClickListener {
            if (selectedImageUri != null) {
                recognizeIngredients() // API 호출
            } else {
                Toast.makeText(this, "먼저 이미지를 업로드하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissions() {
        val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            showImagePickerDialog()
        } else {
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

        val imageUrl = selectedImageUri.toString() // URI를 문자열로 변환

        when (uploadType) {
            "food" -> {
                val request = ImageUploadRequest(image_url = imageUrl)
                ingredientService.uploadImage(request).enqueue(object : Callback<ImageUploadResponse> {
                    override fun onResponse(call: Call<ImageUploadResponse>, response: Response<ImageUploadResponse>) {
                        handleApiResponse(response)
                    }

                    override fun onFailure(call: Call<ImageUploadResponse>, t: Throwable) {
                        handleApiFailure(t)
                    }
                })
            }

            "receipt" -> {
                val request = ReceiptUploadRequest(receipt_url = imageUrl)
                ingredientService.uploadReceipt(request).enqueue(object : Callback<ImageUploadResponse> {
                    override fun onResponse(call: Call<ImageUploadResponse>, response: Response<ImageUploadResponse>) {
                        handleApiResponse(response)
                    }

                    override fun onFailure(call: Call<ImageUploadResponse>, t: Throwable) {
                        handleApiFailure(t)
                    }
                })
            }
        }
    }

    private fun handleApiResponse(response: Response<ImageUploadResponse>) {
        if (response.isSuccessful && response.body() != null) {
            val savedIngredients = response.body()!!.saved_ingredients.map {
                Ingredient(
                    image_url = it.image_url,
                    name = it.name
                )
            }
            val intent = Intent(this, CheckIngredientActivity::class.java).apply {
                putParcelableArrayListExtra("saved_ingredients", ArrayList(savedIngredients))
            }
            startActivity(intent)
        } else {
            Log.e("UploadActivity", "Error: ${response.errorBody()?.string()}")
            Toast.makeText(this, "재료 인식에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleApiFailure(t: Throwable) {
        Log.e("UploadActivity", "Server communication failed", t)
        Toast.makeText(this, "서버와 통신에 실패했습니다.", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            showImagePickerDialog()
        } else {
            Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }
}
