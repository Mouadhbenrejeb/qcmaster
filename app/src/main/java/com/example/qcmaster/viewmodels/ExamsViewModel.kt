package com.example.qcmaster.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qcmaster.models.Exam
import com.example.qcmaster.data.FirebaseExamRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// UI State for Exams Screen
data class ExamsUiState(
    val exams: List<Exam> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showAddExamDialog: Boolean = false,
    val showEditExamDialog: Boolean = false,
    val examName: String = "",
    val examNameError: String? = null,
    val availableClasses: List<String> = emptyList(),
    val selectedClasses: List<String> = emptyList(),
    val classSelectionError: String? = null,
    val selectedExamId: String = ""
)

class ExamsViewModel : ViewModel() {
    private val examRepository = FirebaseExamRepository.getInstance()

    // Single state object
    private var _state by mutableStateOf(ExamsUiState(isLoading = true))
    val state: ExamsUiState get() = _state

    init {
        // Load exams data from Firebase
        loadExams()

        // Load available classes
        loadAvailableClasses()
    }

    private fun loadExams() {
        viewModelScope.launch {
            try {
                updateState(isLoading = true)

                // Observe exams from repository
                examRepository.exams.collectLatest { examsList ->
                    updateState(
                        exams = examsList,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                updateState(
                    isLoading = false,
                    error = "Error loading exams: ${e.message}"
                )
            }
        }
    }

    private fun loadAvailableClasses() {
        viewModelScope.launch {
            try {
                examRepository.getAvailableClasses().collectLatest { classes ->
                    updateState(
                        availableClasses = classes
                    )
                }
            } catch (e: Exception) {
                // Just log the error, don't update state as this is not critical
                println("Error loading classes: ${e.message}")
            }
        }
    }

    // Event handlers
    val onShowAddExamDialog: () -> Unit = {
        updateState(
            showAddExamDialog = true,
            examName = "",
            examNameError = null,
            selectedClasses = emptyList(),
            classSelectionError = null
        )
    }

    val onDismissAddExamDialog: () -> Unit = {
        updateState(
            showAddExamDialog = false
        )
    }

    val onExamNameChanged: (String) -> Unit = { newName ->
        updateState(
            examName = newName,
            examNameError = null
        )
    }

    val onClassToggled: (String, Boolean) -> Unit = { className, isChecked ->
        val currentSelected = _state.selectedClasses.toMutableList()
        if (isChecked) {
            currentSelected.add(className)
        } else {
            currentSelected.remove(className)
        }
        updateState(
            selectedClasses = currentSelected,
            classSelectionError = null
        )
    }

    val onAddExam: () -> Unit = {
        viewModelScope.launch {
            // Validate inputs
            var isValid = true

            if (_state.examName.isBlank()) {
                updateState(examNameError = "Exam name cannot be empty")
                isValid = false
            }

            if (_state.selectedClasses.isEmpty()) {
                updateState(classSelectionError = "Please select at least one class")
                isValid = false
            }

            if (isValid) {
                try {
                    // Show loading indicator
                    updateState(isLoading = true)

                    // Add to repository with selected classes
                    val examId = examRepository.addExam(_state.examName, _state.selectedClasses)

                    if (examId == null) {
                        updateState(
                            isLoading = false,
                            examNameError = "An exam with this name already exists"
                        )
                    } else {
                        // Close dialog
                        updateState(
                            isLoading = false,
                            showAddExamDialog = false,
                            examName = "",
                            selectedClasses = emptyList()
                        )
                    }
                } catch (e: Exception) {
                    // Show error
                    updateState(
                        isLoading = false,
                        error = "Error adding exam: ${e.message}"
                    )
                }
            }
        }
    }

    val onDeleteExam: (String) -> Unit = { examId ->
        viewModelScope.launch {
            try {
                // Show loading indicator
                updateState(isLoading = true)

                examRepository.removeExam(examId)

                // Loading indicator will be hidden when the exams flow updates
            } catch (e: Exception) {
                updateState(
                    isLoading = false,
                    error = "Error deleting exam: ${e.message}"
                )
            }
        }
    }

    val onShowEditExamDialog: (Exam) -> Unit = { exam ->
        val selectedClasses = exam.assignedClasses.toList()
        updateState(
            showEditExamDialog = true,
            examName = exam.name,
            examNameError = null,
            selectedClasses = selectedClasses,
            classSelectionError = null,
            selectedExamId = exam.id
        )
    }

    val onDismissEditExamDialog: () -> Unit = {
        updateState(
            showEditExamDialog = false
        )
    }

    val onUpdateExam: () -> Unit = {
        viewModelScope.launch {
            // Validate inputs
            var isValid = true

            if (_state.examName.isBlank()) {
                updateState(examNameError = "Exam name cannot be empty")
                isValid = false
            }

            if (_state.selectedClasses.isEmpty()) {
                updateState(classSelectionError = "Please select at least one class")
                isValid = false
            }

            if (isValid) {
                try {
                    // Show loading indicator
                    updateState(isLoading = true)

                    // Update exam with selected classes
                    val success = examRepository.assignClassesToExam(_state.selectedExamId, _state.selectedClasses)

                    if (success) {
                        // Close dialog
                        updateState(
                            isLoading = false,
                            showEditExamDialog = false,
                            examName = "",
                            selectedClasses = emptyList(),
                            selectedExamId = ""
                        )
                    } else {
                        updateState(
                            isLoading = false,
                            error = "Failed to update exam"
                        )
                    }
                } catch (e: Exception) {
                    // Show error
                    updateState(
                        isLoading = false,
                        error = "Error updating exam: ${e.message}"
                    )
                }
            }
        }
    }

    // Helper function to update state
    private fun updateState(
        exams: List<Exam> = _state.exams,
        isLoading: Boolean = _state.isLoading,
        error: String? = _state.error,
        showAddExamDialog: Boolean = _state.showAddExamDialog,
        showEditExamDialog: Boolean = _state.showEditExamDialog,
        examName: String = _state.examName,
        examNameError: String? = _state.examNameError,
        availableClasses: List<String> = _state.availableClasses,
        selectedClasses: List<String> = _state.selectedClasses,
        classSelectionError: String? = _state.classSelectionError,
        selectedExamId: String = _state.selectedExamId
    ) {
        _state = _state.copy(
            exams = exams,
            isLoading = isLoading,
            error = error,
            showAddExamDialog = showAddExamDialog,
            showEditExamDialog = showEditExamDialog,
            examName = examName,
            examNameError = examNameError,
            availableClasses = availableClasses,
            selectedClasses = selectedClasses,
            classSelectionError = classSelectionError,
            selectedExamId = selectedExamId
        )
    }
}
