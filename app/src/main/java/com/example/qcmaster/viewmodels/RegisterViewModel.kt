package com.example.qcmaster.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qcmaster.SessionManager
import com.example.qcmaster.data.FirebaseAuthRepository
import com.example.qcmaster.models.Professor
import kotlinx.coroutines.launch
import android.util.Patterns

// UI State for Register Screen
data class RegisterUiState(
    val cin: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val name: String = "",
    val email: String = "",
    val cinExistsError: Boolean = false,
    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val cinHasError: Boolean = false,
    val passwordHasError: Boolean = false,
    val confirmPasswordHasError: Boolean = false,
    val nameHasError: Boolean = false,
    val emailHasError: Boolean = false,
    val isFormValid: Boolean = false
)

class RegisterViewModel : ViewModel() {
    private val authRepository = FirebaseAuthRepository.getInstance()

    // Single state object
    private var _state by mutableStateOf(
        RegisterUiState(
            cinHasError = false,
            passwordHasError = false,
            confirmPasswordHasError = false,
            nameHasError = false,
            emailHasError = false,
            isFormValid = false
        )
    )
    val state: RegisterUiState get() = _state

    // Helper function for email validation
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Event handlers
    val onCinChanged: (String) -> Unit = { value ->
        if (value.length <= 8 && value.all { char -> char.isDigit() }) {
            updateState(
                cin = value,
                cinHasError = value.isNotEmpty() && value.length != 8,
                isFormValid = checkFormValidity(
                    cin = value,
                    password = _state.password,
                    confirmPassword = _state.confirmPassword,
                    name = _state.name,
                    email = _state.email
                )
            )
        }
    }

    val onPasswordChanged: (String) -> Unit = { value ->
        updateState(
            password = value,
            passwordHasError = value.isNotEmpty() && value.length < 6,
            confirmPasswordHasError = _state.confirmPassword.isNotEmpty() && _state.confirmPassword != value,
            isFormValid = checkFormValidity(
                cin = _state.cin,
                password = value,
                confirmPassword = _state.confirmPassword,
                name = _state.name,
                email = _state.email
            )
        )
    }

    val onConfirmPasswordChanged: (String) -> Unit = { value ->
        updateState(
            confirmPassword = value,
            confirmPasswordHasError = value.isNotEmpty() && value != _state.password,
            isFormValid = checkFormValidity(
                cin = _state.cin,
                password = _state.password,
                confirmPassword = value,
                name = _state.name,
                email = _state.email
            )
        )
    }

    val onNameChanged: (String) -> Unit = { value ->
        updateState(
            name = value,
            nameHasError = value.isNotEmpty() && value.length < 2,
            isFormValid = checkFormValidity(
                cin = _state.cin,
                password = _state.password,
                confirmPassword = _state.confirmPassword,
                name = value,
                email = _state.email
            )
        )
    }

    val onEmailChanged: (String) -> Unit = { value ->
        updateState(
            email = value,
            emailHasError = value.isNotEmpty() && !isValidEmail(value),
            isFormValid = checkFormValidity(
                cin = _state.cin,
                password = _state.password,
                confirmPassword = _state.confirmPassword,
                name = _state.name,
                email = value
            )
        )
    }

    val onTogglePasswordVisibility: () -> Unit = {
        updateState(passwordVisible = !_state.passwordVisible)
    }

    val onToggleConfirmPasswordVisibility: () -> Unit = {
        updateState(confirmPasswordVisible = !_state.confirmPasswordVisible)
    }

    // Register function
    val onRegister: (onRegisterSuccess: () -> Unit) -> Unit = { onRegisterSuccess ->
        updateState(cinExistsError = false)
        if (_state.isFormValid) {
            updateState(isLoading = true)
            val professor = Professor(
                cin = _state.cin, 
                password = _state.password, 
                name = _state.name, 
                email = _state.email
            )

            viewModelScope.launch {
                val registrationSuccess = authRepository.register(professor)

                if (registrationSuccess) {
                    // Login the user after successful registration
                    authRepository.login(_state.cin, _state.password)

                    // Save the session using SessionManager
                    SessionManager.saveSession(_state.email, _state.name)

                    // Navigate to home screen
                    onRegisterSuccess()
                } else {
                    updateState(cinExistsError = true)
                }
                updateState(isLoading = false)
            }
        }
    }

    // Helper function to check form validity
    private fun checkFormValidity(
        cin: String,
        password: String,
        confirmPassword: String,
        name: String,
        email: String
    ): Boolean {
        return cin.length == 8 && 
               password.length >= 6 && 
               password == confirmPassword && 
               name.length >= 2 && 
               isValidEmail(email)
    }

    // Helper function to update state
    private fun updateState(
        cin: String = _state.cin,
        password: String = _state.password,
        confirmPassword: String = _state.confirmPassword,
        name: String = _state.name,
        email: String = _state.email,
        cinExistsError: Boolean = _state.cinExistsError,
        passwordVisible: Boolean = _state.passwordVisible,
        confirmPasswordVisible: Boolean = _state.confirmPasswordVisible,
        isLoading: Boolean = _state.isLoading,
        cinHasError: Boolean = _state.cinHasError,
        passwordHasError: Boolean = _state.passwordHasError,
        confirmPasswordHasError: Boolean = _state.confirmPasswordHasError,
        nameHasError: Boolean = _state.nameHasError,
        emailHasError: Boolean = _state.emailHasError,
        isFormValid: Boolean = _state.isFormValid
    ) {
        _state = _state.copy(
            cin = cin,
            password = password,
            confirmPassword = confirmPassword,
            name = name,
            email = email,
            cinExistsError = cinExistsError,
            passwordVisible = passwordVisible,
            confirmPasswordVisible = confirmPasswordVisible,
            isLoading = isLoading,
            cinHasError = cinHasError,
            passwordHasError = passwordHasError,
            confirmPasswordHasError = confirmPasswordHasError,
            nameHasError = nameHasError,
            emailHasError = emailHasError,
            isFormValid = isFormValid
        )
    }
}
