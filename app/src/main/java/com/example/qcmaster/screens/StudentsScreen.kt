package com.example.qcmaster.screens
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme  // Add this import
import androidx.compose.material3.Scaffold

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qcmaster.components.MyNavigationBar

import androidx.navigation.NavController
import com.ui.components.LinearDeterminateIndicator
@Composable
fun StudentsScreen(navController: NavController) {
    Scaffold(
        bottomBar = { MyNavigationBar(navController) } // Keep the nav bar visible
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Text(text = "This is the Students Screen")

                // Your content here (e.g., progress indicator)
                LinearDeterminateIndicator()
            }
        }
    }
}
