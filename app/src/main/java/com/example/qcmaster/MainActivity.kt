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
                    composable("home/{profName}/{profEmail}") { backStackEntry ->
                        val profName = backStackEntry.arguments?.getString("profName") ?: "Unknown"
                        val profEmail = backStackEntry.arguments?.getString("profEmail") ?: "Unknown"

                        HomeScreen(profName, profEmail, navController)
                    }

                    composable("classes") { ClassesScreen(navController) }  // Pass navController here
                    composable("students") { StudentsScreen(navController) }  // Pass navController here
                }
            }
        }
    }
}






