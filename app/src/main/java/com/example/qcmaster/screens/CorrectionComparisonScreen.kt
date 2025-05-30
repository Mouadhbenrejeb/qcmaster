package com.example.qcmaster.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.qcmaster.models.Exam
import com.example.qcmaster.models.Student
import com.example.qcmaster.ui.theme.QcmasterTheme
import com.example.qcmaster.viewmodels.CorrectionComparisonUiState
import com.example.qcmaster.viewmodels.CorrectionComparisonViewModel
import com.example.qcmaster.viewmodels.CorrectionComparisonViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CorrectionComparisonScreen(
    navController: NavController,
    examId: String,
    className: String,
    studentId: String
) {
    val viewModel: CorrectionComparisonViewModel = viewModel(
        factory = CorrectionComparisonViewModelFactory(examId, className, studentId)
    )

    val state = viewModel.state

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Exam Correction: ${state.student?.name ?: "Student"}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
                        Button(onClick = { navController.popBackStack() }) {
                            Text("Go Back")
                        }
                    }
                }
            } else {
                // Content
                CorrectionComparisonContent(state = state)
            }
        }
    }
}

@Composable
fun CorrectionComparisonContent(state: CorrectionComparisonUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Student and Exam Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Student Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

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

        // Score Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📊 Correction Results",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

//                Text(
//                    text = "✅ Correct: ${state.correctCount} / ${state.totalQuestions}",
//                    style = MaterialTheme.typography.bodyLarge
//                )

                Text(
                    text = "✅ Note: ${state.scoreOutOf20}/20",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Answers Comparison Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Answer Comparison",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Question",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Correct",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Student",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Result",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Answer rows
                state.correctAnswers.indices.forEach { i ->
                    val correct = state.correctAnswers.getOrNull(i) ?: "-"
                    val student = state.studentAnswers.getOrNull(i) ?: "-"
                    val isCorrect = correct == student

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Q${i + 1}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = correct,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = student,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (isCorrect) "✓" else "✗",
                            color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (i < state.correctAnswers.size - 1) {
                        Divider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CorrectionComparisonScreenPreview() {
    QcmasterTheme {
        val dummyState = CorrectionComparisonUiState(
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
            correctAnswers = listOf("1", "2", "3", "4", "1"),
            studentAnswers = listOf("1", "2", "3", "4", "1"),
            score = 80,
            correctCount = 4,
            totalQuestions = 5
        )

        CorrectionComparisonContent(state = dummyState)
    }
}
