package com.example.qcmaster.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.qcmaster.data.FakeExamRepository

@Composable
fun ScanExamScreen(
    navController: NavController,
    examName: String
) {
    var correctAnswers by remember { mutableStateOf<List<String>?>(null) }
    var studentAnswers by remember { mutableStateOf<List<String>>(emptyList()) }
    var showResults by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val assignedClasses = FakeExamRepository.getAssignedClasses(examName)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("📄 Scanning for Exam: $examName", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))

        // Show error message if no classes are assigned to this exam
        if (assignedClasses.isEmpty()) {
            Text(
                text = "❗ This exam has no assigned classes. Please assign at least one class.",
                color = MaterialTheme.colorScheme.error
            )
        }

        // Show error message if no correct answers are saved
        if (correctAnswers == null) {
            Button(
                onClick = {
                    // Simulate scanning the correct paper
                    correctAnswers = listOf("A", "B", "C", "D", "A") // Fake correct answers
                    FakeExamRepository.saveCorrectAnswers(examName, correctAnswers!!)
                    errorMessage = null // Clear any previous error
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = assignedClasses.isNotEmpty() // Enable button only if classes are assigned
            ) {
                Text("📷 Scan Correct Paper")
            }
        } else {
            Text("✅ Correct answers saved: ${correctAnswers!!.joinToString()}")
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // Simulate scanning a student paper
                    studentAnswers = listOf("A", "B", "D", "D", "A") // Fake student answers
                    showResults = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("👨‍🎓 Scan Student Paper")
            }

            if (showResults) {
                Spacer(modifier = Modifier.height(24.dp))
                val correct = correctAnswers!!
                val score = correct.zip(studentAnswers).count { it.first == it.second }
                Text("Student Answers: ${studentAnswers.joinToString()}")
                Text("Score: $score / ${correct.size}", style = MaterialTheme.typography.titleMedium)
            }
        }

        // Show any error messages (e.g., no correct answers or classes)
        errorMessage?.let {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Button to navigate back or perform further actions
        Button(
            onClick = {
                if (assignedClasses.isEmpty()) {
                    errorMessage = "You must assign classes to the exam before scanning."
                } else {
                    // Navigate back or handle the flow for scanning
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Exam List")
        }
    }
}
