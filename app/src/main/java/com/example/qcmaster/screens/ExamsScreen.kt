package com.example.qcmaster.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.qcmaster.components.MyNavigationBar
import com.example.qcmaster.data.FakeExamRepository

@Composable
fun ExamsScreen(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    var examName by remember { mutableStateOf("") }
    val exams = FakeExamRepository.exams

    Scaffold(
        bottomBar = { MyNavigationBar(navController) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Exam") },
                text = { Text("Add Exam") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Your Exams", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(16.dp))

            if (exams.isEmpty()) {
                Text("No exams yet.", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn {
                    items(exams) { exam ->
                        val assignedClasses = FakeExamRepository.getAssignedClasses(exam)

                        Card(
                            onClick = {
                                // Navigate to AssignClassesToExamScreen or ScanExamScreen based on the situation
                                if (assignedClasses.isEmpty()) {
                                    navController.navigate("assign_classes_to_exam/$exam")
                                } else {
                                    navController.navigate("scan_exam/$exam")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = exam,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = if (assignedClasses.isNotEmpty())
                                        "Classes: ${assignedClasses.joinToString(", ")}"
                                    else
                                        "No classes assigned yet.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Add a new exam") },
                    text = {
                        OutlinedTextField(
                            value = examName,
                            onValueChange = { examName = it },
                            label = { Text("Exam Name") }
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (examName.isNotBlank() && !exams.contains(examName)) {
                                    FakeExamRepository.addExam(examName)
                                    examName = ""
                                    showDialog = false
                                }
                            }
                        ) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
