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
@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun ExamsScreen(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    var examTitle by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf("") }
    var exams by remember { mutableStateOf(listOf<Pair<String, String>>()) } // Pair<Title, Class>

    // Dummy class list for now
    val classOptions = listOf("Class A", "Class B", "Class C")

    Scaffold(
        bottomBar = { MyNavigationBar(navController) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = "Add Exam") },
                text = { Text("Add Exam") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Exams", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(16.dp))

            if (exams.isEmpty()) {
                Text("No exams added yet.")
            } else {
                LazyColumn {
                    items(exams) { exam ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("📄 ${exam.first}", style = MaterialTheme.typography.bodyLarge)
                                Text("🏫 Class: ${exam.second}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        // Dialog to add an exam
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add Exam") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = examTitle,
                            onValueChange = { examTitle = it },
                            label = { Text("Exam Title") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Dropdown for classes
                        var expanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedClass,
                                onValueChange = {},
                                label = { Text("Select Class") },
                                readOnly = true,
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                classOptions.forEach { cls ->
                                    DropdownMenuItem(
                                        text = { Text(cls) },
                                        onClick = {
                                            selectedClass = cls
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (examTitle.isNotBlank() && selectedClass.isNotBlank()) {
                            exams = exams + (examTitle to selectedClass)
                            examTitle = ""
                            selectedClass = ""
                            showDialog = false
                        }
                    }) {
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
