package com.example.qcmaster.data

import com.example.qcmaster.models.Student

object FakeStudentRepository {
    private val students = mutableListOf<Student>()

    // ✅ classExam[className][examName][studentCin] = grade
    private val classExam = mutableMapOf<String, MutableMap<String, MutableMap<String, Int>>>()

    fun getAllStudents(): List<Student> = students

    fun getStudentsForClass(className: String): List<Student> {
        return students.filter { it.assignedClass == className }
    }

    fun addStudent(student: Student): Boolean {
        if (student.cin.length != 8 || !student.cin.all { it.isDigit() }) return false
        if (students.any { it.cin == student.cin }) return false
        students.add(student)
        return true
    }

    fun removeStudent(cin: String) {
        students.removeIf { it.cin == cin }
    }

    fun clearAllStudents() {
        students.clear()
    }

    // ✅ Get grades for a class and exam
    fun getGradesForClassExam(className: String, examName: String): Map<String, Int> {
        return classExam[className]?.get(examName) ?: emptyMap()
    }

    // ✅ Add or update a student’s grade
    fun addOrUpdateGrade(className: String, examName: String, studentCin: String, grade: Int) {
        val examMap = classExam.getOrPut(className) { mutableMapOf() }
        val studentGrades = examMap.getOrPut(examName) { mutableMapOf() }
        studentGrades[studentCin] = grade
    }

    fun getStudentsInClass(className: String): List<Student> {
        return students.filter { it.assignedClass == className }
    }
}

