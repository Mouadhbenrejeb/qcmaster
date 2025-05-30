package com.example.qcmaster.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Class
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.qcmaster.Routes
import com.example.qcmaster.components.MyNavigationBar
import com.example.qcmaster.models.Exam
import com.example.qcmaster.ui.theme.QcmasterTheme
import com.example.qcmaster.viewmodels.ExamsUiState
import com.example.qcmaster.viewmodels.ExamsViewModel

@Composable
fun ExamsScreen(
    navController: NavController,
    viewModel: ExamsViewModel = viewModel()
) {
    val state = viewModel.state
    val context = LocalContext.current

    // Function to extract notes for a specific exam and class
    val onExtractNotes: (Exam, String) -> Unit = { exam, className ->
        viewModel.extractStudentNotes(context, exam, className)
    }

    ExamsScreenContent(
        state = state,
        onShowAddExamDialog = viewModel.onShowAddExamDialog,
        onDismissAddExamDialog = viewModel.onDismissAddExamDialog,
        onShowEditExamDialog = viewModel.onShowEditExamDialog,
        onDismissEditExamDialog = viewModel.onDismissEditExamDialog,
        onExamNameChanged = viewModel.onExamNameChanged,
        onClassToggled = viewModel.onClassToggled,
        onAddExam = viewModel.onAddExam,
        onUpdateExam = viewModel.onUpdateExam,
        onDeleteExam = viewModel.onDeleteExam,
        onExtractNotes = onExtractNotes,
        navController = navController
    )
}

@Composable
fun ExamsScreenContent(
    state: ExamsUiState,
    onShowAddExamDialog: () -> Unit,
    onDismissAddExamDialog: () -> Unit,
    onShowEditExamDialog: (Exam) -> Unit,
    onDismissEditExamDialog: () -> Unit,
    onExamNameChanged: (String) -> Unit,
    onClassToggled: (String, Boolean) -> Unit,
    onAddExam: () -> Unit,
    onUpdateExam: () -> Unit,
    onDeleteExam: (String) -> Unit,
    onExtractNotes: (Exam, String) -> Unit = { _, _ -> },
    navController: NavController
) {
    Scaffold(
        bottomBar = { MyNavigationBar(navController) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onShowAddExamDialog,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Exam") },
                text = { Text("Add Exam") },
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
            // Content
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Text(
                    text = "Your Exams",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = "Manage your exams and assignments",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Loading indicator
                if (state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (state.error != null) {
                    // Error message
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
                            Button(onClick = { /* Retry logic */ }) {
                                Text("Retry")
                            }
                        }
                    }
                } else if (state.exams.isEmpty()) {
                    // Empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.Assignment,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No exams yet",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Create your first exam to get started",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = onShowAddExamDialog,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Create Exam")
                            }
                        }
                    }
                } else {
                    // List of exams
                    LazyColumn {
                        items(state.exams) { exam ->
                            ExamCard(
                                exam = exam,
                                onExamClick = {
                                    // Show edit exam dialog instead of navigating
                                    onShowEditExamDialog(exam)
                                },
                                onCorrectExam = {
                                    // Navigate to ExamCorrectionClassSelectionScreen
                                    navController.navigate(Routes.ExamCorrectionClassSelection.createRoute(exam.id))
                                },
                                onDelete = { onDeleteExam(exam.id) },
                                onExtractNotes = {
                                    // If exam has multiple classes, show dialog to select class
                                    if (exam.assignedClasses.size > 1) {
                                        // For simplicity, use the first class
                                        // In a real app, you would show a dialog to select the class
                                        val className = exam.assignedClasses.first()
                                        onExtractNotes(exam, className)
                                    } else if (exam.assignedClasses.size == 1) {
                                        // If exam has only one class, use that class
                                        val className = exam.assignedClasses.first()
                                        onExtractNotes(exam, className)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Add Exam Dialog
            if (state.showAddExamDialog) {
                AlertDialog(
                    onDismissRequest = onDismissAddExamDialog,
                    title = { 
                        Text(
                            "Add a new exam",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        ) 
                    },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = state.examName,
                                onValueChange = onExamNameChanged,
                                label = { Text("Exam Name") },
                                isError = state.examNameError != null,
                                supportingText = {
                                    state.examNameError?.let {
                                        Text(
                                            text = it,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Select classes for this exam:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (state.availableClasses.isEmpty()) {
                                Text(
                                    text = "No classes available. Please create classes first.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp)
                                ) {
                                    items(state.availableClasses) { className ->
                                        val isSelected = state.selectedClasses.contains(className)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = { checked ->
                                                    onClassToggled(className, checked)
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = className,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }

                                if (state.classSelectionError != null) {
                                    Text(
                                        text = state.classSelectionError,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = onAddExam,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = onDismissAddExamDialog) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Edit Exam Dialog
            if (state.showEditExamDialog) {
                AlertDialog(
                    onDismissRequest = onDismissEditExamDialog,
                    title = { 
                        Text(
                            "Edit Exam",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        ) 
                    },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = state.examName,
                                onValueChange = onExamNameChanged,
                                label = { Text("Exam Name") },
                                isError = state.examNameError != null,
                                supportingText = {
                                    state.examNameError?.let {
                                        Text(
                                            text = it,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false // Disable name editing for now
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Assign classes to this exam:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (state.availableClasses.isEmpty()) {
                                Text(
                                    text = "No classes available. Please create classes first.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp)
                                ) {
                                    items(state.availableClasses) { className ->
                                        val isSelected = state.selectedClasses.contains(className)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = { checked ->
                                                    onClassToggled(className, checked)
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = className,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }

                                if (state.classSelectionError != null) {
                                    Text(
                                        text = state.classSelectionError,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = onUpdateExam,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Update")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = onDismissEditExamDialog) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ExamCard(
    exam: Exam,
    onExamClick: () -> Unit,
    onCorrectExam: () -> Unit,
    onDelete: () -> Unit,
    onExtractNotes: () -> Unit = {}
) {
    Card(
        onClick = onExamClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exam icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Assignment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Exam details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = exam.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (exam.assignedClasses.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Class,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = "Classes: ${exam.assignedClasses.joinToString(", ")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(
                            text = "No classes assigned yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Exam",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Add correction button if classes are assigned
            if (exam.assignedClasses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onCorrectExam,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Correct Exam Papers")
                }

                // Add Extract Notes button if exam has student marks
                if (exam.studentMarks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onExtractNotes,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text("Extract Class Notes as PDF")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExamsScreenPreview() {
    QcmasterTheme {
        val dummyState = ExamsUiState(
            exams = listOf(
                Exam(
                    id = "1",
                    name = "Math Exam",
                    assignedClasses = listOf("Computer Science", "Mathematics")
                ),
                Exam(
                    id = "2",
                    name = "Physics Exam",
                    assignedClasses = emptyList()
                )
            ),
            isLoading = false,
            error = null,
            showAddExamDialog = false,
            availableClasses = listOf("Computer Science", "Mathematics", "Physics")
        )

        ExamsScreenContent(
            state = dummyState,
            onShowAddExamDialog = {},
            onDismissAddExamDialog = {},
            onShowEditExamDialog = {},
            onDismissEditExamDialog = {},
            onExamNameChanged = {},
            onClassToggled = { _, _ -> },
            onAddExam = {},
            onUpdateExam = {},
            onDeleteExam = {},
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExamsScreenEmptyPreview() {
    QcmasterTheme {
        val dummyState = ExamsUiState(
            exams = emptyList(),
            isLoading = false,
            error = null,
            showAddExamDialog = false
        )

        ExamsScreenContent(
            state = dummyState,
            onShowAddExamDialog = {},
            onDismissAddExamDialog = {},
            onShowEditExamDialog = {},
            onDismissEditExamDialog = {},
            onExamNameChanged = {},
            onClassToggled = { _, _ -> },
            onAddExam = {},
            onUpdateExam = {},
            onDeleteExam = {},
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExamsScreenLoadingPreview() {
    QcmasterTheme {
        val dummyState = ExamsUiState(
            exams = emptyList(),
            isLoading = true,
            error = null,
            showAddExamDialog = false
        )

        ExamsScreenContent(
            state = dummyState,
            onShowAddExamDialog = {},
            onDismissAddExamDialog = {},
            onShowEditExamDialog = {},
            onDismissEditExamDialog = {},
            onExamNameChanged = {},
            onClassToggled = { _, _ -> },
            onAddExam = {},
            onUpdateExam = {},
            onDeleteExam = {},
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExamsScreenErrorPreview() {
    QcmasterTheme {
        val dummyState = ExamsUiState(
            exams = emptyList(),
            isLoading = false,
            error = "Failed to load exams. Please try again.",
            showAddExamDialog = false
        )

        ExamsScreenContent(
            state = dummyState,
            onShowAddExamDialog = {},
            onDismissAddExamDialog = {},
            onShowEditExamDialog = {},
            onDismissEditExamDialog = {},
            onExamNameChanged = {},
            onClassToggled = { _, _ -> },
            onAddExam = {},
            onUpdateExam = {},
            onDeleteExam = {},
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExamsScreenDialogPreview() {
    QcmasterTheme {
        val dummyState = ExamsUiState(
            exams = listOf(
                Exam(
                    id = "1",
                    name = "Math Exam",
                    assignedClasses = listOf("Computer Science", "Mathematics")
                )
            ),
            isLoading = false,
            error = null,
            showAddExamDialog = true,
            examName = "Physics Exam",
            availableClasses = listOf("Computer Science", "Mathematics", "Physics"),
            selectedClasses = listOf("Computer Science")
        )

        ExamsScreenContent(
            state = dummyState,
            onShowAddExamDialog = {},
            onDismissAddExamDialog = {},
            onShowEditExamDialog = {},
            onDismissEditExamDialog = {},
            onExamNameChanged = {},
            onClassToggled = { _, _ -> },
            onAddExam = {},
            onUpdateExam = {},
            onDeleteExam = {},
            navController = rememberNavController()
        )
    }
}
