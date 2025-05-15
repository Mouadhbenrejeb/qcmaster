package com.example.qcmaster.data

import com.example.qcmaster.models.Professor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A fake repository for authentication operations.
 * This simulates a real authentication system with in-memory storage.
 */
class FakeAuthRepository {
    private val _currentUser = MutableStateFlow<Professor?>(null)
    val currentUser: StateFlow<Professor?> = _currentUser.asStateFlow()

    private val professors = mutableListOf<Professor>()

    /**
     * Registers a new professor.
     * @param professor The professor to register
     * @return true if registration was successful, false if the CIN already exists
     */
    fun register(professor: Professor): Boolean {
        return if (professors.any { it.cin == professor.cin }) {
            false // CIN already exists
        } else {
            professors.add(professor)
            true
        }
    }

    /**
     * Logs in a professor.
     * @param cin The CIN of the professor
     * @param password The password of the professor
     * @return true if login was successful, false otherwise
     */
    fun login(cin: String, password: String): Boolean {
        val professor = professors.find { it.cin == cin && it.password == password }
        if (professor != null) {
            _currentUser.value = professor
            return true
        }
        return false
    }

    /**
     * Logs out the current professor.
     */
    fun logout() {
        _currentUser.value = null
    }

    /**
     * Checks if a professor is currently logged in.
     * @return true if a professor is logged in, false otherwise
     */
    fun isLoggedIn(): Boolean {
        return _currentUser.value != null
    }

    companion object {
        // Singleton instance
        private var INSTANCE: FakeAuthRepository? = null

        fun getInstance(): FakeAuthRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = FakeAuthRepository()
                INSTANCE = instance
                instance
            }
        }
    }
}