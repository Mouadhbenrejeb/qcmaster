package com.example.qcmaster.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CorrectionComparisonScreen() {
    // Fake answers (simulating scanning)
    val correctionAnswers = listOf("A", "B", "C", "D", "A")
    val studentAnswers = listOf("A", "B", "A", "D", "B")

    // Calculate score
    val correctCount = correctionAnswers.zip(studentAnswers).count { it.first == it.second }
    val totalQuestions = correctionAnswers.size
    val score = (correctCount.toFloat() / totalQuestions * 100).toInt()

    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("Correction vs Student Answers", style = MaterialTheme.typography.headlineSmall)

        correctionAnswers.indices.forEach { index ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Q${index + 1}: Correction = ${correctionAnswers[index]}")
                Text("Student = ${studentAnswers[index]}")
            }
        }

        Divider()

        Text("Correct Answers: $correctCount / $totalQuestions")
        Text("Score: $score%", style = MaterialTheme.typography.headlineMedium)
    }
}
