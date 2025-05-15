package com.example.qcmaster.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qcmaster.SessionManager
import com.example.qcmaster.data.FirebaseAuthRepository
import kotlinx.coroutines.launch

// UI State for Auth Screen
data class AuthUiState(
    val cin: String = "",
    val password: String = "",
    val loginError: Boolean = false,
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val cinHasError: Boolean = false,
    val passwordHasError: Boolean = false,
    val isFormValid: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val authRepository = FirebaseAuthRepository.getInstance()

    // Single state object
    private var _state by mutableStateOf(
        AuthUiState(
            cinHasError = false,
            passwordHasError = false,
            isFormValid = false
        )
    )
    val state: AuthUiState get() = _state

    // Event handlers
    val onCinChanged: (String) -> Unit = { value ->
        if (value.length <= 8 && value.all { char -> char.isDigit() }) {
            updateState(
                cin = value,
                cinHasError = value.isNotEmpty() && value.length != 8,
                isFormValid = value.length == 8 && _state.password.length >= 6
            )
        }
    }

    val onPasswordChanged: (String) -> Unit = { value ->
        updateState(
            password = value,
            passwordHasError = value.isNotEmpty() && value.length < 6,
            isFormValid = _state.cin.length == 8 && value.length >= 6
        )
    }

    val onTogglePasswordVisibility: () -> Unit = {
        updateState(passwordVisible = !_state.passwordVisible)
    }

    val onLogin: (onLoginSuccess: () -> Unit) -> Unit = { onLoginSuccess ->
        updateState(loginError = false)
        if (_state.isFormValid) {
            updateState(isLoading = true)

            viewModelScope.launch {
                val loginSuccess = authRepository.login(_state.cin, _state.password)
                if (loginSuccess) {
                    val professor = authRepository.currentUser.value
                    if (professor != null) {
                        // Save the session using SessionManager
                        SessionManager.saveSession(professor.email, professor.name)

                        // Navigate to home screen
                        onLoginSuccess()
                    }
                } else {
                    updateState(loginError = true)
                }
                updateState(isLoading = false)
            }
        }
    }

    // Helper function to update state
    private fun updateState(
        cin: String = _state.cin,
        password: String = _state.password,
        loginError: Boolean = _state.loginError,
        passwordVisible: Boolean = _state.passwordVisible,
        isLoading: Boolean = _state.isLoading,
        cinHasError: Boolean = _state.cinHasError,
        passwordHasError: Boolean = _state.passwordHasError,
        isFormValid: Boolean = _state.isFormValid
    ) {
        _state = _state.copy(
            cin = cin,
            password = password,
            loginError = loginError,
            passwordVisible = passwordVisible,
            isLoading = isLoading,
            cinHasError = cinHasError,
            passwordHasError = passwordHasError,
            isFormValid = isFormValid
        )
    }
}
