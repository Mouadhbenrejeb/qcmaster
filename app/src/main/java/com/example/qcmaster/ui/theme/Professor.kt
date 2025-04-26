
package com.example.qcmaster.model



data class Professor(
    val cin: String,
    val password: String,
    val name: String
)

// ✅ TEMPORARY IN-MEMORY STORAGE
val professors = mutableListOf<Professor>()
