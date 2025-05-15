package com.example.qcmaster.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qcmaster.ClassModel
import com.example.qcmaster.SessionManager
import com.example.qcmaster.data.FirebaseClassRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ClassesUiState(
    val classes: List<ClassModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddClassDialog: Boolean = false,
    val newClassName: String = "",
    val newClassCode: String = "",
    val newClassDescription: String = "",
    val duplicateError: Boolean = false
)

class ClassesViewModel : ViewModel() {
    private val classRepository = FirebaseClassRepository.getInstance()

    var state by mutableStateOf(ClassesUiState(isLoading = true))
        private set

    init {
        loadClasses()

        // Observe changes to the classes collection
        viewModelScope.launch {
            classRepository.classes.collectLatest { classes ->
                state = state.copy(
                    classes = classes,
                    isLoading = false
                )
            }
        }
    }

    private fun loadClasses() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)

            try {
                // Use the professor's email as the professorId
                val professorId = SessionManager.getEmail()
                classRepository.fetchClasses(professorId)
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    error = "Failed to load classes: ${e.message}"
                )
            }
        }
    }

    // Show add class dialog
    val onShowAddClassDialog = {
        state = state.copy(
            showAddClassDialog = true,
            newClassName = "",
            newClassCode = "",
            newClassDescription = "",
            duplicateError = false
        )
    }

    // Dismiss add class dialog
    val onDismissAddClassDialog = {
        state = state.copy(showAddClassDialog = false)
    }

    // Update new class name
    val onNewClassNameChanged = { name: String ->
        state = state.copy(
            newClassName = name,
            duplicateError = false
        )
    }

    // Update new class code
    val onNewClassCodeChanged = { code: String ->
        state = state.copy(
            newClassCode = code,
            duplicateError = false
        )
    }

    // Update new class description
    val onNewClassDescriptionChanged = { description: String ->
        state = state.copy(newClassDescription = description)
    }

    // Add a new class
    fun addClass() {
        if (state.newClassName.isBlank() || state.newClassCode.isBlank()) {
            return
        }

        viewModelScope.launch {
            state = state.copy(isLoading = true)

            try {
                val professorId = SessionManager.getEmail()

                // Check if class with this name already exists
                val exists = classRepository.classExists(professorId, state.newClassName)

                if (exists) {
                    state = state.copy(
                        duplicateError = true,
                        isLoading = false
                    )
                    return@launch
                }

                // Create new class
                val newClass = ClassModel(
                    name = state.newClassName,
                    code = state.newClassCode,
                    description = state.newClassDescription
                )

                val success = classRepository.addClass(professorId, newClass)

                if (success) {
                    state = state.copy(
                        showAddClassDialog = false,
                        isLoading = false
                    )
                } else {
                    state = state.copy(
                        error = "Failed to add class",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                state = state.copy(
                    error = "Error adding class: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    // Delete a class
    fun deleteClass(classCode: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            try {
                val professorId = SessionManager.getEmail()
                val success = classRepository.deleteClass(professorId, classCode)

                if (!success) {
                    state = state.copy(
                        error = "Failed to delete class",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                state = state.copy(
                    error = "Error deleting class: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    // Retry loading classes
    val onRetry = {
        loadClasses()
    }

    // Clear error
    val onClearError = {
        state = state.copy(error = null)
    }
}
