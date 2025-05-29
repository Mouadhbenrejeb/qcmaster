package com.example.qcmaster.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import com.example.qcmaster.Routes
import com.example.qcmaster.models.Exam
import com.example.qcmaster.models.Student
import com.example.qcmaster.ui.theme.QcmasterTheme
import com.example.qcmaster.viewmodels.ExamCorrectionStudentSelectionViewModel
import com.example.qcmaster.viewmodels.ExamCorrectionStudentSelectionUiState

class ExamCorrectionStudentSelectionViewModelFactory(
    private val examId: String,
    private val className: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExamCorrectionStudentSelectionViewModel::class.java)) {
            return ExamCorrectionStudentSelectionViewModel(examId, className) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamCorrectionStudentSelectionScreen(
    navController: NavController,
    examId: String,
    className: String
) {
    val viewModel: ExamCorrectionStudentSelectionViewModel = viewModel(
        factory = ExamCorrectionStudentSelectionViewModelFactory(examId, className)
    )

    val state = viewModel.state

    ExamCorrectionStudentSelectionContent(
        state = state,
        onBackPressed = {
            navController.popBackStack()
        },
        onStudentSelected = { student ->
            navController.navigate(Routes.ExamCorrectionPaperUpload.createRoute(examId, className, student.cin))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamCorrectionStudentSelectionContent(
    state: ExamCorrectionStudentSelectionUiState,
    onBackPressed: () -> Unit,
    onStudentSelected: (Student) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Select Student for Correction: ${state.className}",
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
            } else if (state.students.isEmpty()) {
                // No students
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No Students Found",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "There are no students assigned to this class.",
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
                // List of students
                Column {
                    Text(
                        text = "Select a student to correct their exam:",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.students) { student ->
                            CorrectionStudentCard(
                                student = student,
                                onStudentSelected = { onStudentSelected(student) },
                                isCorrected = state.exam?.correctedStudents?.get(student.cin) ?: false,
                                mark = state.exam?.studentMarks?.get(student.cin)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CorrectionStudentCard(
    student: Student,
    onStudentSelected: () -> Unit,
    isCorrected: Boolean = false,
    mark: Int? = null
) {
    Card(
        onClick = onStudentSelected,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Student icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isCorrected)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.name.first().toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Student details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "CIN: ${student.cin}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

//                Row(
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
                    Text(
                        text = if (isCorrected) "Corrected ✓" else "Not corrected - Tap to upload exam paper",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCorrected) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.secondary
                    )

                    if (isCorrected) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Note: 17/20",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
//                }
            }

            if (isCorrected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Corrected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExamCorrectionStudentSelectionContentPreview() {
    QcmasterTheme {
        val dummyState = ExamCorrectionStudentSelectionUiState(
            exam = Exam(
                id = "1",
                name = "Math Exam",
                assignedClasses = listOf("Computer Science")
            ),
            className = "Computer Science",
            students = listOf(
                Student(
                    name = "John Doe",
                    cin = "12345678",
                    assignedClass = "Computer Science"
                ),
                Student(
                    name = "Jane Smith",
                    cin = "87654321",
                    assignedClass = "Computer Science"
                ),
                Student(
                    name = "Bob Johnson",
                    cin = "23456789",
                    assignedClass = "Computer Science"
                )
            ),
            isLoading = false,
            error = null
        )

        ExamCorrectionStudentSelectionContent(
            state = dummyState,
            onBackPressed = {},
            onStudentSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExamCorrectionStudentSelectionLoadingPreview() {
    QcmasterTheme {
        val dummyState = ExamCorrectionStudentSelectionUiState(
            className = "Computer Science",
            isLoading = true,
            error = null
        )

        ExamCorrectionStudentSelectionContent(
            state = dummyState,
            onBackPressed = {},
            onStudentSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExamCorrectionStudentSelectionErrorPreview() {
    QcmasterTheme {
        val dummyState = ExamCorrectionStudentSelectionUiState(
            className = "Computer Science",
            isLoading = false,
            error = "Failed to load students. Please try again."
        )

        ExamCorrectionStudentSelectionContent(
            state = dummyState,
            onBackPressed = {},
            onStudentSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExamCorrectionStudentSelectionEmptyPreview() {
    QcmasterTheme {
        val dummyState = ExamCorrectionStudentSelectionUiState(
            exam = Exam(
                id = "1",
                name = "Math Exam",
                assignedClasses = listOf("Computer Science")
            ),
            className = "Computer Science",
            students = emptyList(),
            isLoading = false,
            error = null
        )

        ExamCorrectionStudentSelectionContent(
            state = dummyState,
            onBackPressed = {},
            onStudentSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CorrectionStudentCardPreview() {
    QcmasterTheme {
        CorrectionStudentCard(
            student = Student(
                name = "John Doe",
                cin = "12345678",
                assignedClass = "Computer Science"
            ),
            onStudentSelected = {},
            isCorrected = true,
            mark = 85
        )
    }
}
