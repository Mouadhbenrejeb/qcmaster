package com.example.qcmaster.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.qcmaster.components.MyNavigationBar
import com.example.qcmaster.data.FakeClassRepository
import com.example.qcmaster.data.FakeStudentRepository
import com.example.qcmaster.models.Student

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var cin by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf("") }
    var cinError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }

    // Create a reactive list to observe changes
    val students = remember { mutableStateListOf<Student>().apply {
        addAll(FakeStudentRepository.getAllStudents())
    } }

    val allClasses = FakeClassRepository.professorClasses
    var classDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = { MyNavigationBar(navController) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Student") },
                text = { Text("Add Student") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Students", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(16.dp))

            if (students.isEmpty()) {
                Text("No students yet.", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn {
                    items(students) { student ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Name: ${student.name}", style = MaterialTheme.typography.bodyLarge)
                                Text("CIN: ${student.cin}", style = MaterialTheme.typography.bodyMedium)
                            }
                            IconButton(
                                onClick = {
                                    FakeStudentRepository.removeStudent(student.cin)
                                    students.removeIf { it.cin == student.cin } // Remove student from list to trigger UI update
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Student"
                                )
                            }
                        }
                        Divider()
                    }
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Add New Student") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = name,
                                onValueChange = {
                                    name = it
                                    nameError = null
                                },
                                label = { Text("Name") },
                                isError = nameError != null
                            )
                            if (nameError != null) {
                                Text(nameError!!, color = MaterialTheme.colorScheme.error)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = cin,
                                onValueChange = {
                                    cin = it
                                    cinError = null
                                },
                                label = { Text("CIN") },
                                isError = cinError != null
                            )
                            if (cinError != null) {
                                Text(cinError!!, color = MaterialTheme.colorScheme.error)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            ExposedDropdownMenuBox(
                                expanded = classDropdownExpanded,
                                onExpandedChange = {
                                    classDropdownExpanded = !classDropdownExpanded
                                }
                            ) {
                                OutlinedTextField(
                                    value = selectedClass,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Assign to Class") },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = "Dropdown"
                                        )
                                    },
                                    modifier = Modifier.menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = classDropdownExpanded,
                                    onDismissRequest = { classDropdownExpanded = false }
                                ) {
                                    allClasses.forEach { className ->
                                        DropdownMenuItem(
                                            text = { Text(className) },
                                            onClick = {
                                                selectedClass = className
                                                classDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                // Validations
                                var isValid = true
                                if (name.isBlank()) {
                                    nameError = "Name cannot be empty"
                                    isValid = false
                                }
                                if (!cin.matches(Regex("\\d{8}"))) {
                                    cinError = "CIN must be exactly 8 digits"
                                    isValid = false
                                } else if (FakeStudentRepository.getAllStudents()
                                        .any { it.cin == cin }
                                ) {
                                    cinError = "This CIN already exists"
                                    isValid = false
                                }

                                if (isValid && selectedClass.isNotBlank()) {
                                    // Add student to repository
                                    val student = Student(name, cin, selectedClass)
                                    FakeStudentRepository.addStudent(student)

                                    // Add student to the reactive list to update UI
                                    students.add(student)

                                    name = ""
                                    cin = ""
                                    selectedClass = ""
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
