package com.example.qcmaster.screens

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.example.qcmaster.data.FakeExamRepository
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ScanExamScreen(
    navController: NavController,
    examName: String
) {
    val context = LocalContext.current

    var correctAnswers by remember { mutableStateOf<List<String>?>(null) }
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var isScanningCorrect by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && currentPhotoUri != null) {
            val image = InputImage.fromFilePath(context, currentPhotoUri!!)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val answers = visionText.text
                        .split(" ", "\n")
                        .map { it.trim().uppercase() }
                        .filter { it in listOf("A", "B", "C", "D") }

                    if (isScanningCorrect) {
                        correctAnswers = answers
                        FakeExamRepository.saveCorrectAnswers(examName, answers)
                        errorMessage = null
                    } else {
                        val correct = correctAnswers ?: emptyList()
                        navController.currentBackStackEntry?.savedStateHandle?.set("correctAnswers", correct)
                        navController.currentBackStackEntry?.savedStateHandle?.set("scannedAnswers", answers)
                        navController.navigate("correction_comparison_screen")
                    }
                }
                .addOnFailureListener {
                    errorMessage = "❌ Failed to scan the paper. Please try again."
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
