package com.example.qcmaster.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.qcmaster.Route
import com.example.qcmaster.models.Exam
import com.example.qcmaster.ui.theme.QcmasterTheme
import com.example.qcmaster.viewmodels.ExamCorrectionClassSelectionViewModel
import com.example.qcmaster.viewmodels.ExamCorrectionClassSelectionUiState

class ExamCorrectionClassSelectionViewModelFactory(private val examId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExamCorrectionClassSelectionViewModel::class.java)) {
            return ExamCorrectionClassSelectionViewModel(examId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamCorrectionClassSelectionScreen(
    navController: NavController,
    examId: String
) {
    val viewModel: ExamCorrectionClassSelectionViewModel = viewModel(
        factory = ExamCorrectionClassSelectionViewModelFactory(examId)
    )

    val state = viewModel.state

    // Check if answer paper has been uploaded
    LaunchedEffect(state.exam) {
        if (state.exam != null && !state.exam.answerPaperUploaded) {
            // Navigate to answer paper upload screen
            navController.navigate(Route.ExamAnswerPaperUploadScreen(examId).route) {
                popUpTo(Route.ExamCorrectionClassSelectionScreen.route) { inclusive = true }
            }
        }
    }

    ExamCorrectionClassSelectionContent(
        state = state,
        onBackPressed = {
            navController.popBackStack()
        },
        onClassSelected = { className ->
            navController.navigate(Route.ExamCorrectionStudentSelectionScreen(examId, className).route)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamCorrectionClassSelectionContent(
    state: ExamCorrectionClassSelectionUiState,
    onBackPressed: () -> Unit,
    onClassSelected: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Select Class for Correction: ${state.exam?.name ?: "Exam"}",
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
            } else if (state.assignedClasses.isEmpty()) {
                // No classes assigned
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No Classes Assigned",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This exam has no classes assigned to it.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBackPressed) {
                            Text("Go Back")
                        }
                    }
                }
            } else {
                // List of classes
                Column {
                    Text(
                        text = "Select a class to correct exams for:",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.assignedClasses) { className ->
                            ClassCard(
                                className = className,
                                onClassSelected = { onClassSelected(className) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClassCard(
    className: String,
    onClassSelected: () -> Unit
) {
    Card(
        onClick = onClassSelected,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Class icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = className.first().toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Class name
            Column {
                Text(
                    text = className,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Tap to select students",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExamCorrectionClassSelectionContentPreview() {
    QcmasterTheme {
        val dummyState = ExamCorrectionClassSelectionUiState(
            exam = Exam(
                id = "1",
                name = "Math Exam",
                assignedClasses = listOf("Computer Science", "Mathematics", "Physics")
            ),
            assignedClasses = listOf("Computer Science", "Mathematics", "Physics"),
            isLoading = false,
            error = null
        )

        ExamCorrectionClassSelectionContent(
            state = dummyState,
            onBackPressed = {},
            onClassSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExamCorrectionClassSelectionLoadingPreview() {
    QcmasterTheme {
        val dummyState = ExamCorrectionClassSelectionUiState(
            isLoading = true,
            error = null
        )

        ExamCorrectionClassSelectionContent(
            state = dummyState,
            onBackPressed = {},
            onClassSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExamCorrectionClassSelectionErrorPreview() {
    QcmasterTheme {
        val dummyState = ExamCorrectionClassSelectionUiState(
            isLoading = false,
            error = "Failed to load exam details. Please try again."
        )

        ExamCorrectionClassSelectionContent(
            state = dummyState,
            onBackPressed = {},
            onClassSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExamCorrectionClassSelectionEmptyPreview() {
    QcmasterTheme {
        val dummyState = ExamCorrectionClassSelectionUiState(
            exam = Exam(
                id = "1",
                name = "Math Exam",
                assignedClasses = emptyList()
            ),
            assignedClasses = emptyList(),
            isLoading = false,
            error = null
        )

        ExamCorrectionClassSelectionContent(
            state = dummyState,
            onBackPressed = {},
            onClassSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ClassCardPreview() {
    QcmasterTheme {
        ClassCard(
            className = "Computer Science",
            onClassSelected = {}
        )
    }
}
