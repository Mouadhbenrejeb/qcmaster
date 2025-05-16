package com.example.qcmaster.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qcmaster.data.FirebaseExamRepository
import com.example.qcmaster.models.Exam
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ExamCorrectionClassSelectionUiState(
    val exam: Exam? = null,
    val assignedClasses: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ExamCorrectionClassSelectionViewModel(private val examId: String) : ViewModel() {
    private val examRepository = FirebaseExamRepository.getInstance()

    var state by mutableStateOf(ExamCorrectionClassSelectionUiState())
        private set

    init {
        loadExam()
    }

    private fun loadExam() {
        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true, error = null)

                examRepository.exams.collectLatest { exams ->
                    val exam = exams.find { it.id == examId }
                    if (exam != null) {
                        state = state.copy(
                            exam = exam,
                            assignedClasses = exam.assignedClasses,
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
}
