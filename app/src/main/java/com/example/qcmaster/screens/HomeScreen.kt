package com.example.qcmaster.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme  // Add this import
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qcmaster.components.MyNavigationBar
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun HomeScreen(profName: String, navController: NavController) {
    // Scaffold with the bottom navigation bar
    Scaffold(
        bottomBar = { MyNavigationBar(navController) } // Pass navController to MyNavigationBar
    ) { paddingValues ->
        // Main content area
        Box(modifier = Modifier.padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Welcome, Prof. $profName",
                    style = MaterialTheme.typography.headlineSmall,  // Updated to headlineSmall
                    fontSize = 24.sp
                )

                // You can add more content below as needed
            }
        }
    }
}
