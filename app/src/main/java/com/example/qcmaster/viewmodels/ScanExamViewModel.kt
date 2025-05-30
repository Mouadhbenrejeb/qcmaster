package com.example.qcmaster.viewmodels

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qcmaster.ai.AnswerRow
import com.example.qcmaster.ai.Shape
import com.example.qcmaster.ai.extractAnswersOpenCv
import com.example.qcmaster.models.Exam
import com.example.qcmaster.data.FirebaseExamRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// UI State for Scan Exam Screen
data class ScanExamUiState(
    val exam: Exam? = null,
    val isLoading: Boolean = true,
    val isProcessing: Boolean = false,
    val error: String? = null,
    val bitmap: ImageBitmap? = null,
    val answers: List<AnswerRow> = emptyList(),
    val correctAnswers: Map<String, String> = emptyMap(),
    val letterAnswers: List<String> = emptyList()
)

class ScanExamViewModel(private val examId: String) : ViewModel() {
    private val examRepository = FirebaseExamRepository.getInstance()

    // Single state object
    private var _state by mutableStateOf(ScanExamUiState(isLoading = true))
    val state: ScanExamUiState get() = _state

    init {
        // Load exam data
        loadExam()
    }

    private fun loadExam() {
        viewModelScope.launch {
            try {
                updateState(isLoading = true)

                // Observe exams from repository
                examRepository.exams.collectLatest { examsList ->
                    val exam = examsList.find { it.id == examId }
                    if (exam != null) {
                        updateState(
                            exam = exam,
                            isLoading = false
                        )
                    } else {
                        updateState(
                            isLoading = false,
                            error = "Exam not found"
                        )
                    }
                }
            } catch (e: Exception) {
                updateState(
                    isLoading = false,
                    error = "Error loading exam: ${e.message}"
                )
            }
        }
    }

    // Process the exam image to extract answers
    val processExamImage: (Bitmap) -> Unit = { bitmap ->
        viewModelScope.launch {
            try {
                updateState(isProcessing = true, error = null)

                val result = extractAnswersOpenCv(answerKeyBitmap = bitmap)

                updateState(
                    answers = result.answers,
                    bitmap = result.bitmap?.asImageBitmap(),
                    isProcessing = false
                )

                // Save correct answers to Firebase if they were successfully extracted
                if (result.answers.isNotEmpty()) {
                    // Show loading indicator for Firebase operation
                    updateState(isProcessing = true)

                    // Keep answers as indices
                    val indexAnswers = result.answers.map { row -> row.answer.toString() }
                    val saveSuccess = examRepository.saveCorrectAnswers(examId, indexAnswers)

                    // Update state based on save result
                    updateState(
                        isProcessing = false,
                        letterAnswers = indexAnswers,
                        error = if (!saveSuccess) "Failed to save answers to server" else null
                    )
                }
            } catch (e: Exception) {
                updateState(
                    isProcessing = false,
                    error = "Error processing image: ${e.message}"
                )
            }
        }
    }

    // Clear the current scan results
    val clearScanResults: () -> Unit = {
        updateState(
            answers = emptyList(),
            bitmap = null,
            error = null
        )
    }

    // Helper function to update state
    private fun updateState(
        exam: Exam? = _state.exam,
        isLoading: Boolean = _state.isLoading,
        isProcessing: Boolean = _state.isProcessing,
        error: String? = _state.error,
        bitmap: ImageBitmap? = _state.bitmap,
        answers: List<AnswerRow> = _state.answers,
        letterAnswers: List<String> = _state.letterAnswers,
    ) {
        _state = _state.copy(
            exam = exam,
            isLoading = isLoading,
            isProcessing = isProcessing,
            error = error,
            bitmap = bitmap,
            answers = answers,
            letterAnswers = letterAnswers,
        )
    }
}
