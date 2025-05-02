package com.example.qcmaster.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.navigation.NavController
import com.example.qcmaster.data.FakeClassRepository
import com.example.qcmaster.data.FakeExamRepository
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignClassesToExamScreen(
    navController: NavController,
    examName: String
) {
    val allClasses = FakeClassRepository.professorClasses
    var assignedClasses by remember { mutableStateOf(listOf<String>()) }
    var newClassName by remember { mutableStateOf("") }
    var showAddClassDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Assign Classes to $examName") })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddClassDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Class") },
                text = { Text("Add Class") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Select classes to assign:")

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn {
                items(allClasses) { className ->
                    val isAssigned = assignedClasses.contains(className)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Checkbox(
                            checked = isAssigned,
                            onCheckedChange = {
                                assignedClasses = if (it) {
                                    assignedClasses + className
                                } else {
                                    assignedClasses - className
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = className)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {
                FakeExamRepository.assignClassesToExam(examName, assignedClasses)
                navController.popBackStack()
            }) {
                Text("Save Assignments")
            }


            if (showAddClassDialog) {
        AlertDialog(
            onDismissRequest = { showAddClassDialog = false },
            title = { Text("Add New Class") },
            text = {
                OutlinedTextField(
                    value = newClassName,
                    onValueChange = { newClassName = it },
                    label = { Text("Class Name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newClassName.isNotBlank() && !allClasses.contains(newClassName)) {
                            FakeClassRepository.professorClasses.add(newClassName)
                            newClassName = ""
                            showAddClassDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddClassDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}}}
