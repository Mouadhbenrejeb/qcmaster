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
import com.example.qcmaster.screens.ExamAnswerPaperUploadScreen
import com.example.qcmaster.screens.ExamCorrectionClassSelectionScreen
import com.example.qcmaster.screens.ExamCorrectionStudentSelectionScreen
import com.example.qcmaster.screens.ExamCorrectionPaperUploadScreen
import com.example.qcmaster.screens.DebugScreen
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
//                    startDestination = Routes.Debug.route
                    startDestination = if (isLoggedIn) Routes.Home.route else Routes.Auth.route
                ) {
                    // Auth screens
                    composable(Routes.Auth.route) { 
                        AuthScreen(
                            onNavigateToHome = {
                                navController.navigate(Routes.Home.route) {
                                    popUpTo(Routes.Auth.route) { inclusive = true }
                                }
                            },
                            onNavigateToRegister = {
                                navController.navigate(Routes.Register.route)
                            }
                        ) 
                    }

                    composable(Routes.Register.route) { 
                        RegisterScreen(
                            onNavigateToHome = {
                                navController.navigate(Routes.Home.route) {
                                    popUpTo(Routes.Register.route) { inclusive = true }
                                }
                            },
                            onNavigateToLogin = {
                                navController.navigate(Routes.Auth.route) {
                                    popUpTo(Routes.Register.route) { inclusive = true }
                                }
                            }
                        ) 
                    }

                    // Home screen
                    composable(Routes.Home.route) {
                        val profName = SessionManager.profName
                        val profEmail = SessionManager.profEmail

                        // Check if user is still logged in
                        LaunchedEffect(currentUser) {
                            if (currentUser == null && SessionManager.getEmail().isEmpty()) {
                                navController.navigate(Routes.Auth.route) {
                                    popUpTo(Routes.Home.route) { inclusive = true }
                                }
                            }
                        }

                        HomeScreen(profName, profEmail, navController)
                    }

                    // Main screens
                    composable(Routes.Classes.route) { 
                        ClassesScreen(navController) 
                    }

                    composable(Routes.Students.route) { 
                        StudentsScreen(navController) 
                    }

                    composable(Routes.Exams.route) { 
                        ExamsScreen(navController) 
                    }

                    // Screens with parameters
                    composable(
                        route = Routes.AssignClassesToExam.route,
                        arguments = listOf(
                            navArgument(Routes.AssignClassesToExam.examIdArg) {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->
                        val examId = backStackEntry.arguments?.getString(Routes.AssignClassesToExam.examIdArg) ?: ""
                        AssignClassesToExamScreen(navController, examId)
                    }

                    composable(
                        route = Routes.ScanExam.route,
                        arguments = listOf(
                            navArgument(Routes.ScanExam.examIdArg) {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->
                        val examId = backStackEntry.arguments?.getString(Routes.ScanExam.examIdArg) ?: ""
                        ScanExamScreen(navController, examId)
                    }

                    composable(
                        route = Routes.ClassExams.route,
                        arguments = listOf(
                            navArgument(Routes.ClassExams.classNameArg) {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->
                        val className = backStackEntry.arguments?.getString(Routes.ClassExams.classNameArg) ?: ""
                        ClassExamsScreen(navController, className)
                    }

                    composable(
                        route = Routes.ExamStudentGrades.route,
                        arguments = listOf(
                            navArgument(Routes.ExamStudentGrades.classNameArg) {
                                type = NavType.StringType
                            },
                            navArgument(Routes.ExamStudentGrades.examNameArg) {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->
                        val className = backStackEntry.arguments?.getString(Routes.ExamStudentGrades.classNameArg) ?: ""
                        val examName = backStackEntry.arguments?.getString(Routes.ExamStudentGrades.examNameArg) ?: ""
                        ExamStudentGradesScreen(className, examName)
                    }

                    composable(
                        route = Routes.CorrectionComparison.route,
                        arguments = listOf(
                            navArgument(Routes.CorrectionComparison.examIdArg) {
                                type = NavType.StringType
                            },
                            navArgument(Routes.CorrectionComparison.classNameArg) {
                                type = NavType.StringType
                            },
                            navArgument(Routes.CorrectionComparison.studentIdArg) {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->
                        val examId = backStackEntry.arguments?.getString(Routes.CorrectionComparison.examIdArg) ?: ""
                        val className = backStackEntry.arguments?.getString(Routes.CorrectionComparison.classNameArg) ?: ""
                        val studentId = backStackEntry.arguments?.getString(Routes.CorrectionComparison.studentIdArg) ?: ""
                        CorrectionComparisonScreen(navController, examId, className, studentId)
                    }

                    // Exam correction screens
                    composable(
                        route = Routes.ExamAnswerPaperUpload.route,
                        arguments = listOf(
                            navArgument(Routes.ExamAnswerPaperUpload.examIdArg) {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->
                        val examId = backStackEntry.arguments?.getString(Routes.ExamAnswerPaperUpload.examIdArg) ?: ""
                        ExamAnswerPaperUploadScreen(navController, examId)
                    }

                    composable(
                        route = Routes.ExamCorrectionClassSelection.route,
                        arguments = listOf(
                            navArgument(Routes.ExamCorrectionClassSelection.examIdArg) {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->
                        val examId = backStackEntry.arguments?.getString(Routes.ExamCorrectionClassSelection.examIdArg) ?: ""
                        ExamCorrectionClassSelectionScreen(navController, examId)
                    }

                    composable(
                        route = Routes.ExamCorrectionStudentSelection.route,
                        arguments = listOf(
                            navArgument(Routes.ExamCorrectionStudentSelection.examIdArg) {
                                type = NavType.StringType
                            },
                            navArgument(Routes.ExamCorrectionStudentSelection.classNameArg) {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->
                        val examId = backStackEntry.arguments?.getString(Routes.ExamCorrectionStudentSelection.examIdArg) ?: ""
                        val className = backStackEntry.arguments?.getString(Routes.ExamCorrectionStudentSelection.classNameArg) ?: ""
                        ExamCorrectionStudentSelectionScreen(navController, examId, className)
                    }

                    composable(
                        route = Routes.ExamCorrectionPaperUpload.route,
                        arguments = listOf(
                            navArgument(Routes.ExamCorrectionPaperUpload.examIdArg) {
                                type = NavType.StringType
                            },
                            navArgument(Routes.ExamCorrectionPaperUpload.classNameArg) {
                                type = NavType.StringType
                            },
                            navArgument(Routes.ExamCorrectionPaperUpload.studentIdArg) {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->
                        val examId = backStackEntry.arguments?.getString(Routes.ExamCorrectionPaperUpload.examIdArg) ?: ""
                        val className = backStackEntry.arguments?.getString(Routes.ExamCorrectionPaperUpload.classNameArg) ?: ""
                        val studentId = backStackEntry.arguments?.getString(Routes.ExamCorrectionPaperUpload.studentIdArg) ?: ""
                        ExamCorrectionPaperUploadScreen(navController, examId, className, studentId)
                    }

                    // Debug screen
                    composable(Routes.Debug.route) {
                        DebugScreen(navController)
                    }
                }
            }
        }
    }
}
