package com.example.qcmaster.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Class
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Description
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
import com.example.qcmaster.ClassModel
import com.example.qcmaster.components.MyNavigationBar
import com.example.qcmaster.ui.theme.QcmasterTheme
import com.example.qcmaster.viewmodels.ClassesUiState
import com.example.qcmaster.viewmodels.ClassesViewModel

@Composable
fun ClassesScreen(navController: NavController) {
    val viewModel: ClassesViewModel = viewModel()

    ClassesScreenContent(
        state = viewModel.state,
        onShowAddClassDialog = viewModel.onShowAddClassDialog,
        onDismissAddClassDialog = viewModel.onDismissAddClassDialog,
        onNewClassNameChanged = viewModel.onNewClassNameChanged,
        onNewClassCodeChanged = viewModel.onNewClassCodeChanged,
        onNewClassDescriptionChanged = viewModel.onNewClassDescriptionChanged,
        onAddClass = viewModel::addClass,
        onDeleteClass = viewModel::deleteClass,
        onRetry = viewModel.onRetry,
        onClearError = viewModel.onClearError,
        onNavigateToClassExams = { className ->
            navController.navigate("class_exams/$className")
        },
        navController = navController
    )
}

@Composable
fun ClassesScreenContent(
    state: ClassesUiState,
    onShowAddClassDialog: () -> Unit,
    onDismissAddClassDialog: () -> Unit,
    onNewClassNameChanged: (String) -> Unit,
    onNewClassCodeChanged: (String) -> Unit,
    onNewClassDescriptionChanged: (String) -> Unit,
    onAddClass: () -> Unit,
    onDeleteClass: (String) -> Unit,
    onRetry: () -> Unit,
    onClearError: () -> Unit,
    onNavigateToClassExams: (String) -> Unit,
    navController: NavController
) {
    Scaffold(
        bottomBar = { MyNavigationBar(navController) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onShowAddClassDialog,
                icon = { Icon(Icons.Filled.Add, contentDescription = "Add Class") },
                text = { Text("Add Class") },
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
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onRetry,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            } else {
                // Content state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    // Header
                    Text(
                        text = "Your Classes",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Manage your classes and students",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                    )

                    if (state.classes.isEmpty()) {
                        // Empty state
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Class,
                                    contentDescription = "No Classes",
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No classes added yet",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Add your first class by clicking the button below",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = onShowAddClassDialog,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = "Add Class"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add Class")
                                }
                            }
                        }
                    } else {
                        // Classes list
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.classes) { classItem ->
                                ClassCard(
                                    classModel = classItem,
                                    onClassClick = { onNavigateToClassExams(classItem.name) },
                                    onDeleteClick = { onDeleteClass(classItem.code) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add Class Dialog
        if (state.showAddClassDialog) {
            AlertDialog(
                onDismissRequest = onDismissAddClassDialog,
                title = { 
                    Text(
                        "Add a new class",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = state.newClassName,
                            onValueChange = onNewClassNameChanged,
                            label = { Text("Class Name") },
                            isError = state.duplicateError,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Class,
                                    contentDescription = "Class Name",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )

                        if (state.duplicateError) {
                            Text(
                                text = "This class already exists!",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        OutlinedTextField(
                            value = state.newClassCode,
                            onValueChange = onNewClassCodeChanged,
                            label = { Text("Class Code") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Code,
                                    contentDescription = "Class Code",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )

                        OutlinedTextField(
                            value = state.newClassDescription,
                            onValueChange = onNewClassDescriptionChanged,
                            label = { Text("Description (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Description,
                                    contentDescription = "Description",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onAddClass,
                        enabled = state.newClassName.isNotBlank() && state.newClassCode.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = onDismissAddClassDialog,
                        shape = RoundedCornerShape(12.dp)
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
fun ClassCard(
    classModel: ClassModel,
    onClassClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClassClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Class,
                            contentDescription = "Class Icon",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = classModel.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Code: ${classModel.code}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Class",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (classModel.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = classModel.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ClassesScreenPreview() {
    QcmasterTheme {
        val dummyState = ClassesUiState(
            classes = listOf(
                ClassModel(
                    name = "Mobile Development",
                    code = "MOB101",
                    description = "Learn to build mobile apps for Android and iOS"
                ),
                ClassModel(
                    name = "Web Development",
                    code = "WEB101",
                    description = "Learn to build web applications"
                ),
                ClassModel(
                    name = "Database Systems",
                    code = "DB101",
                    description = ""
                )
            ),
            isLoading = false,
            error = null,
            showAddClassDialog = false
        )

        ClassesScreenContent(
            state = dummyState,
            onShowAddClassDialog = {},
            onDismissAddClassDialog = {},
            onNewClassNameChanged = {},
            onNewClassCodeChanged = {},
            onNewClassDescriptionChanged = {},
            onAddClass = {},
            onDeleteClass = {},
            onRetry = {},
            onClearError = {},
            onNavigateToClassExams = {},
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ClassesScreenEmptyPreview() {
    QcmasterTheme {
        val dummyState = ClassesUiState(
            classes = emptyList(),
            isLoading = false,
            error = null,
            showAddClassDialog = false
        )

        ClassesScreenContent(
            state = dummyState,
            onShowAddClassDialog = {},
            onDismissAddClassDialog = {},
            onNewClassNameChanged = {},
            onNewClassCodeChanged = {},
            onNewClassDescriptionChanged = {},
            onAddClass = {},
            onDeleteClass = {},
            onRetry = {},
            onClearError = {},
            onNavigateToClassExams = {},
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ClassesScreenLoadingPreview() {
    QcmasterTheme {
        val dummyState = ClassesUiState(
            classes = emptyList(),
            isLoading = true,
            error = null,
            showAddClassDialog = false
        )

        ClassesScreenContent(
            state = dummyState,
            onShowAddClassDialog = {},
            onDismissAddClassDialog = {},
            onNewClassNameChanged = {},
            onNewClassCodeChanged = {},
            onNewClassDescriptionChanged = {},
            onAddClass = {},
            onDeleteClass = {},
            onRetry = {},
            onClearError = {},
            onNavigateToClassExams = {},
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ClassesScreenErrorPreview() {
    QcmasterTheme {
        val dummyState = ClassesUiState(
            classes = emptyList(),
            isLoading = false,
            error = "Failed to load classes",
            showAddClassDialog = false
        )

        ClassesScreenContent(
            state = dummyState,
            onShowAddClassDialog = {},
            onDismissAddClassDialog = {},
            onNewClassNameChanged = {},
            onNewClassCodeChanged = {},
            onNewClassDescriptionChanged = {},
            onAddClass = {},
            onDeleteClass = {},
            onRetry = {},
            onClearError = {},
            onNavigateToClassExams = {},
            navController = rememberNavController()
        )
    }
}
