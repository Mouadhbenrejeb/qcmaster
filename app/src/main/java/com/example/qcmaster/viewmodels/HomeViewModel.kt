package com.example.qcmaster.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qcmaster.SessionManager
import com.example.qcmaster.data.FirebaseAuthRepository
import com.example.qcmaster.models.Professor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// UI State for Home Screen
data class HomeUiState(
    val professor: Professor? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val showProfileDialog: Boolean = false
)

class HomeViewModel : ViewModel() {
    private val authRepository = FirebaseAuthRepository.getInstance()

    // Single state object
    private var _state by mutableStateOf(HomeUiState(isLoading = true))
    val state: HomeUiState get() = _state

    init {
        // Load professor data from Firebase
        loadProfessorData()
        
        // Observe auth state changes
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { professor ->
                updateState(
                    professor = professor,
                    isLoading = false
                )
            }
        }
    }

    private fun loadProfessorData() {
        viewModelScope.launch {
            try {
                updateState(isLoading = true)
                
                // If we already have a professor in the repository, use it
                val currentProfessor = authRepository.currentUser.value
                if (currentProfessor != null) {
                    updateState(
                        professor = currentProfessor,
                        isLoading = false
                    )
                } else {
                    // Otherwise, check if we have session data
                    val email = SessionManager.getEmail()
                    val name = SessionManager.getName()
                    
                    if (email.isNotEmpty() && name.isNotEmpty()) {
                        // Create a professor from session data
                        val professor = Professor(
                            cin = "", // We don't have CIN in session
                            password = "", // We don't store passwords
                            name = name,
                            email = email
                        )
                        updateState(
                            professor = professor,
                            isLoading = false
                        )
                    } else {
                        // No data available
                        updateState(
                            isLoading = false,
                            error = "No professor data available"
                        )
                    }
                }
            } catch (e: Exception) {
                updateState(
                    isLoading = false,
                    error = "Error loading professor data: ${e.message}"
                )
            }
        }
    }

    // Event handlers
    val onShowProfileDialog: () -> Unit = {
        updateState(showProfileDialog = true)
    }

    val onDismissProfileDialog: () -> Unit = {
        updateState(showProfileDialog = false)
    }

    val onLogout: (onLogoutSuccess: () -> Unit) -> Unit = { onLogoutSuccess ->
        viewModelScope.launch {
            // Logout using the repository
            authRepository.logout()

            // Clear session
            SessionManager.clearSession()

            // Navigate to auth screen
            onLogoutSuccess()
        }
    }

    // Helper function to update state
    private fun updateState(
        professor: Professor? = _state.professor,
        isLoading: Boolean = _state.isLoading,
        error: String? = _state.error,
        showProfileDialog: Boolean = _state.showProfileDialog
    ) {
        _state = _state.copy(
            professor = professor,
            isLoading = isLoading,
            error = error,
            showProfileDialog = showProfileDialog
        )
    }
}