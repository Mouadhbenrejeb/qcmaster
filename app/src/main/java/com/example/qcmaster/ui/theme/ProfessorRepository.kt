
package com.example.qcmaster.data

import com.example.qcmaster.model.Professor

object ProfessorRepository {
    private val professors = mutableListOf<Professor>()

    fun register(professor: Professor): Boolean {
        return if (professors.any { it.cin == professor.cin }) {
            false // CIN already exists
        } else {
            professors.add(professor)
            true
        }
    }

    fun login(cin: String, password: String): Boolean {
        return professors.any { it.cin == cin && it.password == password }
    }
}
