package com.example.personalphotostyling.data

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("stylize")
    suspend fun transferStyle(
        @Part content: MultipartBody.Part,
        @Part style: MultipartBody.Part
    ): Response<ResponseBody>
}

