package com.example.qcmaster.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.qcmaster.components.MyNavigationBar
import com.example.qcmaster.data.FakeClassRepository
import com.example.qcmaster.data.FakeStudentRepository
import com.example.qcmaster.ui.screens.AuthScreen

@Composable
fun ClassesScreen(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    var className by remember { mutableStateOf("") }
    val classes = FakeClassRepository.professorClasses
    var duplicateError by remember { mutableStateOf(false) }

    val students = FakeStudentRepository.getAllStudents()

    Scaffold(
        bottomBar = { MyNavigationBar(navController) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    showDialog = true
                    duplicateError = false
                },
                icon = { Icon(Icons.Filled.Add, contentDescription = "Add Class") },
                text = { Text("Add Class") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Your Classes", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            if (classes.isEmpty()) {
                Text("No classes added yet.", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(classes) { classItem ->
                        val studentCount = students.count { it.assignedClass == classItem }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    navController.navigate("class_exams/$classItem")
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = classItem,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "$studentCount student${if (studentCount != 1) "s" else ""}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                IconButton(onClick = {
                                    FakeClassRepository.professorClasses.remove(classItem)
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Class"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add a new class") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = className,
                            onValueChange = {
                                className = it
                                duplicateError = false
                            },
                            label = { Text("Class Name") },
                            isError = duplicateError
                        )
                        if (duplicateError) {
                            Text(
                                text = "This class already exists!",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (className.isNotBlank()) {
                                if (classes.contains(className)) {
                                    duplicateError = true
                                } else {
                                    FakeClassRepository.professorClasses.add(className)
                                    className = ""
                                    duplicateError = false
                                    showDialog = false
                                }
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog = false
                        duplicateError = false
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ClassesScreenPreview() {
    ClassesScreen(rememberNavController())
}