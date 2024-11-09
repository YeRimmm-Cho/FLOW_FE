package com.example.martfia

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import okhttp3.MediaType
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

    // IngredientRecognitionService 객체 생성
    private val ingredientService: IngredientRecognitionService by lazy {
        MartfiaRetrofitClient.createService(IngredientRecognitionService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_upload)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        val uploadContainer = findViewById<ImageView>(R.id.uploadContainer)
        uploadContainer.setOnClickListener {
            checkPermissions()
        }

        val recognizeButton = findViewById<Button>(R.id.recognizeButton)
        recognizeButton.setOnClickListener {
            if (selectedImageUri != null) {
                uploadImageAndRecognizeIngredients()
            } else {
                Toast.makeText(this, "먼저 이미지를 업로드하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissions() {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val audioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)

        if (cameraPermission == PackageManager.PERMISSION_GRANTED && audioPermission == PackageManager.PERMISSION_GRANTED) {
            showImagePickerDialog()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                REQUEST_PERMISSIONS
            )
        }
    }

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
                    val uploadContainer = findViewById<ImageView>(R.id.uploadContainer)
                    uploadContainer.setImageURI(selectedImageUri)
                }
            }
        }
    }

    // 이미지 파일을 서버에 업로드하고 재료를 인식하는 함수
    private fun uploadImageAndRecognizeIngredients() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "이미지를 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val imageFile = File(selectedImageUri!!.path!!)
        val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), imageFile)
        val multipartBody = MultipartBody.Part.createFormData("image", imageFile.name, requestBody)

        // API 호출
        ingredientService.uploadImage(multipartBody).enqueue(object : Callback<IngredientRecognitionResponse> {
            override fun onResponse(call: Call<IngredientRecognitionResponse>, response: Response<IngredientRecognitionResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val ingredients = ArrayList(response.body()!!.ingredients)
                    val intent = Intent(this@UploadActivity, CheckIngredientActivity::class.java)
                    intent.putStringArrayListExtra("ingredient_list", ingredients)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@UploadActivity, "재료 인식에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<IngredientRecognitionResponse>, t: Throwable) {
                Toast.makeText(this@UploadActivity, "서버와의 통신에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
