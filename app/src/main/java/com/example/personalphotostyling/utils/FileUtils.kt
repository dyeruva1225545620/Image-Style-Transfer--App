package com.example.personalphotostyling.utils

import android.net.Uri
import android.content.Context
import java.io.File
import java.io.FileOutputStream

fun uriToFile(context: Context, uri: Uri): File {
    // Create a temporary file in cache directory
    val file = File.createTempFile("temp_${System.currentTimeMillis()}", ".jpg", context.cacheDir)

    // Copy the input stream to the file
    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }

    return file
}
