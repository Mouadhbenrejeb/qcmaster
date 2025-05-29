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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.qcmaster.ai.Circle
import com.example.qcmaster.ai.Square
import com.example.qcmaster.models.Exam
import com.example.qcmaster.ui.theme.QcmasterTheme
import com.example.qcmaster.viewmodels.ScanExamUiState
import com.example.qcmaster.viewmodels.ScanExamViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ScanExamViewModelFactory(private val examId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScanExamViewModel::class.java)) {
            return ScanExamViewModel(examId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanExamScreen(
    navController: NavController,
    examId: String,
) {
    val viewModel: ScanExamViewModel = viewModel(
        factory = ScanExamViewModelFactory(examId)
    )

    val state = viewModel.state
    val context = LocalContext.current
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    ScanExamContent(
        state = state,
        onProcessImage = viewModel.processExamImage,
        onClearResults = viewModel.clearScanResults,
        onBackPressed = { navController.popBackStack() },
        context = context,
        currentPhotoUri = currentPhotoUri,
        onPhotoUriChanged = { currentPhotoUri = it },
        errorMessage = errorMessage,
        onErrorMessageChanged = { errorMessage = it }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanExamContent(
    state: ScanExamUiState,
    onProcessImage: (Bitmap) -> Unit,
    onClearResults: () -> Unit,
    onBackPressed: () -> Unit,
    context: Context,
    currentPhotoUri: Uri?,
    onPhotoUriChanged: (Uri?) -> Unit,
    errorMessage: String?,
    onErrorMessageChanged: (String?) -> Unit
) {
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && currentPhotoUri != null) {
            currentPhotoUri.let { uri ->
                try {
                    val bitmap = if (Build.VERSION.SDK_INT < 28) {
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    } else {
                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                        ImageDecoder.decodeBitmap(source)
                    }
                    onProcessImage(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                    onErrorMessageChanged("❌ Failed to process camera image: ${e.message}")
                }
            }
        } else {
            onErrorMessageChanged("❌ Camera cancelled or failed.")
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                }
                onProcessImage(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                onErrorMessageChanged("❌ Failed to load selected image: ${e.message}")
            }
        } else {
            onErrorMessageChanged("❌ Image selection cancelled or failed.")
        }
    }

    // Function to create a temporary image file for camera
    fun createImageFile(): Uri? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "JPEG_${timeStamp}_"
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val image = File.createTempFile(fileName, ".jpg", storageDir)
            FileProvider.getUriForFile(context, "${context.packageName}.provider", image)
        } catch (e: Exception) {
            onErrorMessageChanged("❌ Failed to create image file: ${e.message}")
            null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Scan Exam: ${state.exam?.name ?: "Loading..."}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (state.isLoading) {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null) {
                // Error state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBackPressed) {
                            Text("Go Back")
                        }
                    }
                }
            } else {
                // Content
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Instructions
                    Text(
                        text = "Scan the answer sheet to extract answers",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (state.correctAnswers.isEmpty()) {
                        // No answers yet, show scan options
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Capture Answer Sheet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        val uri = createImageFile()
                                        if (uri != null) {
                                            onPhotoUriChanged(uri)
                                            cameraLauncher.launch(uri)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("📷 Take Photo with Camera")
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        imagePickerLauncher.launch("image/*")
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Text("🖼️ Select Image from Gallery")
                                }
                            }
                        }
                    } else {
                        // Answers found, show results
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "✅ Answers Detected",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Found ${state.correctAnswers.size} answers",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = onClearResults,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Clear Results")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Show the processed image if available
                    state.bitmap?.let { bitmap ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = "Processed Image",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .drawWithContent {
                                            drawContent()

                                            val scaleX = size.width / bitmap.width
                                            val scaleY = size.height / bitmap.height

                                            state.answers.forEach { shape ->
                                                if (shape is Circle) {
                                                    val radius = shape.radius * scaleX
                                                    val centerX = shape.center.x * scaleX
                                                    val centerY = shape.center.y * scaleY

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

                                                if (shape is Square) {
                                                    drawRect(
                                                        color = Color.Red,
                                                        topLeft = Offset(
                                                            x = shape.topLeft.x * scaleX,
                                                            y = shape.topLeft.y * scaleY
                                                        ),
                                                        size = Size(
                                                            width = shape.size * scaleX,
                                                            height = shape.size * scaleY
                                                        ),
                                                        style = Stroke(
                                                            width = 2.dp.toPx()
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                )

                                // Show processing indicator
                                if (state.isProcessing) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Show error message if any
                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScanExamScreenPreview() {
    QcmasterTheme {
        val dummyState = ScanExamUiState(
            exam = Exam(
                id = "1",
                name = "Math Exam",
                assignedClasses = listOf("Computer Science", "Mathematics")
            ),
            isLoading = false,
            isProcessing = false,
            error = null,
            correctAnswers = emptyMap()
        )

        ScanExamContent(
            state = dummyState,
            onProcessImage = {},
            onClearResults = {},
            onBackPressed = {},
            context = LocalContext.current,
            currentPhotoUri = null,
            onPhotoUriChanged = {},
            errorMessage = null,
            onErrorMessageChanged = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ScanExamScreenWithAnswersPreview() {
    QcmasterTheme {
        val dummyState = ScanExamUiState(
            exam = Exam(
                id = "1",
                name = "Math Exam",
                assignedClasses = listOf("Computer Science", "Mathematics")
            ),
            isLoading = false,
            isProcessing = false,
            error = null,
            correctAnswers = mapOf("1" to "A", "2" to "B", "3" to "C")
        )

        ScanExamContent(
            state = dummyState,
            onProcessImage = {},
            onClearResults = {},
            onBackPressed = {},
            context = LocalContext.current,
            currentPhotoUri = null,
            onPhotoUriChanged = {},
            errorMessage = null,
            onErrorMessageChanged = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ScanExamScreenLoadingPreview() {
    QcmasterTheme {
        val dummyState = ScanExamUiState(
            isLoading = true
        )

        ScanExamContent(
            state = dummyState,
            onProcessImage = {},
            onClearResults = {},
            onBackPressed = {},
            context = LocalContext.current,
            currentPhotoUri = null,
            onPhotoUriChanged = {},
            errorMessage = null,
            onErrorMessageChanged = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ScanExamScreenErrorPreview() {
    QcmasterTheme {
        val dummyState = ScanExamUiState(
            isLoading = false,
            error = "Failed to load exam details"
        )

        ScanExamContent(
            state = dummyState,
            onProcessImage = {},
            onClearResults = {},
            onBackPressed = {},
            context = LocalContext.current,
            currentPhotoUri = null,
            onPhotoUriChanged = {},
            errorMessage = null,
            onErrorMessageChanged = {}
        )
    }
}
