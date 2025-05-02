
package com.example.qcmaster.models



data class Professor(
    val cin: String,
    val password: String,
    val name: String,
    val email: String
)


// ✅ TEMPORARY IN-MEMORY STORAGE
val professors = mutableListOf<Professor>()
