package com.example.personalphotostyling.ui.viewmodels

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalphotostyling.data.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import org.json.JSONObject


class StyleTransferViewModel : ViewModel() {
    var contentUri by mutableStateOf<Uri?>(null)
    var styleUri by mutableStateOf<Uri?>(null)
    var resultBitmap by mutableStateOf<Bitmap?>(null) // Change to Bitmap for direct usage

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://image-style-transfer-447522.wl.r.appspot.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    fun transferStyle(contentFile: File, styleFile: File) {
        viewModelScope.launch {
            val contentRequestBody = contentFile.asRequestBody("image/*".toMediaTypeOrNull())
            val styleRequestBody = styleFile.asRequestBody("image/*".toMediaTypeOrNull())

            val contentPart = MultipartBody.Part.createFormData("content_image", contentFile.name, contentRequestBody)
            val stylePart = MultipartBody.Part.createFormData("style_image", styleFile.name, styleRequestBody)

            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.transferStyle(contentPart, stylePart)
                }
                if (response.isSuccessful) {
                    Log.d("StyleTransfer", "API call successful")
                    val responseBody = response.body()?.string()

                    // Extract the base64 string from the JSON response
                    val stylizedImageBase64 = JSONObject(responseBody).getString("stylized_image")

                    // Decode the base64 string to a Bitmap
                    val decodedBytes = Base64.decode(stylizedImageBase64, Base64.DEFAULT)
                    resultBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                    Log.d("StyleTransfer", "Stylized image decoded successfully")
                } else {
                    Log.e("StyleTransfer", "API call failed: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("StyleTransfer", "Network error: ${e.message}", e)
            }
        }
    }
}
