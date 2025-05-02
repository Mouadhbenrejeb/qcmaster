package com.example.qcmaster.screens



import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.qcmaster.data.FakeExamRepository
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassExamsScreen(navController: NavController, className: String) {
    val exams = FakeExamRepository.exams.filter {
        FakeExamRepository.getAssignedClasses(it).contains(className)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Exams for $className") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            if (exams.isEmpty()) {
                Text("No exams assigned to this class.")
            } else {
                exams.forEach { exam ->
                    Button(
                        onClick = {
                            navController.navigate("class_exam_grades/$className/$exam")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(exam)
                    }
                }
            }
        }
    }
}
