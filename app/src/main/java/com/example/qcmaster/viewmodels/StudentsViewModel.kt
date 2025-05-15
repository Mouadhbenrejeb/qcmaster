package com.example.qcmaster.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qcmaster.models.Student
import com.example.qcmaster.data.FirebaseStudentRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// UI State for Students Screen
data class StudentsUiState(
    val students: List<Student> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showAddStudentDialog: Boolean = false,
    val name: String = "",
    val cin: String = "",
    val selectedClass: String = "",
    val availableClasses: List<String> = emptyList(),
    val nameError: String? = null,
    val cinError: String? = null,
    val classDropdownExpanded: Boolean = false
)

class StudentsViewModel : ViewModel() {
    private val studentRepository = FirebaseStudentRepository.getInstance()

    // Single state object
    private var _state by mutableStateOf(StudentsUiState(isLoading = true))
    val state: StudentsUiState get() = _state

    init {
        // Load students data from Firebase
        loadStudents()
        
        // Load available classes
        loadAvailableClasses()
    }

    private fun loadStudents() {
        viewModelScope.launch {
            try {
                updateState(isLoading = true)
                
                // Observe students from repository
                studentRepository.students.collectLatest { studentsList ->
                    updateState(
                        students = studentsList,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                updateState(
                    isLoading = false,
                    error = "Error loading students: ${e.message}"
                )
            }
        }
    }

    private fun loadAvailableClasses() {
        viewModelScope.launch {
            try {
                studentRepository.getAvailableClasses().collectLatest { classes ->
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
    val onShowAddStudentDialog: () -> Unit = {
        updateState(
            showAddStudentDialog = true,
            name = "",
            cin = "",
            selectedClass = "",
            nameError = null,
            cinError = null
        )
    }

    val onDismissAddStudentDialog: () -> Unit = {
        updateState(
            showAddStudentDialog = false
        )
    }

    val onNameChanged: (String) -> Unit = { newName ->
        updateState(
            name = newName,
            nameError = null
        )
    }

    val onCinChanged: (String) -> Unit = { newCin ->
        updateState(
            cin = newCin,
            cinError = null
        )
    }

    val onSelectedClassChanged: (String) -> Unit = { newClass ->
        updateState(
            selectedClass = newClass
        )
    }

    val onToggleClassDropdown: () -> Unit = {
        updateState(
            classDropdownExpanded = !_state.classDropdownExpanded
        )
    }

    val onCloseClassDropdown: () -> Unit = {
        updateState(
            classDropdownExpanded = false
        )
    }

    val onAddStudent: () -> Unit = {
        viewModelScope.launch {
            // Validate inputs
            var isValid = true
            
            if (_state.name.isBlank()) {
                updateState(nameError = "Name cannot be empty")
                isValid = false
            }
            
            if (!_state.cin.matches(Regex("\\d{8}"))) {
                updateState(cinError = "CIN must be exactly 8 digits")
                isValid = false
            } else {
                // Check if CIN already exists
                val cinExists = _state.students.any { it.cin == _state.cin }
                if (cinExists) {
                    updateState(cinError = "This CIN already exists")
                    isValid = false
                }
            }
            
            if (_state.selectedClass.isBlank()) {
                // We could add a class error, but for now just return
                isValid = false
            }
            
            if (isValid) {
                try {
                    // Create new student
                    val newStudent = Student(
                        name = _state.name,
                        cin = _state.cin,
                        assignedClass = _state.selectedClass
                    )
                    
                    // Add to repository
                    studentRepository.addStudent(newStudent)
                    
                    // Close dialog
                    updateState(
                        showAddStudentDialog = false,
                        name = "",
                        cin = "",
                        selectedClass = ""
                    )
                } catch (e: Exception) {
                    // Show error
                    updateState(
                        error = "Error adding student: ${e.message}"
                    )
                }
            }
        }
    }

    val onDeleteStudent: (String) -> Unit = { studentCin ->
        viewModelScope.launch {
            try {
                studentRepository.removeStudent(studentCin)
            } catch (e: Exception) {
                updateState(
                    error = "Error deleting student: ${e.message}"
                )
            }
        }
    }

    // Helper function to update state
    private fun updateState(
        students: List<Student> = _state.students,
        isLoading: Boolean = _state.isLoading,
        error: String? = _state.error,
        showAddStudentDialog: Boolean = _state.showAddStudentDialog,
        name: String = _state.name,
        cin: String = _state.cin,
        selectedClass: String = _state.selectedClass,
        availableClasses: List<String> = _state.availableClasses,
        nameError: String? = _state.nameError,
        cinError: String? = _state.cinError,
        classDropdownExpanded: Boolean = _state.classDropdownExpanded
    ) {
        _state = _state.copy(
            students = students,
            isLoading = isLoading,
            error = error,
            showAddStudentDialog = showAddStudentDialog,
            name = name,
            cin = cin,
            selectedClass = selectedClass,
            availableClasses = availableClasses,
            nameError = nameError,
            cinError = cinError,
            classDropdownExpanded = classDropdownExpanded
        )
    }
}