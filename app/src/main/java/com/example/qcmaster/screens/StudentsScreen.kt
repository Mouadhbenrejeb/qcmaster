package com.example.qcmaster.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Class
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.qcmaster.components.MyNavigationBar
import com.example.qcmaster.models.Student
import com.example.qcmaster.ui.theme.QcmasterTheme
import com.example.qcmaster.viewmodels.StudentsUiState
import com.example.qcmaster.viewmodels.StudentsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(navController: NavController) {
    val viewModel: StudentsViewModel = viewModel()

    StudentsScreenContent(
        state = viewModel.state,
        onShowAddStudentDialog = viewModel.onShowAddStudentDialog,
        onDismissAddStudentDialog = viewModel.onDismissAddStudentDialog,
        onNameChanged = viewModel.onNameChanged,
        onCinChanged = viewModel.onCinChanged,
        onSelectedClassChanged = viewModel.onSelectedClassChanged,
        onToggleClassDropdown = viewModel.onToggleClassDropdown,
        onCloseClassDropdown = viewModel.onCloseClassDropdown,
        onAddStudent = viewModel.onAddStudent,
        onDeleteStudent = viewModel.onDeleteStudent,
        navController = navController
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreenContent(
    state: StudentsUiState,
    onShowAddStudentDialog: () -> Unit,
    onDismissAddStudentDialog: () -> Unit,
    onNameChanged: (String) -> Unit,
    onCinChanged: (String) -> Unit,
    onSelectedClassChanged: (String) -> Unit,
    onToggleClassDropdown: () -> Unit,
    onCloseClassDropdown: () -> Unit,
    onAddStudent: () -> Unit,
    onDeleteStudent: (String) -> Unit,
    navController: NavController
) {
    Scaffold(
        bottomBar = { MyNavigationBar(navController) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onShowAddStudentDialog,
                icon = { Icon(Icons.Filled.Add, contentDescription = "Add Student") },
                text = { Text("Add Student") },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (state.isLoading) {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (state.error != null) {
                // Error state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            } else {
                // Content state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // Header
                    Text(
                        text = "Students",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Manage your students",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (state.students.isEmpty()) {
                        // Empty state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = "No Students",
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No students yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Add students using the button below",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        // Students list
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.students) { student ->
                                StudentCard(
                                    student = student,
                                    onDelete = { onDeleteStudent(student.cin) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add Student Dialog
        if (state.showAddStudentDialog) {
            AlertDialog(
                onDismissRequest = onDismissAddStudentDialog,
                title = { 
                    Text(
                        text = "Add New Student",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Name field
                        OutlinedTextField(
                            value = state.name,
                            onValueChange = onNameChanged,
                            label = { Text("Name") },
                            isError = state.nameError != null,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = "Name Icon",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                        if (state.nameError != null) {
                            Text(
                                text = state.nameError,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        // CIN field
                        OutlinedTextField(
                            value = state.cin,
                            onValueChange = onCinChanged,
                            label = { Text("CIN") },
                            isError = state.cinError != null,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        if (state.cinError != null) {
                            Text(
                                text = state.cinError,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        // Class dropdown
                        ExposedDropdownMenuBox(
                            expanded = state.classDropdownExpanded,
                            onExpandedChange = { onToggleClassDropdown() }
                        ) {
                            OutlinedTextField(
                                value = state.selectedClass,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Assign to Class") },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Dropdown"
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Class,
                                        contentDescription = "Class Icon",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = state.classDropdownExpanded,
                                onDismissRequest = onCloseClassDropdown
                            ) {
                                state.availableClasses.forEach { className ->
                                    DropdownMenuItem(
                                        text = { Text(className) },
                                        onClick = {
                                            onSelectedClassChanged(className)
                                            onCloseClassDropdown()
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onAddStudent,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = onDismissAddStudentDialog
                    ) {
                        Text("Cancel")
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun StudentCard(
    student: Student,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "CIN: ${student.cin}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(
                        text = student.assignedClass,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            IconButton(
                onClick = onDelete, modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Student",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StudentsScreenPreview() {
    QcmasterTheme {
        val dummyState = StudentsUiState(
            students = listOf(
                Student(
                    name = "John Doe",
                    cin = "12345678",
                    assignedClass = "Computer Science"
                ),
                Student(
                    name = "Jane Smith",
                    cin = "87654321",
                    assignedClass = "Mathematics"
                )
            ),
            isLoading = false,
            error = null,
            showAddStudentDialog = false,
            availableClasses = listOf("Computer Science", "Mathematics", "Physics")
        )

        StudentsScreenContent(
            state = dummyState,
            onShowAddStudentDialog = {},
            onDismissAddStudentDialog = {},
            onNameChanged = {},
            onCinChanged = {},
            onSelectedClassChanged = {},
            onToggleClassDropdown = {},
            onCloseClassDropdown = {},
            onAddStudent = {},
            onDeleteStudent = {},
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StudentsScreenEmptyPreview() {
    QcmasterTheme {
        val dummyState = StudentsUiState(
            students = emptyList(),
            isLoading = false,
            error = null,
            showAddStudentDialog = false,
            availableClasses = listOf("Computer Science", "Mathematics", "Physics")
        )

        StudentsScreenContent(
            state = dummyState,
            onShowAddStudentDialog = {},
            onDismissAddStudentDialog = {},
            onNameChanged = {},
            onCinChanged = {},
            onSelectedClassChanged = {},
            onToggleClassDropdown = {},
            onCloseClassDropdown = {},
            onAddStudent = {},
            onDeleteStudent = {},
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StudentsScreenLoadingPreview() {
    QcmasterTheme {
        val dummyState = StudentsUiState(
            students = emptyList(),
            isLoading = true,
            error = null,
            showAddStudentDialog = false
        )

        StudentsScreenContent(
            state = dummyState,
            onShowAddStudentDialog = {},
            onDismissAddStudentDialog = {},
            onNameChanged = {},
            onCinChanged = {},
            onSelectedClassChanged = {},
            onToggleClassDropdown = {},
            onCloseClassDropdown = {},
            onAddStudent = {},
            onDeleteStudent = {},
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StudentsScreenErrorPreview() {
    QcmasterTheme {
        val dummyState = StudentsUiState(
            students = emptyList(),
            isLoading = false,
            error = "Failed to load students",
            showAddStudentDialog = false
        )

        StudentsScreenContent(
            state = dummyState,
            onShowAddStudentDialog = {},
            onDismissAddStudentDialog = {},
            onNameChanged = {},
            onCinChanged = {},
            onSelectedClassChanged = {},
            onToggleClassDropdown = {},
            onCloseClassDropdown = {},
            onAddStudent = {},
            onDeleteStudent = {},
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StudentCardPreview() {
    QcmasterTheme {
        StudentCard(
            student = Student(
                name = "John Doe",
                cin = "12345678",
                assignedClass = "Computer Science"
            ),
            onDelete = {}
        )
    }
}
