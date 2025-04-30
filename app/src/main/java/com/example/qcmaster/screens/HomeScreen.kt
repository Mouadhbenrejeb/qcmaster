package com.example.qcmaster.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.qcmaster.SessionManager
import com.example.qcmaster.components.MyNavigationBar

@Composable
fun HomeScreen(
    profName: String,
    profEmail: String,
    navController: NavController
) {
    var showProfile by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = { MyNavigationBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Welcome, Prof. $profName",
                    style = MaterialTheme.typography.titleLarge
                )

                Button(
                    onClick = { showProfile = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(text = "Profile")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Home",
                style = MaterialTheme.typography.titleMedium
            )
        }

        // --- Profile Dialog ---
        if (showProfile) {
            AlertDialog(
                onDismissRequest = { showProfile = false },
                title = {
                    Text(text = "Profile Details")
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "👨‍🏫 Name: Professor. $profName")
                        Text(text = "📧 Email: $profEmail")
                    }
                },
                confirmButton = {
                    Button(onClick = { showProfile = false }) {
                        Text("Close")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        // Clear session and go to auth screen
                        SessionManager.clearSession()
                        navController.navigate("auth") {
                            // Pop up the entire back stack so the user cannot navigate back
                            popUpTo("auth") { inclusive = true }
                        }
                    }) {
                        Text("Logout")
                    }
                }
            )
        }
    }
}
