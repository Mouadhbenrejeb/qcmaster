package com.example.qcmaster

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kotlinx.serialization.Serializable

// Base interface for all routes
sealed interface Route {
    val route: String
}

// Screens without arguments
object Routes {
    // Simple screens without arguments
    object Auth : Route {
        override val route = "auth"
    }

    object Register : Route {
        override val route = "register"
    }

    object Home : Route {
        override val route = "home"
    }

    object Classes : Route {
        override val route = "classes"
    }

    object Students : Route {
        override val route = "students"
    }

    object Exams : Route {
        override val route = "exams"
    }

    object Debug : Route {
        override val route = "debug"
    }

    // Screens with a single examId parameter
    object AssignClassesToExam : Route {
        override val route = "assign_classes_to_exam/{examId}"
        const val examIdArg = "examId"

        fun createRoute(examId: String) = "assign_classes_to_exam/$examId"
    }

    object ScanExam : Route {
        override val route = "scan_exam/{examId}"
        const val examIdArg = "examId"

        fun createRoute(examId: String) = "scan_exam/$examId"
    }

    object ExamAnswerPaperUpload : Route {
        override val route = "exam_answer_paper_upload/{examId}"
        const val examIdArg = "examId"

        fun createRoute(examId: String) = "exam_answer_paper_upload/$examId"
    }

    object ExamCorrectionClassSelection : Route {
        override val route = "exam_correction_class_selection/{examId}"
        const val examIdArg = "examId"

        fun createRoute(examId: String) = "exam_correction_class_selection/$examId"
    }

    // Screen with a single className parameter
    object ClassExams : Route {
        override val route = "class_exams/{className}"
        const val classNameArg = "className"

        fun createRoute(className: String) = "class_exams/$className"
    }

    // Screens with multiple parameters
    object ExamStudentGrades : Route {
        override val route = "class_exam_grades/{className}/{examName}"
        const val classNameArg = "className"
        const val examNameArg = "examName"

        fun createRoute(className: String, examName: String) = "class_exam_grades/$className/$examName"
    }

    object ExamCorrectionStudentSelection : Route {
        override val route = "exam_correction_student_selection/{examId}/{className}"
        const val examIdArg = "examId"
        const val classNameArg = "className"

        fun createRoute(examId: String, className: String) = "exam_correction_student_selection/$examId/$className"
    }

    object CorrectionComparison : Route {
        override val route = "correction_comparison_screen/{examId}/{className}/{studentId}"
        const val examIdArg = "examId"
        const val classNameArg = "className"
        const val studentIdArg = "studentId"

        fun createRoute(examId: String, className: String, studentId: String) = 
            "correction_comparison_screen/$examId/$className/$studentId"
    }

    object ExamCorrectionPaperUpload : Route {
        override val route = "exam_correction_paper_upload/{examId}/{className}/{studentId}"
        const val examIdArg = "examId"
        const val classNameArg = "className"
        const val studentIdArg = "studentId"

        fun createRoute(examId: String, className: String, studentId: String) = 
            "exam_correction_paper_upload/$examId/$className/$studentId"
    }
}

// Serializable argument classes for type-safe navigation
@Serializable
data class ExamIdArg(val examId: String)

@Serializable
data class ClassNameArg(val className: String)

@Serializable
data class ExamClassArgs(val examId: String, val className: String)

@Serializable
data class ClassExamArgs(val className: String, val examName: String)

@Serializable
data class CorrectionArgs(val examId: String, val className: String, val studentId: String)
