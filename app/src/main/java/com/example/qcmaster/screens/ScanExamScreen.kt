package com.example.qcmaster.screens

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.example.qcmaster.ai.extractAnswers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


@Composable
fun ScanExamScreen(
    navController: NavController,
    examName: String,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var correctAnswers by remember { mutableStateOf<List<String>?>(null) }
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var isScanningCorrect by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var bitmap by remember {
        mutableStateOf<ImageBitmap?>(null)
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    fun getExamAnswers(bitmap: Bitmap) {
        scope.launch {
            isLoading = true
            runCatching {
                extractAnswers(
                    apiKey = "",
                    answerKeyBitmap = bitmap,
                )
            }
                .onSuccess { answers ->
                    println("Answers: $answers")
                }
                .onFailure {
                    it.printStackTrace()
                }

            isLoading = false

        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && currentPhotoUri != null) {
            currentPhotoUri?.let { uri ->
                try {
                    val bitmap = if (Build.VERSION.SDK_INT < 28) {
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    } else {
                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                        ImageDecoder.decodeBitmap(source)
                    }

                    getExamAnswers(
                        bitmap = bitmap,
                    )
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            errorMessage = "❌ Camera cancelled or failed."
        }
    }

    fun createImageFile(context: Context): Uri? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "JPEG_${timeStamp}_"
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val image = File.createTempFile(fileName, ".jpg", storageDir)
            FileProvider.getUriForFile(context, "${context.packageName}.provider", image)
        } catch (e: Exception) {
            null
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("📄 Scanning for Exam: $examName", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))

        if (correctAnswers == null) {
            Button(
                onClick = {
                    isScanningCorrect = true
                    val uri = createImageFile(context)
                    if (uri != null) {
                        currentPhotoUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        errorMessage = "❌ Failed to create image file."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📷 Scan Correct Paper")
            }
        } else {
            Text("✅ Correct answers saved: ${correctAnswers!!.joinToString()}")
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    isScanningCorrect = false
                    val uri = createImageFile(context)
                    if (uri != null) {
                        currentPhotoUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        errorMessage = "❌ Failed to create image file."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("👨‍🎓 Scan Student Paper")
            }
        }

        bitmap?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }

        if (isLoading) {
            CircularProgressIndicator()
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
            Text("⬅ Back to Exam List")
        }
    }
}
