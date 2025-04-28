package com.example.qcmaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.qcmaster.ui.theme.QcmasterTheme
import com.example.qcmaster.ui.screens.AuthScreen
import com.example.qcmaster.ui.screens.HomeScreen
import com.example.qcmaster.ui.screens.RegisterScreen
import com.example.qcmaster.screens.ClassesScreen
import com.example.qcmaster.screens.StudentsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QcmasterTheme {
                val navController = rememberNavController()

                // Navigation setup
                NavHost(navController = navController, startDestination = "auth") {
                    composable("auth") { AuthScreen(navController) }
                    composable("register") { RegisterScreen(navController) }
                    composable("home/{profName}") { backStackEntry ->
                        val profName = backStackEntry.arguments?.getString("profName") ?: "Unknown"
                        HomeScreen(profName, navController)  // Pass the navController here
                    }
                    composable("classes") { ClassesScreen(navController) }
                    composable("students") { StudentsScreen(navController) }
                }
            }
        }
    }
}

