package com.example.qcmaster.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CorrectionComparisonScreen(
    correctAnswers: List<String>,
    studentAnswers: List<String>
) {
    val total = correctAnswers.size
    val correctCount = correctAnswers.zip(studentAnswers).count { it.first == it.second }
    val score = if (total > 0) (correctCount * 100 / total) else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📊 Correction Results", style = MaterialTheme.typography.headlineSmall)

        correctAnswers.indices.forEach { i ->
            val correct = correctAnswers.getOrNull(i) ?: "-"
            val student = studentAnswers.getOrNull(i) ?: "-"
            val color = if (correct == student) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Q${i + 1}: $correct", color = color)
                Text("Student: $student", color = color)
            }
        }

        Divider()
        Text("✅ Correct: $correctCount / $total")
        Text("🎯 Score: $score%", style = MaterialTheme.typography.titleLarge)
    }
}
