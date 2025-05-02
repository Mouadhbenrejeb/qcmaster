package com.example.qcmaster.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.qcmaster.data.FakeStudentRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamStudentGradesScreen(className: String, examName: String) {
    val students = FakeStudentRepository.getStudentsInClass(className)
    val grades = FakeStudentRepository.getGradesForClassExam(className, examName)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Grades for $examName - $className") })
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(16.dp)) {
            if (students.isEmpty()) {
                Text("No students in this class.")
            } else {
                students.forEach { student ->
                    val note = grades[student.cin]?.toString() ?: "N/A"
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("👤 ${student.name}")
                            Text("🆔 CIN: ${student.cin}")
                            Text("📝 Grade: $note")
                        }
                    }
                }
            }
        }
    }
}
