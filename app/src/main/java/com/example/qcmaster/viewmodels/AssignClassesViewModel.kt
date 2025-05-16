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

// UI State for Assign Classes Screen
data class AssignClassesUiState(
    val exam: Exam? = null,
    val allClasses: List<String> = emptyList(),
    val assignedClasses: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showAddClassDialog: Boolean = false,
    val newClassName: String = "",
    val newClassNameError: String? = null
)

class AssignClassesViewModel(private val examId: String) : ViewModel() {
    private val examRepository = FirebaseExamRepository.getInstance()

    // Single state object
    private var _state by mutableStateOf(AssignClassesUiState(isLoading = true))
    val state: AssignClassesUiState get() = _state

    init {
        // Load exam data and available classes
        loadExam()
        loadAvailableClasses()
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
                            assignedClasses = exam.assignedClasses,
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

    private fun loadAvailableClasses() {
        viewModelScope.launch {
            try {
                examRepository.getAvailableClasses().collectLatest { classes ->
                    updateState(
                        allClasses = classes
                    )
                }
            } catch (e: Exception) {
                // Just log the error, don't update state as this is not critical
                println("Error loading classes: ${e.message}")
            }
        }
    }

    // Event handlers
    val onClassToggled: (String, Boolean) -> Unit = { className, isChecked ->
        val currentAssigned = _state.assignedClasses.toMutableList()
        if (isChecked) {
            currentAssigned.add(className)
        } else {
            currentAssigned.remove(className)
        }
        updateState(
            assignedClasses = currentAssigned
        )
    }

    val onShowAddClassDialog: () -> Unit = {
        updateState(
            showAddClassDialog = true,
            newClassName = "",
            newClassNameError = null
        )
    }

    val onDismissAddClassDialog: () -> Unit = {
        updateState(
            showAddClassDialog = false
        )
    }

    val onNewClassNameChanged: (String) -> Unit = { newName ->
        updateState(
            newClassName = newName,
            newClassNameError = null
        )
    }

    val onAddClass: () -> Unit = {
        when {
            _state.newClassName.isBlank() -> {
                updateState(newClassNameError = "Class name cannot be empty")
            }
            _state.allClasses.contains(_state.newClassName) -> {
                updateState(newClassNameError = "Class already exists")
            }
            else -> {
                // TODO: Add class to Firebase
                // For now, just update the local state
                updateState(
                    allClasses = _state.allClasses + _state.newClassName,
                    showAddClassDialog = false,
                    newClassName = ""
                )
            }
        }
    }

    val onSaveAssignments: () -> Unit = {
        viewModelScope.launch {
            try {
                updateState(isLoading = true)

                val success = examRepository.assignClassesToExam(examId, _state.assignedClasses)

                if (success) {
                    // No need to update state here as the exam will be updated via the flow
                } else {
                    updateState(
                        isLoading = false,
                        error = "Failed to save assignments"
                    )
                }
            } catch (e: Exception) {
                updateState(
                    isLoading = false,
                    error = "Error saving assignments: ${e.message}"
                )
            }
        }
    }

    // Helper function to update state
    private fun updateState(
        exam: Exam? = _state.exam,
        allClasses: List<String> = _state.allClasses,
        assignedClasses: List<String> = _state.assignedClasses,
        isLoading: Boolean = _state.isLoading,
        error: String? = _state.error,
        showAddClassDialog: Boolean = _state.showAddClassDialog,
        newClassName: String = _state.newClassName,
        newClassNameError: String? = _state.newClassNameError
    ) {
        _state = _state.copy(
            exam = exam,
            allClasses = allClasses,
            assignedClasses = assignedClasses,
            isLoading = isLoading,
            error = error,
            showAddClassDialog = showAddClassDialog,
            newClassName = newClassName,
            newClassNameError = newClassNameError
        )
    }
}
