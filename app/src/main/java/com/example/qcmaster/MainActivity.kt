package com.example.qcmaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.qcmaster.ui.screens.AuthScreen
import com.example.qcmaster.ui.screens.HomeScreen
import com.example.qcmaster.ui.screens.RegisterScreen
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
import com.example.qcmaster.SessionManager.profEmail

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SessionManager.init(this)

        // ✅ Request camera permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1001)
        }

        setContent {
            QcmasterTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    composable("auth") { AuthScreen(navController) }
                    composable("register") { RegisterScreen(navController) }

                    composable("home/{profName}/{profEmail}") { backStackEntry ->
                        val profName = backStackEntry.arguments?.getString("profName") ?: "Unknown"
                        val profEmail = backStackEntry.arguments?.getString("profEmail") ?: "Unknown"
                        HomeScreen(profName, profEmail, navController)
                    }

                    composable("home") { backStackEntry ->
                        val profName = "Unknown"
                        val profEmail = "test@test.com"
                        HomeScreen(profName, profEmail, navController)
                    }

                    composable("classes") { ClassesScreen(navController) }
                    composable("students") { StudentsScreen(navController) }
                    composable("exams") { ExamsScreen(navController) }

                    composable("assign_classes_to_exam/{examName}") { backStackEntry ->
                        val examName = backStackEntry.arguments?.getString("examName") ?: ""
                        AssignClassesToExamScreen(navController, examName)
                    }

                    composable("scan_exam/{examName}") { backStackEntry ->
                        val examName = backStackEntry.arguments?.getString("examName") ?: ""
                        ScanExamScreen(navController, examName)
                    }

                    composable("class_exams/{className}") { backStackEntry ->
                        val className = backStackEntry.arguments?.getString("className") ?: ""
                        ClassExamsScreen(navController, className)
                    }

                    composable("class_exam_grades/{className}/{examName}") { backStackEntry ->
                        val className = backStackEntry.arguments?.getString("className") ?: ""
                        val examName = backStackEntry.arguments?.getString("examName") ?: ""
                        ExamStudentGradesScreen(className, examName)
                    }


                    composable("correction_comparison_screen") { backStackEntry ->
                        val correctAnswers = backStackEntry.savedStateHandle.get<List<String>>("correctAnswers") ?: emptyList()
                        val scannedAnswers = backStackEntry.savedStateHandle.get<List<String>>("scannedAnswers") ?: emptyList()
                        CorrectionComparisonScreen(correctAnswers, scannedAnswers)
                    }

                }
            }
        }
    }
}
