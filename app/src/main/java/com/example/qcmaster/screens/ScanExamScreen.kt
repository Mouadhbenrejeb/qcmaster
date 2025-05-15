package com.example.qcmaster.screens

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.example.qcmaster.ai.Circle
import com.example.qcmaster.ai.extractAnswersOpenCv
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

    var correctAnswers by remember { mutableStateOf(emptyMap<String, String>()) }
    var circles by remember { mutableStateOf(emptyList<Circle>()) }
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var isScanningCorrect by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var bitmap by remember {
        mutableStateOf<ImageBitmap?>(null)
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    fun getExamAnswers(answerBitmap: Bitmap) {
        scope.launch {
            isLoading = true
            runCatching {

//                extractAnswers(
//                    apiKey = BuildConfig.API_KEY,
//                    answerKeyBitmap = bitmap,
//                )
                extractAnswersOpenCv(
                    answerKeyBitmap = answerBitmap
                )
            }
                .onSuccess { result ->
                    correctAnswers = result.answers
                        .mapKeys { it.toString() }
                        .mapValues { it.toString() }

                    circles = result.circles
                    bitmap = result.bitmap?.asImageBitmap()
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
                        answerBitmap = bitmap,
                    )
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            errorMessage = "❌ Camera cancelled or failed."
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                }

                getExamAnswers(
                    answerBitmap = bitmap,
                )
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "❌ Failed to load selected image."
            }
        } else {
            errorMessage = "❌ Image selection cancelled or failed."
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

        if (correctAnswers.isEmpty()) {
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
                Text("📷 Scan Correct Paper with Camera")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    isScanningCorrect = true
                    imagePickerLauncher.launch("image/*")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🖼️ Pick Image from Gallery")
            }
        } else {
            Text("✅ Correct answers saved:")
//            correctAnswers.forEach { (key, value) ->
//                Text("question number $key: answer number $value")
//            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    correctAnswers = emptyMap()
                    circles = emptyList()
                    bitmap = null
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear answers")
            }
        }

        bitmap?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .drawWithContent {
                        drawContent()

                        val scaleX = size.width / it.width
                        val scaleY = size.height / it.height

                        circles.forEach { circle ->
                            val radius = circle.radius * scaleX
                            val centerX = (circle.center.x * scaleX).toFloat()
                            val centerY = (circle.center.y * scaleY).toFloat()

                            drawCircle(
                                color = Color.Red,
                                radius = radius,
                                center = Offset(
                                    x = centerX,
                                    y = centerY
                                ),
                                style = Stroke(
                                    width = 2.dp.toPx()
                                )
                            )
                        }
                    }
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
