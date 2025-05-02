
package com.example.qcmaster.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.qcmaster.data.ProfessorRepository
import com.example.qcmaster.models.Professor
import com.example.qcmaster.models.professors
import com.example.qcmaster.SessionManager


fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}
@Composable
fun RegisterScreen(navController: NavController) {
    var cin by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") } // ✨ New email field

    val cinHasError = cin.isNotEmpty() && cin.length != 8
    val passwordHasError = password.isNotEmpty() && password.length < 6
    val confirmPasswordHasError = confirmPassword.isNotEmpty() && confirmPassword != password
   // val isFormValid = cin.length == 8 && password.length >= 6 && name.isNotBlank() && email.isNotBlank()

    var cinExistsError by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val emailHasError = email.isNotEmpty() && !isValidEmail(email)
    val isFormValid = cin.length == 8 && password.length >= 6 && isValidEmail(email)


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Register Professor", fontSize = 22.sp)
        Spacer(modifier = Modifier.height(16.dp))

        // --- CIN Field ---
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
        if (cinExistsError) {
            Text(
                text = "This CIN is already registered.",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Password Field ---
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
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = icon, contentDescription = null)
                }
            }
        )
        if (passwordHasError) {
            Text(
                text = "Password must be at least 6 characters",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Confirm Password Field ---
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = confirmPasswordHasError,
            trailingIcon = {
                val icon = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(imageVector = icon, contentDescription = null)
                }
            }
        )
        if (confirmPasswordHasError) {
            Text(
                text = "Passwords do not match",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Register Button ---
        Button(
            onClick = {
                cinExistsError = false
                if (isFormValid && confirmPassword == password) {
                    val registrationSuccess = ProfessorRepository.register(
                        Professor(cin = cin, password = password, name = name, email = email) // Save email too!
                    )

                    if (registrationSuccess) {
                        professors.add(Professor(cin, password, name, email)) // Don't forget to modify your Professor model
                        SessionManager.profName = name
                        SessionManager.profEmail = email
                        navController.navigate("home/$name/$email")


                    } else {
                        cinExistsError = true
                    }
                }
            }
        ) {
            Text("Register")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Name Field ---
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- Email Field ---
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            isError = emailHasError
        )

        if (emailHasError) {
            Text(
                text = "Invalid email format",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Login Redirect ---
        TextButton(onClick = { navController.popBackStack() }) {
            Text("Already have an account? Login", color = MaterialTheme.colorScheme.primary)
        }
    }
}

