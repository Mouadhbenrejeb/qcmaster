package com.example.qcmaster.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Class
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.qcmaster.R
import com.example.qcmaster.Routes
import com.example.qcmaster.SessionManager
import com.example.qcmaster.components.MyNavigationBar
import com.example.qcmaster.data.FirebaseAuthRepository
import com.example.qcmaster.models.Professor
import com.example.qcmaster.ui.theme.QcmasterTheme
import com.example.qcmaster.viewmodels.HomeUiState
import com.example.qcmaster.viewmodels.HomeViewModel

@Composable
fun HomeScreen(
    profName: String,
    profEmail: String,
    navController: NavController
) {
    val viewModel: HomeViewModel = viewModel()

    HomeScreenContent(
        state = viewModel.state,
        onShowProfile = viewModel.onShowProfileDialog,
        onDismissProfile = viewModel.onDismissProfileDialog,
        onLogout = { viewModel.onLogout {
            navController.navigate(Routes.Auth.route) {
                popUpTo(Routes.Home.route) { inclusive = true }
            }
        }},
        onNavigateToExams = { navController.navigate(Routes.Exams.route) },
        onNavigateToClasses = { navController.navigate(Routes.Classes.route) },
        onNavigateToStudents = { navController.navigate(Routes.Students.route) },
        navController = navController
    )
}

@Composable
fun HomeScreenContent(
    state: HomeUiState,
    onShowProfile: () -> Unit,
    onDismissProfile: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToExams: () -> Unit,
    onNavigateToClasses: () -> Unit,
    onNavigateToStudents: () -> Unit,
    navController: NavController
) {
    Scaffold(
        bottomBar = { MyNavigationBar(navController) }
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
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            Text(
                                text = "Welcome back,",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "Prof. ${state.professor?.name ?: ""}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(
                            onClick = onShowProfile,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Profile Icon",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Profile")
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Dashboard Cards
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Cards Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Exams Card
                        DashboardCard(
                            title = "Exams",
                            icon = Icons.Outlined.Description,
                            onClick = onNavigateToExams,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // Classes Card
                        DashboardCard(
                            title = "Classes",
                            icon = Icons.Outlined.Class,
                            onClick = onNavigateToClasses,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Students Card
                    DashboardCard(
                        title = "Students",
                        icon = Icons.Outlined.Person,
                        onClick = onNavigateToStudents,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Quick Actions
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Scan Exam Button
                    Button(
                        onClick = onNavigateToExams,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "📄 Scan Exam", 
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        // Profile Dialog
        if (state.showProfileDialog) {
            AlertDialog(
                onDismissRequest = onDismissProfile,
                title = {
                    Text(
                        text = "Profile Details",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.qcm_logo),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(40.dp))
                                .align(Alignment.CenterHorizontally)
                        )

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        Text(
                            text = "👨‍🏫 Name: Professor ${state.professor?.name ?: ""}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "📧 Email: ${state.professor?.email ?: ""}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onDismissProfile,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Logout")
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "$title Icon",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    QcmasterTheme {
        val dummyState = HomeUiState(
            professor = Professor(
                cin = "12345678",
                password = "",
                name = "John Doe",
                email = "john.doe@example.com"
            ),
            isLoading = false,
            error = null,
            showProfileDialog = false
        )

        HomeScreenContent(
            state = dummyState,
            onShowProfile = {},
            onDismissProfile = {},
            onLogout = {},
            onNavigateToExams = {},
            onNavigateToClasses = {},
            onNavigateToStudents = {},
            navController = rememberNavController()
        )
    }
}
