package com.example.qcmaster.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qcmaster.R
import com.example.qcmaster.ui.theme.QcmasterTheme
import com.example.qcmaster.viewmodels.AuthUiState
import com.example.qcmaster.viewmodels.AuthViewModel

@Composable
fun AuthScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
) {
    val viewModel: AuthViewModel = viewModel()

    AuthScreenContent(
        state = viewModel.state,
        onCinChanged = viewModel.onCinChanged,
        onPasswordChanged = viewModel.onPasswordChanged,
        onTogglePasswordVisibility = viewModel.onTogglePasswordVisibility,
        onLogin = { viewModel.onLogin(onNavigateToHome) },
        onNavigateToRegister = onNavigateToRegister
    )
}

@Composable
fun AuthScreenContent(
    state: AuthUiState,
    onCinChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // App Logo or Icon
            Image(
                painter = painterResource(id = R.drawable.qcm_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "Welcome Back",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Sign in to continue",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            // CIN TextField
            OutlinedTextField(
                value = state.cin,
                onValueChange = onCinChanged,
                label = { Text("CIN") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "CIN Icon",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = state.cinHasError,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            if (state.cinHasError) {
                Text(
                    text = "CIN must be exactly 8 digits",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .padding(top = 4.dp, start = 4.dp)
                        .align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password TextField
            OutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChanged,
                label = { Text("Password") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "Password Icon",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                singleLine = true,
                visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = state.passwordHasError,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    val icon = if (state.passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (state.passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = onTogglePasswordVisibility) {
                        Icon(imageVector = icon, contentDescription = description)
                    }
                }
            )

            if (state.loginError) {
                Text(
                    text = "Invalid CIN or password.",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .padding(top = 4.dp, start = 4.dp)
                        .align(Alignment.Start)
                )
            }

            if (state.passwordHasError) {
                Text(
                    text = "Password must be at least 6 characters",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .padding(top = 4.dp, start = 4.dp)
                        .align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Login Button
            Button(
                onClick = onLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = state.isFormValid && !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Sign In", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Register Link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Don't have an account?")
                TextButton(
                    onClick = onNavigateToRegister
                ) {
                    Text(
                        "Sign Up",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(name = "Light Mode")
@Composable
private fun AuthScreenLightPreview() {
    QcmasterTheme(darkTheme = false) {
        // Create a dummy state for preview
        val dummyState = AuthUiState(
            cin = "12345678",
            password = "password",
            loginError = false,
            passwordVisible = false,
            isLoading = false,
            cinHasError = false,
            passwordHasError = false,
            isFormValid = true
        )

        AuthScreenContent(
            state = dummyState,
            onCinChanged = {},
            onPasswordChanged = {},
            onTogglePasswordVisibility = {},
            onLogin = {},
            onNavigateToRegister = {}
        )
    }
}

@Preview(name = "Dark Mode")
@Composable
private fun AuthScreenDarkPreview() {
    QcmasterTheme(darkTheme = true) {
        // Create a dummy state for preview
        val dummyState = AuthUiState(
            cin = "12345678",
            password = "password",
            loginError = false,
            passwordVisible = false,
            isLoading = false,
            cinHasError = false,
            passwordHasError = false,
            isFormValid = true
        )

        AuthScreenContent(
            state = dummyState,
            onCinChanged = {},
            onPasswordChanged = {},
            onTogglePasswordVisibility = {},
            onLogin = {},
            onNavigateToRegister = {}
        )
    }
}
