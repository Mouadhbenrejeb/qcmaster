package com.example.qcmaster.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf


object FakeExamRepository {
    var exams = mutableStateListOf<String>()
    private val examClassMap = mutableStateMapOf<String, List<String>>()

    private val correctAnswersMap = mutableStateMapOf<String, List<String>>() // exam -> correct answers
    private val studentAnswersMap = mutableStateMapOf<String, MutableMap<String, List<String>>>() // exam -> (studentCIN -> answers)

    fun addExam(examName: String) {
        if (!exams.contains(examName)) {
            exams.add(examName)
        }
    }

    fun assignClassesToExam(exam: String, classes: List<String>) {
        examClassMap[exam] = classes
    }

    fun getAssignedClasses(exam: String): List<String> {
        return examClassMap[exam] ?: emptyList()
    }

    // Store correct answers for an exam
    fun saveCorrectAnswers(exam: String, answers: List<String>) {
        correctAnswersMap[exam] = answers
    }

    // Retrieve correct answers for an exam
    fun getCorrectAnswers(exam: String): List<String> {
        return correctAnswersMap[exam] ?: emptyList()
    }

    // Save student answers for a specific exam
    fun saveStudentAnswers(exam: String, studentCIN: String, answers: List<String>) {
        val studentsMap = studentAnswersMap.getOrPut(exam) { mutableMapOf() }
        studentsMap[studentCIN] = answers
    }

    // Get answers for a student for an exam
    fun getStudentAnswers(exam: String, studentCIN: String): List<String> {
        return studentAnswersMap[exam]?.get(studentCIN) ?: emptyList()
    }
}