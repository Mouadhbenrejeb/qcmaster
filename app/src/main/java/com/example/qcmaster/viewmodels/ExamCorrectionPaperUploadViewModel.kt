package com.example.qcmaster.viewmodels

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qcmaster.ai.extractAnswersOpenCv
import com.example.qcmaster.data.FirebaseExamRepository
import com.example.qcmaster.data.FirebaseStudentRepository
import com.example.qcmaster.models.Exam
import com.example.qcmaster.models.Student
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ExamCorrectionPaperUploadUiState(
    val exam: Exam? = null,
    val student: Student? = null,
    val className: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val examPaperUri: Uri? = null,
    val examPaperBitmap: Bitmap? = null,
    val isUploading: Boolean = false,
    val uploadSuccess: Boolean = false,
    val uploadError: String? = null
)

class ExamCorrectionPaperUploadViewModel(
    private val examId: String,
    private val className: String,
    private val studentId: String
) : ViewModel() {
    private val examRepository = FirebaseExamRepository.getInstance()
    private val studentRepository = FirebaseStudentRepository.getInstance()

    var state by mutableStateOf(ExamCorrectionPaperUploadUiState(className = className))
        private set

    init {
        loadExam()
        loadStudent()
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

    fun onExamPaperUriChanged(uri: Uri?) {
        state = state.copy(examPaperUri = uri)
    }

    fun onExamPaperBitmapChanged(bitmap: Bitmap?) {
        state = state.copy(examPaperBitmap = bitmap)
    }

    fun uploadPapers() {
        // Process the exam paper and extract student answers
        state = state.copy(isUploading = true, uploadError = null)

        viewModelScope.launch {
            try {
                // Check if exam paper is selected
                if (state.examPaperBitmap == null) {
                    state = state.copy(
                        isUploading = false,
                        uploadError = "Please select an exam paper"
                    )
                    return@launch
                }

                // Extract student answers from the exam paper
                val bitmap = state.examPaperBitmap ?: return@launch
                val result = extractAnswersOpenCv(bitmap)

                if (result.answers.isNotEmpty()) {
                    // Keep answers as indices
                    val studentAnswers = result.answers.map { row -> row.answer.toString() }

                    // Save student answers to the database
                    val saveSuccess = examRepository.saveStudentAnswers(
                        examId = examId,
                        studentCIN = studentId,
                        answers = studentAnswers
                    )

                    if (!saveSuccess) {
                        state = state.copy(
                            isUploading = false,
                            uploadError = "Failed to save student answers"
                        )
                        return@launch
                    }

                    // Update student correction status
                    if (state.exam != null && state.student != null) {
                        examRepository.updateStudentCorrectionStatus(
                            examId = examId,
                            studentId = studentId,
                            isCorrected = true
                        )
                    }

                    // Upload successful
                    state = state.copy(
                        isUploading = false,
                        uploadSuccess = true
                    )
                } else {
                    state = state.copy(
                        isUploading = false,
                        uploadError = "Failed to extract answers from the exam paper"
                    )
                }
            } catch (e: Exception) {
                state = state.copy(
                    isUploading = false,
                    uploadError = "Error processing exam paper: ${e.message}"
                )
            }
        }
    }

    fun resetUpload() {
        state = state.copy(
            examPaperUri = null,
            examPaperBitmap = null,
            isUploading = false,
            uploadSuccess = false,
            uploadError = null
        )
    }
}
