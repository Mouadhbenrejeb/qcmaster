package com.example.qcmaster

sealed interface Route {
    val route: String

    data object AuthScreen : Route {
        override val route: String = "auth"
    }

    data object RegisterScreen : Route {
        override val route: String = "register"
    }

    data object HomeScreen : Route {
        override val route: String = "home"
    }

    data object ClassesScreen : Route {
        override val route: String = "classes"
    }

    data object StudentsScreen : Route {
        override val route: String = "students"
    }

    data object ExamsScreen : Route {
        override val route: String = "exams"
    }

    data class AssignClassesToExamScreen(val examName: String) : Route {
        override val route: String = "assign_classes_to_exam/$examName"

        companion object {
            const val route = "assign_classes_to_exam/{examName}"
            const val examNameArg = "examName"
        }
    }

    data class ScanExamScreen(val examName: String) : Route {
        override val route: String = "scan_exam/$examName"

        companion object {
            const val route = "scan_exam/{examName}"
            const val examNameArg = "examName"
        }
    }

    data class ClassExamsScreen(val className: String) : Route {
        override val route: String = "class_exams/$className"

        companion object {
            const val route = "class_exams/{className}"
            const val classNameArg = "className"
        }
    }

    data class ExamStudentGradesScreen(val className: String, val examName: String) : Route {
        override val route: String = "class_exam_grades/$className/$examName"

        companion object {
            const val route = "class_exam_grades/{className}/{examName}"
            const val classNameArg = "className"
            const val examNameArg = "examName"
        }
    }

    data object CorrectionComparisonScreen : Route {
        override val route: String = "correction_comparison_screen"
    }
}
