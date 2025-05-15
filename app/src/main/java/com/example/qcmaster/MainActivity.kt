package com.example.qcmaster

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.qcmaster.screens.AuthScreen
import com.example.qcmaster.screens.HomeScreen
import com.example.qcmaster.screens.RegisterScreen
import com.example.qcmaster.screens.ClassesScreen
import com.example.qcmaster.screens.StudentsScreen
import com.example.qcmaster.screens.ExamsScreen
import com.example.qcmaster.screens.AssignClassesToExamScreen
import com.example.qcmaster.ui.theme.QcmasterTheme
import com.example.qcmaster.screens.ScanExamScreen
import com.example.qcmaster.screens.ClassExamsScreen
import com.example.qcmaster.screens.ExamStudentGradesScreen
import com.example.qcmaster.screens.CorrectionComparisonScreen
import android.Manifest
import android.content.pm.PackageManager
import com.example.qcmaster.data.FirebaseAuthRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.opencv.android.OpenCVLoader
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private lateinit var authRepository: FirebaseAuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SessionManager.init(this)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize the FirebaseAuthRepository
        authRepository = FirebaseAuthRepository.getInstance()

        // Request camera permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1001)
        }

        OpenCVLoader.initLocal()

        // Monitor authentication state changes
        lifecycleScope.launch {
            authRepository.currentUser.collectLatest { user ->
                if (user == null && SessionManager.getEmail().isNotEmpty()) {
                    // User was logged in but is no longer logged in
                    SessionManager.clearSession()
                    Toast.makeText(
                        this@MainActivity,
                        "You have been logged out. Please sign in again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        setContent {
            QcmasterTheme {
                val navController = rememberNavController()
                val currentUser by authRepository.currentUser.collectAsState(initial = null)
                val isLoggedIn = currentUser != null || SessionManager.getEmail().isNotEmpty()

                NavHost(
                    navController = navController,
                    startDestination = if (isLoggedIn) Route.HomeScreen.route else Route.AuthScreen.route
                ) {
                    // Auth screens
                    composable(Route.AuthScreen.route) { 
                        AuthScreen(
                            onNavigateToHome = {
                                navController.navigate(Route.HomeScreen.route) {
                                    popUpTo(Route.AuthScreen.route) { inclusive = true }
                                }
                            },
                            onNavigateToRegister = {
                                navController.navigate(Route.RegisterScreen.route)
                            }
                        ) 
                    }

                    composable(Route.RegisterScreen.route) { 
                        RegisterScreen(
                            onNavigateToHome = {
                                navController.navigate(Route.HomeScreen.route) {
                                    popUpTo(Route.RegisterScreen.route) { inclusive = true }
                                }
                            },
                            onNavigateToLogin = {
                                navController.navigate(Route.AuthScreen.route) {
                                    popUpTo(Route.RegisterScreen.route) { inclusive = true }
                                }
                            }
                        ) 
                    }

                    // Home screen
                    composable(Route.HomeScreen.route) {
                        val profName = SessionManager.profName
                        val profEmail = SessionManager.profEmail

                        // Check if user is still logged in
                        LaunchedEffect(currentUser) {
                            if (currentUser == null && SessionManager.getEmail().isEmpty()) {
                                navController.navigate(Route.AuthScreen.route) {
                                    popUpTo(Route.HomeScreen.route) { inclusive = true }
                                }
                            }
                        }

                        HomeScreen(profName, profEmail, navController)
                    }

                    // Main screens
                    composable(Route.ClassesScreen.route) { 
                        ClassesScreen(navController) 
                    }

                    composable(Route.StudentsScreen.route) { 
                        StudentsScreen(navController) 
                    }

                    composable(Route.ExamsScreen.route) { 
                        ExamsScreen(navController) 
                    }

                    // Screens with parameters
                    composable(
                        route = Route.AssignClassesToExamScreen.route,
                        arguments = listOf(
                            navArgument(Route.AssignClassesToExamScreen.examNameArg) {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->
                        val examName = backStackEntry.arguments?.getString(Route.AssignClassesToExamScreen.examNameArg) ?: ""
                        AssignClassesToExamScreen(navController, examName)
                    }

                    composable(
                        route = Route.ScanExamScreen.route,
                        arguments = listOf(
                            navArgument(Route.ScanExamScreen.examNameArg) {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->
                        val examName = backStackEntry.arguments?.getString(Route.ScanExamScreen.examNameArg) ?: ""
                        ScanExamScreen(navController, examName)
                    }

                    composable(
                        route = Route.ClassExamsScreen.route,
                        arguments = listOf(
                            navArgument(Route.ClassExamsScreen.classNameArg) {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->
                        val className = backStackEntry.arguments?.getString(Route.ClassExamsScreen.classNameArg) ?: ""
                        ClassExamsScreen(navController, className)
                    }

                    composable(
                        route = Route.ExamStudentGradesScreen.route,
                        arguments = listOf(
                            navArgument(Route.ExamStudentGradesScreen.classNameArg) {
                                type = NavType.StringType
                            },
                            navArgument(Route.ExamStudentGradesScreen.examNameArg) {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->
                        val className = backStackEntry.arguments?.getString(Route.ExamStudentGradesScreen.classNameArg) ?: ""
                        val examName = backStackEntry.arguments?.getString(Route.ExamStudentGradesScreen.examNameArg) ?: ""
                        ExamStudentGradesScreen(className, examName)
                    }

                    composable(Route.CorrectionComparisonScreen.route) { backStackEntry ->
                        val correctAnswers = backStackEntry.savedStateHandle.get<List<String>>("correctAnswers") ?: emptyList()
                        val scannedAnswers = backStackEntry.savedStateHandle.get<List<String>>("scannedAnswers") ?: emptyList()
                        CorrectionComparisonScreen(correctAnswers, scannedAnswers)
                    }
                }
            }
        }
    }
}
