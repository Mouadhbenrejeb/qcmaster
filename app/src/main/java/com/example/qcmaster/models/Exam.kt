package com.example.qcmaster.models

/**
 * Data class representing an exam.
 *
 * @property id The unique identifier of the exam
 * @property name The name of the exam
 * @property assignedClasses The list of classes assigned to this exam
 * @property correctAnswers The list of correct answers for this exam
 * @property answerPaperUploaded Whether the answer paper has been uploaded
 * @property correctedStudents Map of student IDs to boolean indicating whether their exam has been corrected
 * @property studentMarks Map of student IDs to their marks (score as percentage)
 */
data class Exam(
    val id: String = "",
    val name: String = "",
    val assignedClasses: List<String> = emptyList(),
    val correctAnswers: List<String> = emptyList(),
    val answerPaperUploaded: Boolean = false,
    val correctedStudents: Map<String, Boolean> = emptyMap(),
    val studentMarks: Map<String, Int> = emptyMap()
)
