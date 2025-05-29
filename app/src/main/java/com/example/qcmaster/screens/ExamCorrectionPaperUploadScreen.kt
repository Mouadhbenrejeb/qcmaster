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
import com.example.qcmaster.Routes
import com.example.qcmaster.models.Exam
import com.example.qcmaster.models.Student
import com.example.qcmaster.ui.theme.QcmasterTheme
import com.example.qcmaster.viewmodels.ExamCorrectionPaperUploadViewModel
import com.example.qcmaster.viewmodels.ExamCorrectionPaperUploadUiState
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ExamCorrectionPaperUploadViewModelFactory(
    private val examId: String,
    private val className: String,
    private val studentId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExamCorrectionPaperUploadViewModel::class.java)) {
            return ExamCorrectionPaperUploadViewModel(examId, className, studentId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamCorrectionPaperUploadScreen(
    navController: NavController,
    examId: String,
    className: String,
    studentId: String
) {
    val viewModel: ExamCorrectionPaperUploadViewModel = viewModel(
        factory = ExamCorrectionPaperUploadViewModelFactory(examId, className, studentId)
    )

    val state = viewModel.state
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }

    ExamCorrectionPaperUploadContent(
        state = state,
        onBackPressed = {
            navController.popBackStack()
        },
        onUploadPapers = viewModel::uploadPapers,
        onResetUpload = viewModel::resetUpload,
        onExamPaperUriChanged = viewModel::onExamPaperUriChanged,
        onExamPaperBitmapChanged = viewModel::onExamPaperBitmapChanged,
        context = context,
        errorMessage = errorMessage,
        onErrorMessageChanged = { errorMessage = it },
        navController = navController,
        examId = examId,
        className = className,
        studentId = studentId
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamCorrectionPaperUploadContent(
    state: ExamCorrectionPaperUploadUiState,
    onBackPressed: () -> Unit,
    onUploadPapers: () -> Unit,
    onResetUpload: () -> Unit,
    onExamPaperUriChanged: (Uri?) -> Unit,
    onExamPaperBitmapChanged: (Bitmap?) -> Unit,
    context: Context,
    errorMessage: String?,
    onErrorMessageChanged: (String?) -> Unit,
    navController: NavController? = null,
    examId: String? = null,
    className: String? = null,
    studentId: String? = null
) {
    // Camera launcher for exam paper
    val examPaperCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && state.examPaperUri != null) {
            state.examPaperUri.let { uri ->
                try {
                    val bitmap = if (Build.VERSION.SDK_INT < 28) {
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    } else {
                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                        ImageDecoder.decodeBitmap(source)
                    }
                    onExamPaperBitmapChanged(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                    onErrorMessageChanged("❌ Failed to process camera image: ${e.message}")
                }
            }
        } else {
            onErrorMessageChanged("❌ Camera cancelled or failed.")
        }
    }

    // Image picker launcher for exam paper
    val examPaperPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            onExamPaperUriChanged(uri)
            try {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                }
                onExamPaperBitmapChanged(bitmap)
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
                        text = "Upload Exam Paper: ${state.student?.name ?: "Student"}",
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
                            text = "The exam paper has been uploaded successfully.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { 
                                    if (navController != null && examId != null && className != null && studentId != null) {
                                        navController.navigate(
                                            Routes.CorrectionComparison.createRoute(
                                                examId, 
                                                className, 
                                                studentId
                                            )
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                enabled = navController != null && examId != null && className != null && studentId != null
                            ) {
                                Text("View Correction Results")
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Button(
                                    onClick = onResetUpload,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Text("Upload Another")
                                }

                                Button(
                                    onClick = onBackPressed,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary
                                    )
                                ) {
                                    Text("Go Back")
                                }
                            }
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
                    // Student info
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
                                text = "Student Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Name: ${state.student?.name ?: "N/A"}",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = "CIN: ${state.student?.cin ?: "N/A"}",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = "Class: ${state.className}",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = "Exam: ${state.exam?.name ?: "N/A"}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Exam Paper Section
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
                                text = "Exam Paper",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (state.examPaperBitmap != null) {
                                // Show the selected image
                                Image(
                                    bitmap = state.examPaperBitmap.asImageBitmap(),
                                    contentDescription = "Exam Paper",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Fit
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = { onExamPaperBitmapChanged(null) },
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
                                            onExamPaperUriChanged(uri)
                                            examPaperCameraLauncher.launch(uri)
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
                                        examPaperPickerLauncher.launch("image/*")
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
                        onClick = onUploadPapers,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.examPaperBitmap != null && !state.isUploading,
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
                        Text("Upload Exam Paper")
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
fun ExamCorrectionPaperUploadLoadingPreview() {
    QcmasterTheme {
        val dummyState = ExamCorrectionPaperUploadUiState(
            className = "Computer Science",
            isLoading = true,
            error = null
        )

        ExamCorrectionPaperUploadContent(
            state = dummyState,
            onBackPressed = {},
            onUploadPapers = {},
            onResetUpload = {},
            onExamPaperUriChanged = {},
            onExamPaperBitmapChanged = {},
            context = LocalContext.current,
            errorMessage = null,
            onErrorMessageChanged = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExamCorrectionPaperUploadErrorPreview() {
    QcmasterTheme {
        val dummyState = ExamCorrectionPaperUploadUiState(
            className = "Computer Science",
            isLoading = false,
            error = "Failed to load student details. Please try again."
        )

        ExamCorrectionPaperUploadContent(
            state = dummyState,
            onBackPressed = {},
            onUploadPapers = {},
            onResetUpload = {},
            onExamPaperUriChanged = {},
            onExamPaperBitmapChanged = {},
            context = LocalContext.current,
            errorMessage = null,
            onErrorMessageChanged = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExamCorrectionPaperUploadSuccessPreview() {
    QcmasterTheme {
        val dummyState = ExamCorrectionPaperUploadUiState(
            exam = Exam(
                id = "1",
                name = "Math Exam",
                assignedClasses = listOf("Computer Science")
            ),
            student = Student(
                name = "John Doe",
                cin = "12345678",
                assignedClass = "Computer Science"
            ),
            className = "Computer Science",
            isLoading = false,
            error = null,
            uploadSuccess = true
        )

        ExamCorrectionPaperUploadContent(
            state = dummyState,
            onBackPressed = {},
            onUploadPapers = {},
            onResetUpload = {},
            onExamPaperUriChanged = {},
            onExamPaperBitmapChanged = {},
            context = LocalContext.current,
            errorMessage = null,
            onErrorMessageChanged = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExamCorrectionPaperUploadContentPreview() {
    QcmasterTheme {
        val dummyState = ExamCorrectionPaperUploadUiState(
            exam = Exam(
                id = "1",
                name = "Math Exam",
                assignedClasses = listOf("Computer Science")
            ),
            student = Student(
                name = "John Doe",
                cin = "12345678",
                assignedClass = "Computer Science"
            ),
            className = "Computer Science",
            isLoading = false,
            error = null
        )

        ExamCorrectionPaperUploadContent(
            state = dummyState,
            onBackPressed = {},
            onUploadPapers = {},
            onResetUpload = {},
            onExamPaperUriChanged = {},
            onExamPaperBitmapChanged = {},
            context = LocalContext.current,
            errorMessage = null,
            onErrorMessageChanged = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExamCorrectionPaperUploadUploadingPreview() {
    QcmasterTheme {
        val dummyState = ExamCorrectionPaperUploadUiState(
            exam = Exam(
                id = "1",
                name = "Math Exam",
                assignedClasses = listOf("Computer Science")
            ),
            student = Student(
                name = "John Doe",
                cin = "12345678",
                assignedClass = "Computer Science"
            ),
            className = "Computer Science",
            isLoading = false,
            error = null,
            isUploading = true
        )

        ExamCorrectionPaperUploadContent(
            state = dummyState,
            onBackPressed = {},
            onUploadPapers = {},
            onResetUpload = {},
            onExamPaperUriChanged = {},
            onExamPaperBitmapChanged = {},
            context = LocalContext.current,
            errorMessage = null,
            onErrorMessageChanged = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExamCorrectionPaperUploadErrorMessagePreview() {
    QcmasterTheme {
        val dummyState = ExamCorrectionPaperUploadUiState(
            exam = Exam(
                id = "1",
                name = "Math Exam",
                assignedClasses = listOf("Computer Science")
            ),
            student = Student(
                name = "John Doe",
                cin = "12345678",
                assignedClass = "Computer Science"
            ),
            className = "Computer Science",
            isLoading = false,
            error = null,
            uploadError = "Failed to upload paper. Please try again."
        )

        ExamCorrectionPaperUploadContent(
            state = dummyState,
            onBackPressed = {},
            onUploadPapers = {},
            onResetUpload = {},
            onExamPaperUriChanged = {},
            onExamPaperBitmapChanged = {},
            context = LocalContext.current,
            errorMessage = "Camera cancelled or failed.",
            onErrorMessageChanged = {}
        )
    }
}
