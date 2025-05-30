package com.example.qcmaster.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.qcmaster.data.FirebaseExamRepository
import com.example.qcmaster.data.FirebaseStudentRepository
import com.example.qcmaster.models.Exam
import com.example.qcmaster.models.Student
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class CorrectionComparisonUiState(
    val exam: Exam? = null,
    val student: Student? = null,
    val className: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val correctAnswers: List<String> = emptyList(),
    val studentAnswers: List<String> = emptyList(),
    val score: Int = 0,
    val scoreOutOf20: Int = 0,
    val correctCount: Int = 0,
    val totalQuestions: Int = 0
)

class CorrectionComparisonViewModel(
    private val examId: String,
    private val className: String,
    private val studentId: String
) : ViewModel() {
    private val examRepository = FirebaseExamRepository.getInstance()
    private val studentRepository = FirebaseStudentRepository.getInstance()

    var state by mutableStateOf(CorrectionComparisonUiState(className = className))
        private set

    init {
        loadExam()
        loadStudent()
        loadCorrectAnswers()
        loadStudentAnswers()
    }

    private fun loadExam() {
        viewModelScope.launch {
            try {
                examRepository.exams.collectLatest { exams ->
                    val exam = exams.find { it.id == examId }
                    if (exam != null) {
                        state = state.copy(
                            exam = exam,
                            isLoading = false
                        )
                    } else {
                        state = state.copy(
                            error = "Exam not found",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                state = state.copy(
                    error = "Error loading exam: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun loadStudent() {
        viewModelScope.launch {
            try {
                studentRepository.students.collectLatest { students ->
                    val student = students.find { it.cin == studentId }
                    if (student != null) {
                        state = state.copy(
                            student = student,
                            isLoading = false
                        )
                    } else {
                        state = state.copy(
                            error = "Student not found",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                state = state.copy(
                    error = "Error loading student: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    // Load correct answers from the exam
    private fun loadCorrectAnswers() {
        viewModelScope.launch {
            try {
                // Use the correct answers from the exam model
                val exam = state.exam
                if (exam != null && exam.correctAnswers.isNotEmpty()) {
                    state = state.copy(
                        correctAnswers = exam.correctAnswers,
                        totalQuestions = exam.correctAnswers.size
                    )

                    calculateScore()
                }
            } catch (e: Exception) {
                state = state.copy(
                    error = "Error loading correct answers: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    // Load student answers from the repository
    private fun loadStudentAnswers() {
        viewModelScope.launch {
            try {
                // Get the student answers from the repository
                val studentAnswers = examRepository.getStudentAnswers(examId, studentId)

                if (studentAnswers.isNotEmpty()) {
                    state = state.copy(
                        studentAnswers = studentAnswers
                    )

                    calculateScore()
                } else {
                    state = state.copy(
                        error = "No answers found for this student",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                state = state.copy(
                    error = "Error loading student answers: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun calculateScore() {
        val correctAnswers = state.correctAnswers
        val studentAnswers = state.studentAnswers

        if (correctAnswers.isNotEmpty() && studentAnswers.isNotEmpty()) {
            val correctCount = correctAnswers.zip(studentAnswers)
                .count { (correct, student) -> correct == student }

            val score = if (correctAnswers.size > 0) {
                (correctCount * 100) / correctAnswers.size
            } else {
                0
            }

            // Calculate score out of 20
            val scoreOutOf20 = if (correctAnswers.size > 0) {
                (correctCount * 20) / correctAnswers.size
            } else {
                0
            }

            state = state.copy(
                correctCount = correctCount,
                score = score,
                scoreOutOf20 = scoreOutOf20
            )

            // Save the score to the database
            saveStudentMark(score)
        }
    }

    private fun saveStudentMark(score: Int) {
        viewModelScope.launch {
            try {
                examRepository.updateStudentMark(examId, studentId, score)
            } catch (e: Exception) {
                // Handle error if needed
                println("Error saving student mark: ${e.message}")
            }
        }
    }
}

class CorrectionComparisonViewModelFactory(
    private val examId: String,
    private val className: String,
    private val studentId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CorrectionComparisonViewModel::class.java)) {
            return CorrectionComparisonViewModel(examId, className, studentId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
