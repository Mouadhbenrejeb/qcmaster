package com.example.qcmaster.viewmodels

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qcmaster.data.FirebaseExamRepository
import com.example.qcmaster.models.Exam
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ExamAnswerPaperUploadUiState(
    val exam: Exam? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val answerPaperUri: Uri? = null,
    val answerPaperBitmap: Bitmap? = null,
    val isUploading: Boolean = false,
    val uploadSuccess: Boolean = false,
    val uploadError: String? = null
)

class ExamAnswerPaperUploadViewModel(
    private val examId: String
) : ViewModel() {
    private val examRepository = FirebaseExamRepository.getInstance()
    
    var state by mutableStateOf(ExamAnswerPaperUploadUiState())
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
    
    fun onAnswerPaperUriChanged(uri: Uri?) {
        state = state.copy(answerPaperUri = uri)
    }
    
    fun onAnswerPaperBitmapChanged(bitmap: Bitmap?) {
        state = state.copy(answerPaperBitmap = bitmap)
    }
    
    fun uploadAnswerPaper() {
        state = state.copy(isUploading = true, uploadError = null)
        
        viewModelScope.launch {
            try {
                // Check if answer paper is selected
                if (state.answerPaperBitmap == null) {
                    state = state.copy(
                        isUploading = false,
                        uploadError = "Please select an answer paper"
                    )
                    return@launch
                }
                
                // Upload answer paper
                val success = examRepository.uploadAnswerPaper(examId)
                
                if (success) {
                    state = state.copy(
                        isUploading = false,
                        uploadSuccess = true
                    )
                } else {
                    state = state.copy(
                        isUploading = false,
                        uploadError = "Failed to upload answer paper"
                    )
                }
            } catch (e: Exception) {
                state = state.copy(
                    isUploading = false,
                    uploadError = "Error uploading answer paper: ${e.message}"
                )
            }
        }
    }
    
    fun resetUpload() {
        state = state.copy(
            answerPaperUri = null,
            answerPaperBitmap = null,
            isUploading = false,
            uploadSuccess = false,
            uploadError = null
        )
    }
}