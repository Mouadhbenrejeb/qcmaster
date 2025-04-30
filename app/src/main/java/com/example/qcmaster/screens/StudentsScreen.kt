package com.example.qcmaster.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.qcmaster.components.MyNavigationBar

data class Student(val cin: String, val name: String, val assignedClass: String?)

@Composable
fun StudentsScreen(navController: NavController) {
    var students by remember { mutableStateOf(listOf<Student>()) }
    var showDialog by remember { mutableStateOf(false) }
    var cin by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var assignedClass by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = { MyNavigationBar(navController) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = "Add Student") },
                text = { Text("Add Student") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Students", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            if (students.isEmpty()) {
                Text("No students added yet.", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn {
                    items(students) { student ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Name: ${student.name}")
                                    Text("CIN: ${student.cin}")
                                    Text("Class: ${student.assignedClass ?: "Not Assigned"}")
                                }
                                IconButton(onClick = {
                                    students = students.filterNot { it.cin == student.cin }
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    cin = ""
                    name = ""
                    assignedClass = ""
                    error = ""
                },
                title = { Text("Add a new student") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = cin,
                            onValueChange = {
                                if (it.length <= 8 && it.all { c -> c.isDigit() }) cin = it
                            },
                            label = { Text("CIN (8 digits)") },
                            isError = cin.length != 8
                        )
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") }
                        )
                        OutlinedTextField(
                            value = assignedClass,
                            onValueChange = { assignedClass = it },
                            label = { Text("Class (optional)") }
                        )
                        if (error.isNotEmpty()) {
                            Text(error, color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        error = ""
                        if (cin.length != 8) {
                            error = "CIN must be exactly 8 digits."
                        } else if (name.isBlank()) {
                            error = "Name cannot be empty."
                        } else if (students.any { it.cin == cin }) {
                            error = "Student with this CIN already exists."
                        } else {
                            students = students + Student(cin, name, assignedClass.takeIf { it.isNotBlank() })
                            cin = ""
                            name = ""
                            assignedClass = ""
                            showDialog = false
                        }
                    }) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog = false
                        cin = ""
                        name = ""
                        assignedClass = ""
                        error = ""
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
