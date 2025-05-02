package com.example.qcmaster.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.VisualTransformation
import com.example.qcmaster.SessionManager
import com.example.qcmaster.models.professors

@Composable
fun AuthScreen(navController: NavController) {
    var cin by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val cinHasError = cin.isNotEmpty() && cin.length != 8
    val passwordHasError = password.isNotEmpty() && password.length < 6
    val isFormValid = cin.length == 8 && password.length >= 6
    var loginError by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    val prof = professors.find { it.cin == cin && it.password == password }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Professor Login", fontSize = 22.sp)
        Spacer(modifier = Modifier.height(16.dp))

        // CIN TextField
        OutlinedTextField(
            value = cin,
            onValueChange = {
                if (it.length <= 8 && it.all { char -> char.isDigit() }) {
                    cin = it
                }
            },
            label = { Text("CIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = cinHasError
        )
        if (cinHasError) {
            Text(
                text = "CIN must be exactly 8 digits",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Password TextField
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = passwordHasError,
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = icon, contentDescription = description)
                }
            }
        )

        if (loginError) {
            Text(
                text = "Invalid CIN or password.",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }

        if (passwordHasError) {
            Text(
                text = "Password must be at least 6 characters",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Continue Button for login
        Button(
            onClick = {
                loginError = false
                if (cin.length == 8 && password.length >= 6) {
                    val professor = professors.find { it.cin == cin && it.password == password }

                    if (professor != null) {
                        // Save the session using SessionManager
                        SessionManager.saveSession(professor.email, professor.name)

                        // Navigate to home screen
                        navController.navigate("home/${professor.name}/${professor.email}") {
                            popUpTo("auth") { inclusive = true }
                        }
                    } else {
                        loginError = true
                    }
                }
            }
        ) {
            Text("Continue")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // "If you don't have an account" phrase and Register button
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("If you don't have an account, ")
            TextButton(
                contentPadding = PaddingValues(0.dp),
                onClick = { navController.navigate("register") }
            ) {
                Text("Register", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
