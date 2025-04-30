package com.example.qcmaster

import android.content.Context
import android.content.SharedPreferences

object SessionManager {

    private lateinit var prefs: SharedPreferences

    var profName: String = ""
    var profEmail: String = ""

    // Initialize session manager
    fun init(context: Context) {
        prefs = context.getSharedPreferences("QCMASTER_PREFS", Context.MODE_PRIVATE)
        profEmail = prefs.getString("email", "") ?: ""
        profName = prefs.getString("name", "") ?: ""
    }

    // Save session data (email and name)
    fun saveSession(email: String, name: String) {
        profEmail = email
        profName = name
        prefs.edit().apply {
            putString("email", email)
            putString("name", name)
            apply()
        }
    }

    // Clear session data
    fun clearSession() {
        profEmail = ""
        profName = ""
        prefs.edit().clear().apply()
    }

    // Getters for email and name
    fun getEmail(): String = profEmail
    fun getName(): String = profName
}
