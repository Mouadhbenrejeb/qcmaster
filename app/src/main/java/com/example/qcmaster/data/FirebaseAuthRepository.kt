package com.example.qcmaster.data

import com.example.qcmaster.models.Professor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A Firebase-based repository for authentication operations using coroutines.
 */
class FirebaseAuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _currentUser = MutableStateFlow<Professor?>(null)
    val currentUser: StateFlow<Professor?> = _currentUser.asStateFlow()

    init {
        // Check if user is already signed in
        auth.currentUser?.let { firebaseUser ->
            firebaseUser.email?.let { email ->
                // We need to create a Professor object from the Firebase user
                // Since we don't have access to the CIN and password, we'll use defaults
                // The name will be extracted from the display name or email
                val name = firebaseUser.displayName ?: email.substringBefore('@')
                _currentUser.value = Professor(
                    cin = firebaseUser.uid.takeLast(8), // Use last 8 chars of UID as CIN
                    password = "", // We don't store passwords
                    name = name,
                    email = email
                )
            }
        }
    }

    /**
     * Registers a new professor using coroutines.
     * @param professor The professor to register
     * @return true if registration was successful, false if the email already exists
     */
    suspend fun register(professor: Professor): Boolean = withContext(Dispatchers.IO) {
        try {
            // Create user with email and password using coroutines
            val authResult = auth.createUserWithEmailAndPassword(professor.email, professor.password).await()

            // Update display name
            val user = authResult.user
            if (user != null) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(professor.name)
                    .build()

                user.updateProfile(profileUpdates).await()

                // Set current user
                _currentUser.value = professor
                true
            } else {
                false
            }
        } catch (e: Exception) {
            // Registration failed
            false
        }
    }

    /**
     * Logs in a professor using coroutines.
     * @param cin The CIN of the professor (used as email for Firebase)
     * @param password The password of the professor
     * @return true if login was successful, false otherwise
     */
    suspend fun login(cin: String, password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // For Firebase, we need an email, but we're using CIN for login
            // We'll create an email using the CIN as the username part
            val email = "$cin@example.com"

            val authResult = auth.signInWithEmailAndPassword(email, password).await()

            val user = authResult.user
            if (user != null && user.email != null) {
                val name = user.displayName ?: user.email!!.substringBefore('@')
                _currentUser.value = Professor(
                    cin = cin,
                    password = "", // We don't store passwords
                    name = name,
                    email = user.email!!
                )
                true
            } else {
                false
            }
        } catch (e: Exception) {
            // Login failed
            false
        }
    }

    /**
     * Logs out the current professor.
     */
    fun logout() {
        auth.signOut()
        _currentUser.value = null
    }

    /**
     * Checks if a professor is currently logged in.
     * @return true if a professor is logged in, false otherwise
     */
    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    companion object {
        // Singleton instance
        private var INSTANCE: FirebaseAuthRepository? = null

        fun getInstance(): FirebaseAuthRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = FirebaseAuthRepository()
                INSTANCE = instance
                instance
            }
        }
    }
}
