package com.example.qcmaster.screens

import android.R.attr.centerX
import android.R.attr.centerY
import android.R.attr.radius
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
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
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.navigation.NavController
import com.example.qcmaster.ai.AnswerRow
import com.example.qcmaster.ai.Circle
import com.example.qcmaster.ai.Shape
import com.example.qcmaster.ai.Square
import com.example.qcmaster.ai.extractAnswersOpenCv
import com.example.qcmaster.ui.theme.QcmasterTheme

class DebugViewModel : ViewModel() {
    var bitmap by mutableStateOf<Bitmap?>(null)
    var imageBitmap by mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null)
    var answers by mutableStateOf<List<AnswerRow>>(emptyList())
    var isProcessing by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    fun processImage(bitmap: Bitmap) {
        this.bitmap = bitmap
        this.imageBitmap = bitmap.asImageBitmap()
        isProcessing = true
        error = null

        // Process the image in a coroutine
        viewModelScope.launch {
            try {
                // Call the suspend function within the coroutine
                val result = withContext(Dispatchers.Default) {
                    extractAnswersOpenCv(answerKeyBitmap = bitmap)
                }
                println("result bitmap size: ${result.bitmap?.width}x${result.bitmap?.height}")
                println("Results: ${result.answers}")
                answers = result.answers
                imageBitmap = result.bitmap?.asImageBitmap()
                isProcessing = false
            } catch (e: Exception) {
                error = "Error processing image: ${e.message}"
                isProcessing = false
            }
        }
    }

    fun clearResults() {
        answers = emptyList()
        error = null
    }
}

@Composable
fun DebugScreen(navController: NavController) {
    val viewModel: DebugViewModel = viewModel()
    val context = LocalContext.current
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    DebugScreenContent(
        bitmap = viewModel.imageBitmap,
        answers = viewModel.answers,
        isProcessing = viewModel.isProcessing,
        error = viewModel.error,
        onProcessImage = viewModel::processImage,
        onClearResults = viewModel::clearResults,
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
fun DebugScreenContent(
    bitmap: androidx.compose.ui.graphics.ImageBitmap?,
    answers: List<AnswerRow>,
    isProcessing: Boolean,
    error: String?,
    onProcessImage: (Bitmap) -> Unit,
    onClearResults: () -> Unit,
    onBackPressed: () -> Unit,
    context: Context,
    currentPhotoUri: Uri?,
    onPhotoUriChanged: (Uri?) -> Unit,
    errorMessage: String?,
    onErrorMessageChanged: (String?) -> Unit
) {
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }
                onProcessImage(bitmap)
            } catch (e: Exception) {
                onErrorMessageChanged("Error loading image: ${e.message}")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debug Image Detection") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Buttons for selecting image
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Select Image")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onClearResults,
                    modifier = Modifier.weight(1f),
                    enabled = answers.isNotEmpty()
                ) {
                    Text("Clear Results")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Show the processed image if available
            bitmap?.let { img ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = img,
                            contentDescription = "Processed Image",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .drawWithContent {
                                    drawContent()

                                    val scaleX = size.width / img.width
                                    val scaleY = size.height / img.height

                                    val colors = listOf(
                                        Color.Red,
                                        Color.Blue,
                                        Color.Green,
                                        Color.Yellow,
                                        Color.Magenta,
                                        Color.Cyan
                                    )

                                    answers.forEachIndexed { index, row ->
                                        val color = colors[index % colors.size]

                                        row.shapes.forEachIndexed { index, shape ->
                                            if (shape is Circle) {
                                                val radius = shape.radius * scaleX
                                                val centerX = shape.center.x * scaleX
                                                val centerY = shape.center.y * scaleY

                                                drawCircle(
                                                    color = color,
                                                    radius = radius,
                                                    center = Offset(
                                                        x = centerX,
                                                        y = centerY
                                                    ),
                                                    style = Stroke(
                                                        width = 1.dp.toPx()
                                                    )
                                                )

                                                if (index == row.answer) {
                                                    drawCircle(
                                                        color = color,
                                                        radius = radius,
                                                        center = Offset(
                                                            x = centerX,
                                                            y = centerY
                                                        ),
                                                    )
                                                }
                                            }

                                            if (shape is Square) {
                                                drawRect(
                                                    color = color,
                                                    topLeft = Offset(
                                                        x = shape.topLeft.x * scaleX,
                                                        y = shape.topLeft.y * scaleY
                                                    ),
                                                    size = Size(
                                                        width = shape.size * scaleX,
                                                        height = shape.size * scaleY
                                                    ),
                                                    style = Stroke(
                                                        width = 1.dp.toPx()
                                                    )
                                                )

                                                if (index == row.answer) {
                                                    drawRect(
                                                        color = color,
                                                        topLeft = Offset(
                                                            x = shape.topLeft.x * scaleX,
                                                            y = shape.topLeft.y * scaleY
                                                        ),
                                                        size = Size(
                                                            width = shape.size * scaleX,
                                                            height = shape.size * scaleY
                                                        ),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                        )

                        // Show processing indicator
                        if (isProcessing) {
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

                // Display circle information
                if (answers.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Detected ${answers.size} circles",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Show error message if any
            error?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

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

@Preview(showBackground = true)
@Composable
fun DebugScreenPreview() {
    QcmasterTheme {
        DebugScreenContent(
            bitmap = null,
            answers = emptyList(),
            isProcessing = false,
            error = null,
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
