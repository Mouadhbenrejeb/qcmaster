package com.example.qcmaster.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.qcmaster.ui.theme.QcmasterTheme
import com.example.qcmaster.viewmodels.AssignClassesUiState
import com.example.qcmaster.viewmodels.AssignClassesViewModel

class AssignClassesViewModelFactory(private val examId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AssignClassesViewModel::class.java)) {
            return AssignClassesViewModel(examId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignClassesToExamScreen(
    navController: NavController,
    examId: String
) {
    val viewModel: AssignClassesViewModel = viewModel(
        factory = AssignClassesViewModelFactory(examId)
    )

    val state = viewModel.state

    AssignClassesToExamContent(
        state = state,
        onClassToggled = viewModel.onClassToggled,
        onShowAddClassDialog = viewModel.onShowAddClassDialog,
        onDismissAddClassDialog = viewModel.onDismissAddClassDialog,
        onNewClassNameChanged = viewModel.onNewClassNameChanged,
        onAddClass = viewModel.onAddClass,
        onSaveAssignments = {
            viewModel.onSaveAssignments()
            navController.popBackStack()
        },
        onBackPressed = {
            navController.popBackStack()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignClassesToExamContent(
    state: AssignClassesUiState,
    onClassToggled: (String, Boolean) -> Unit,
    onShowAddClassDialog: () -> Unit,
    onDismissAddClassDialog: () -> Unit,
    onNewClassNameChanged: (String) -> Unit,
    onAddClass: () -> Unit,
    onSaveAssignments: () -> Unit,
    onBackPressed: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Assign Classes to ${state.exam?.name ?: "Exam"}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onShowAddClassDialog,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Class") },
                text = { Text("Add Class") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (state.isLoading) {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null) {
                // Error state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBackPressed) {
                            Text("Go Back")
                        }
                    }
                }
            } else {
                // Content
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Select classes to assign:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (state.allClasses.isEmpty()) {
                        // Empty state for classes
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No classes available. Add a class to get started.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // List of classes
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(state.allClasses) { className ->
                                val isAssigned = state.assignedClasses.contains(className)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isAssigned) 
                                            MaterialTheme.colorScheme.primaryContainer 
                                        else 
                                            MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isAssigned,
                                            onCheckedChange = { checked ->
                                                onClassToggled(className, checked)
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = MaterialTheme.colorScheme.primary,
                                                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = className,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = if (isAssigned) 
                                                MaterialTheme.colorScheme.onPrimaryContainer 
                                            else 
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Save button
                    Button(
                        onClick = onSaveAssignments,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Save Assignments")
                    }
                }
            }

            // Add Class Dialog
            if (state.showAddClassDialog) {
                AlertDialog(
                    onDismissRequest = onDismissAddClassDialog,
                    title = { 
                        Text(
                            "Add New Class",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        ) 
                    },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = state.newClassName,
                                onValueChange = onNewClassNameChanged,
                                label = { Text("Class Name") },
                                isError = state.newClassNameError != null,
                                supportingText = {
                                    state.newClassNameError?.let {
                                        Text(
                                            text = it,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = onAddClass,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = onDismissAddClassDialog) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AssignClassesToExamScreenPreview() {
    QcmasterTheme {
        val dummyState = AssignClassesUiState(
            exam = com.example.qcmaster.models.Exam(
                id = "1",
                name = "Math Exam",
                assignedClasses = listOf("Computer Science")
            ),
            allClasses = listOf("Computer Science", "Mathematics", "Physics"),
            assignedClasses = listOf("Computer Science"),
            isLoading = false,
            error = null,
            showAddClassDialog = false
        )

        AssignClassesToExamContent(
            state = dummyState,
            onClassToggled = { _, _ -> },
            onShowAddClassDialog = {},
            onDismissAddClassDialog = {},
            onNewClassNameChanged = {},
            onAddClass = {},
            onSaveAssignments = {},
            onBackPressed = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AssignClassesToExamScreenLoadingPreview() {
    QcmasterTheme {
        val dummyState = AssignClassesUiState(
            isLoading = true
        )

        AssignClassesToExamContent(
            state = dummyState,
            onClassToggled = { _, _ -> },
            onShowAddClassDialog = {},
            onDismissAddClassDialog = {},
            onNewClassNameChanged = {},
            onAddClass = {},
            onSaveAssignments = {},
            onBackPressed = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AssignClassesToExamScreenErrorPreview() {
    QcmasterTheme {
        val dummyState = AssignClassesUiState(
            isLoading = false,
            error = "Failed to load exam details"
        )

        AssignClassesToExamContent(
            state = dummyState,
            onClassToggled = { _, _ -> },
            onShowAddClassDialog = {},
            onDismissAddClassDialog = {},
            onNewClassNameChanged = {},
            onAddClass = {},
            onSaveAssignments = {},
            onBackPressed = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AssignClassesToExamScreenDialogPreview() {
    QcmasterTheme {
        val dummyState = AssignClassesUiState(
            exam = com.example.qcmaster.models.Exam(
                id = "1",
                name = "Math Exam"
            ),
            allClasses = listOf("Computer Science", "Mathematics", "Physics"),
            assignedClasses = emptyList(),
            isLoading = false,
            error = null,
            showAddClassDialog = true,
            newClassName = "Biology"
        )

        AssignClassesToExamContent(
            state = dummyState,
            onClassToggled = { _, _ -> },
            onShowAddClassDialog = {},
            onDismissAddClassDialog = {},
            onNewClassNameChanged = {},
            onAddClass = {},
            onSaveAssignments = {},
            onBackPressed = {}
        )
    }
}
