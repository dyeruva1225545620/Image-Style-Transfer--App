package com.example.personalphotostyling.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.personalphotostyling.ui.viewmodels.StyleTransferViewModel
import com.example.personalphotostyling.utils.uriToFile // Import your utility function
import androidx.compose.ui.platform.LocalContext // Ensure this import is present

@Composable
fun HomeScreen(viewModel: StyleTransferViewModel = remember { StyleTransferViewModel() }) {
    var contentUri by remember { mutableStateOf<Uri?>(null) }
    var styleUri by remember { mutableStateOf<Uri?>(null) }

    // Launchers for selecting content and style images
    val contentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        contentUri = uri
        viewModel.contentUri = uri // Update ViewModel
    }

    val styleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        styleUri = uri
        viewModel.styleUri = uri // Update ViewModel
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { contentLauncher.launch("image/*") }) {
            Text("Select Content Image")
        }
        Spacer(modifier = Modifier.height(8.dp))
        ImagePreview(contentUri, "Content Image")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { styleLauncher.launch("image/*") }) {
            Text("Select Style Image")
        }
        Spacer(modifier = Modifier.height(8.dp))
        ImagePreview(styleUri, "Style Image")

        Spacer(modifier = Modifier.height(16.dp))
        val context = LocalContext.current
        Button(
            onClick = {
                if (contentUri != null && styleUri != null) {
                    // Get the current context for file conversion

                    // Convert URIs to Files using the utility function defined in FileUtil.kt
                    val contentFile = uriToFile(context, contentUri!!)
                    val styleFile = uriToFile(context, styleUri!!)

                    // Call the transferStyle method from the ViewModel with the files
                    viewModel.transferStyle(contentFile, styleFile)
                }
            },
            enabled = contentUri != null && styleUri != null
        ) {
            Text("Transfer Style")
        }

        Spacer(modifier = Modifier.height(16.dp))

        viewModel.resultBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(), // Convert Bitmap to ImageBitmap for Compose
                contentDescription = "Result Image",
                modifier = Modifier.fillMaxWidth()
            )
        } ?: run {
            Text(text = "No result image available")
        }

    }
}

@Composable
fun ImagePreview(uri: Uri?, description: String) {
    uri?.let {
        Image(
            painter = rememberAsyncImagePainter(it),
            contentDescription = description,
            modifier = Modifier.size(200.dp)
        )
    }
}
