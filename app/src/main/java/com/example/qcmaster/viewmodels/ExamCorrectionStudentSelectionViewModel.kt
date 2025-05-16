package com.example.qcmaster.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qcmaster.data.FirebaseExamRepository
import com.example.qcmaster.data.FirebaseStudentRepository
import com.example.qcmaster.models.Exam
import com.example.qcmaster.models.Student
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ExamCorrectionStudentSelectionUiState(
    val exam: Exam? = null,
    val className: String = "",
    val students: List<Student> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ExamCorrectionStudentSelectionViewModel(
    private val examId: String,
    private val className: String
) : ViewModel() {
    private val examRepository = FirebaseExamRepository.getInstance()
    private val studentRepository = FirebaseStudentRepository.getInstance()
    
    var state by mutableStateOf(ExamCorrectionStudentSelectionUiState(className = className))
        private set
    
    init {
        loadExam()
        loadStudents()
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
    
    private fun loadStudents() {
        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true, error = null)
                
                val students = studentRepository.getStudentsForClass(className)
                state = state.copy(
                    students = students,
                    isLoading = false
                )
            } catch (e: Exception) {
                state = state.copy(
                    error = "Error loading students: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
}