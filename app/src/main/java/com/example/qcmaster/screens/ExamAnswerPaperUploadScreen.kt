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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.qcmaster.Route
import com.example.qcmaster.models.Exam
import com.example.qcmaster.ui.theme.QcmasterTheme
import com.example.qcmaster.viewmodels.ExamAnswerPaperUploadViewModel
import com.example.qcmaster.viewmodels.ExamAnswerPaperUploadUiState
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ExamAnswerPaperUploadViewModelFactory(private val examId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExamAnswerPaperUploadViewModel::class.java)) {
            return ExamAnswerPaperUploadViewModel(examId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamAnswerPaperUploadScreen(
    navController: NavController,
    examId: String
) {
    val viewModel: ExamAnswerPaperUploadViewModel = viewModel(
        factory = ExamAnswerPaperUploadViewModelFactory(examId)
    )

    val state = viewModel.state
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }

    ExamAnswerPaperUploadContent(
        state = state,
        onBackPressed = {
            navController.popBackStack()
        },
        onUploadAnswerPaper = viewModel::uploadAnswerPaper,
        onResetUpload = viewModel::resetUpload,
        onAnswerPaperUriChanged = viewModel::onAnswerPaperUriChanged,
        onAnswerPaperBitmapChanged = viewModel::onAnswerPaperBitmapChanged,
        context = context,
        errorMessage = errorMessage,
        onErrorMessageChanged = { errorMessage = it },
        onContinueToClassSelection = {
            navController.navigate(Route.ExamCorrectionClassSelectionScreen(examId).route) {
                popUpTo(Route.ExamAnswerPaperUploadScreen.route) { inclusive = true }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamAnswerPaperUploadContent(
    state: ExamAnswerPaperUploadUiState,
    onBackPressed: () -> Unit,
    onUploadAnswerPaper: () -> Unit,
    onResetUpload: () -> Unit,
    onAnswerPaperUriChanged: (Uri?) -> Unit,
    onAnswerPaperBitmapChanged: (Bitmap?) -> Unit,
    context: Context,
    errorMessage: String?,
    onErrorMessageChanged: (String?) -> Unit,
    onContinueToClassSelection: () -> Unit
) {
    // Camera launcher for answer paper
    val answerPaperCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && state.answerPaperUri != null) {
            state.answerPaperUri.let { uri ->
                try {
                    val bitmap = if (Build.VERSION.SDK_INT < 28) {
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    } else {
                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                        ImageDecoder.decodeBitmap(source)
                    }
                    onAnswerPaperBitmapChanged(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                    onErrorMessageChanged("❌ Failed to process camera image: ${e.message}")
                }
            }
        } else {
            onErrorMessageChanged("❌ Camera cancelled or failed.")
        }
    }

    // Image picker launcher for answer paper
    val answerPaperPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            onAnswerPaperUriChanged(uri)
            try {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                }
                onAnswerPaperBitmapChanged(bitmap)
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
                        text = "Upload Answer Paper: ${state.exam?.name ?: "Exam"}",
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
            } else if (state.uploadSuccess) {
                // Upload success state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Upload Successful",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "The answer paper has been uploaded successfully.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onContinueToClassSelection,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Continue to Class Selection")
                        }
                    }
                }
            } else {
                // Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Exam info
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Exam Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Name: ${state.exam?.name ?: "N/A"}",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = "Classes: ${state.exam?.assignedClasses?.joinToString(", ") ?: "None"}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Instructions
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Instructions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Upload the answer paper for this exam. This will be used to correct all student exams.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Answer Paper Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Answer Paper",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (state.answerPaperBitmap != null) {
                                // Show the selected image
                                Image(
                                    bitmap = state.answerPaperBitmap.asImageBitmap(),
                                    contentDescription = "Answer Paper",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Fit
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = { onAnswerPaperBitmapChanged(null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Remove Image")
                                }
                            } else {
                                // Show options to select an image
                                Button(
                                    onClick = {
                                        val uri = createImageFile()
                                        if (uri != null) {
                                            onAnswerPaperUriChanged(uri)
                                            answerPaperCameraLauncher.launch(uri)
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
                                        answerPaperPickerLauncher.launch("image/*")
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
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Upload button
                    Button(
                        onClick = onUploadAnswerPaper,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.answerPaperBitmap != null && !state.isUploading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (state.isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Upload Answer Paper")
                    }

                    // Error message
                    if (state.uploadError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.uploadError,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    // Other error message
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExamAnswerPaperUploadLoadingPreview() {
    QcmasterTheme {
        val dummyState = ExamAnswerPaperUploadUiState(
            isLoading = true,
            error = null
        )

        ExamAnswerPaperUploadContent(
            state = dummyState,
            onBackPressed = {},
            onUploadAnswerPaper = {},
            onResetUpload = {},
            onAnswerPaperUriChanged = {},
            onAnswerPaperBitmapChanged = {},
            context = LocalContext.current,
            errorMessage = null,
            onErrorMessageChanged = {},
            onContinueToClassSelection = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExamAnswerPaperUploadErrorPreview() {
    QcmasterTheme {
        val dummyState = ExamAnswerPaperUploadUiState(
            isLoading = false,
            error = "Failed to load exam details. Please try again."
        )

        ExamAnswerPaperUploadContent(
            state = dummyState,
            onBackPressed = {},
            onUploadAnswerPaper = {},
            onResetUpload = {},
            onAnswerPaperUriChanged = {},
            onAnswerPaperBitmapChanged = {},
            context = LocalContext.current,
            errorMessage = null,
            onErrorMessageChanged = {},
            onContinueToClassSelection = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExamAnswerPaperUploadSuccessPreview() {
    QcmasterTheme {
        val dummyState = ExamAnswerPaperUploadUiState(
            exam = Exam(
                id = "1",
                name = "Math Exam",
                assignedClasses = listOf("Computer Science")
            ),
            isLoading = false,
            error = null,
            uploadSuccess = true
        )

        ExamAnswerPaperUploadContent(
            state = dummyState,
            onBackPressed = {},
            onUploadAnswerPaper = {},
            onResetUpload = {},
            onAnswerPaperUriChanged = {},
            onAnswerPaperBitmapChanged = {},
            context = LocalContext.current,
            errorMessage = null,
            onErrorMessageChanged = {},
            onContinueToClassSelection = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExamAnswerPaperUploadContentPreview() {
    QcmasterTheme {
        val dummyState = ExamAnswerPaperUploadUiState(
            exam = Exam(
                id = "1",
                name = "Math Exam",
                assignedClasses = listOf("Computer Science")
            ),
            isLoading = false,
            error = null
        )

        ExamAnswerPaperUploadContent(
            state = dummyState,
            onBackPressed = {},
            onUploadAnswerPaper = {},
            onResetUpload = {},
            onAnswerPaperUriChanged = {},
            onAnswerPaperBitmapChanged = {},
            context = LocalContext.current,
            errorMessage = null,
            onErrorMessageChanged = {},
            onContinueToClassSelection = {}
        )
    }
}