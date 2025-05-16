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

    data class AssignClassesToExamScreen(val examId: String) : Route {
        override val route: String = "assign_classes_to_exam/$examId"

        companion object {
            const val route = "assign_classes_to_exam/{examId}"
            const val examIdArg = "examId"
        }
    }

    data class ScanExamScreen(val examId: String) : Route {
        override val route: String = "scan_exam/$examId"

        companion object {
            const val route = "scan_exam/{examId}"
            const val examIdArg = "examId"
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

    data class CorrectionComparisonScreen(val examId: String, val className: String, val studentId: String) : Route {
        override val route: String = "correction_comparison_screen/$examId/$className/$studentId"

        companion object {
            const val route = "correction_comparison_screen/{examId}/{className}/{studentId}"
            const val examIdArg = "examId"
            const val classNameArg = "className"
            const val studentIdArg = "studentId"
        }
    }

    data class ExamCorrectionClassSelectionScreen(val examId: String) : Route {
        override val route: String = "exam_correction_class_selection/$examId"

        companion object {
            const val route = "exam_correction_class_selection/{examId}"
            const val examIdArg = "examId"
        }
    }

    data class ExamCorrectionStudentSelectionScreen(val examId: String, val className: String) : Route {
        override val route: String = "exam_correction_student_selection/$examId/$className"

        companion object {
            const val route = "exam_correction_student_selection/{examId}/{className}"
            const val examIdArg = "examId"
            const val classNameArg = "className"
        }
    }

    data class ExamAnswerPaperUploadScreen(val examId: String) : Route {
        override val route: String = "exam_answer_paper_upload/$examId"

        companion object {
            const val route = "exam_answer_paper_upload/{examId}"
            const val examIdArg = "examId"
        }
    }

    data class ExamCorrectionPaperUploadScreen(val examId: String, val className: String, val studentId: String) : Route {
        override val route: String = "exam_correction_paper_upload/$examId/$className/$studentId"

        companion object {
            const val route = "exam_correction_paper_upload/{examId}/{className}/{studentId}"
            const val examIdArg = "examId"
            const val classNameArg = "className"
            const val studentIdArg = "studentId"
        }
    }
}
