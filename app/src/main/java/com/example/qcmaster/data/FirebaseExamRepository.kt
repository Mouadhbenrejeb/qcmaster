package com.example.qcmaster.data

import com.example.qcmaster.models.Exam
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A Firebase-based repository for exam operations using coroutines.
 */
class FirebaseExamRepository {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val examsCollection = db.collection("exams")
    private val classesCollection = db.collection("classes")

    private val _exams = MutableStateFlow<List<Exam>>(emptyList())
    val exams: StateFlow<List<Exam>> = _exams.asStateFlow()

    init {
        // Listen for exam changes
        examsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("Error listening for exam changes: ${error.message}")
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val examsList = snapshot.documents.mapNotNull { doc ->
                    try {
                        val id = doc.id
                        val name = doc.getString("name") ?: ""
                        val assignedClasses = doc.get("assignedClasses") as? List<String> ?: emptyList()
                        val correctAnswers = doc.get("correctAnswers") as? List<String> ?: emptyList()
                        val answerPaperUploaded = doc.getBoolean("answerPaperUploaded") ?: false
                        val correctedStudents = doc.get("correctedStudents") as? Map<String, Boolean> ?: emptyMap()
                        val studentMarks = doc.get("studentMarks") as? Map<String, Int> ?: emptyMap()

                        Exam(
                            id = id,
                            name = name,
                            assignedClasses = assignedClasses,
                            correctAnswers = correctAnswers,
                            answerPaperUploaded = answerPaperUploaded,
                            correctedStudents = correctedStudents,
                            studentMarks = studentMarks
                        )
                    } catch (e: Exception) {
                        println("Error parsing exam document: ${e.message}")
                        null
                    }
                }
                _exams.value = examsList
            }
        }
    }

    /**
     * Gets all available classes.
     * @return A StateFlow of class names
     */
    fun getAvailableClasses(): StateFlow<List<String>> {
        val classesFlow = MutableStateFlow<List<String>>(emptyList())

        classesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("Error listening for class changes: ${error.message}")
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val classesList = snapshot.documents.mapNotNull { doc ->
                    doc.getString("name")
                }
                classesFlow.value = classesList
            }
        }

        return classesFlow
    }

    /**
     * Adds a new exam.
     * @param examName The name of the exam to add
     * @param assignedClasses The list of classes to assign to the exam
     * @return The ID of the newly created exam, or null if there was an error
     */
    suspend fun addExam(examName: String, assignedClasses: List<String> = emptyList()): String? = withContext(Dispatchers.IO) {
        try {
            // Check if exam with this name already exists
            val existingExam = examsCollection
                .whereEqualTo("name", examName)
                .get()
                .await()

            if (!existingExam.isEmpty) {
                return@withContext null
            }

            // Add the exam
            val examData = hashMapOf(
                "name" to examName,
                "assignedClasses" to assignedClasses,
                "correctAnswers" to emptyList<String>(),
                "answerPaperUploaded" to false,
                "correctedStudents" to emptyMap<String, Boolean>()
            )

            val docRef = examsCollection.add(examData).await()
            docRef.id
        } catch (e: Exception) {
            println("Error adding exam: ${e.message}")
            null
        }
    }

    /**
     * Assigns classes to an exam.
     * @param examId The ID of the exam
     * @param classes The list of class names to assign
     * @return true if the classes were assigned successfully, false otherwise
     */
    suspend fun assignClassesToExam(examId: String, classes: List<String>): Boolean = withContext(Dispatchers.IO) {
        try {
            examsCollection.document(examId)
                .update("assignedClasses", classes)
                .await()
            true
        } catch (e: Exception) {
            println("Error assigning classes to exam: ${e.message}")
            false
        }
    }

    /**
     * Saves correct answers for an exam.
     * @param examId The ID of the exam
     * @param answers The list of correct answers
     * @return true if the answers were saved successfully, false otherwise
     */
    suspend fun saveCorrectAnswers(examId: String, answers: List<String>): Boolean = withContext(Dispatchers.IO) {
        try {
            examsCollection.document(examId)
                .update("correctAnswers", answers)
                .await()
            true
        } catch (e: Exception) {
            println("Error saving correct answers: ${e.message}")
            false
        }
    }

    /**
     * Saves a student's answers for an exam.
     * @param examId The ID of the exam
     * @param studentCIN The CIN of the student
     * @param answers The list of student's answers
     * @return true if the answers were saved successfully, false otherwise
     */
    suspend fun saveStudentAnswers(examId: String, studentCIN: String, answers: List<String>): Boolean = withContext(Dispatchers.IO) {
        try {
            // Create a subcollection for student answers
            examsCollection.document(examId)
                .collection("studentAnswers")
                .document(studentCIN)
                .set(hashMapOf("answers" to answers))
                .await()
            true
        } catch (e: Exception) {
            println("Error saving student answers: ${e.message}")
            false
        }
    }

    /**
     * Gets a student's answers for an exam.
     * @param examId The ID of the exam
     * @param studentCIN The CIN of the student
     * @return The list of student's answers, or an empty list if there was an error
     */
    suspend fun getStudentAnswers(examId: String, studentCIN: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val document = examsCollection.document(examId)
                .collection("studentAnswers")
                .document(studentCIN)
                .get()
                .await()

            if (document.exists()) {
                document.get("answers") as? List<String> ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("Error getting student answers: ${e.message}")
            emptyList()
        }
    }

    /**
     * Removes an exam.
     * @param examId The ID of the exam to remove
     * @return true if the exam was removed successfully, false otherwise
     */
    suspend fun removeExam(examId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // First delete all student answers
            val studentAnswersSnapshot = examsCollection.document(examId)
                .collection("studentAnswers")
                .get()
                .await()

            for (doc in studentAnswersSnapshot.documents) {
                doc.reference.delete().await()
            }

            // Then delete the exam
            examsCollection.document(examId).delete().await()
            true
        } catch (e: Exception) {
            println("Error removing exam: ${e.message}")
            false
        }
    }

    /**
     * Uploads answer paper for an exam.
     * @param examId The ID of the exam
     * @return true if the answer paper was uploaded successfully, false otherwise
     */
    suspend fun uploadAnswerPaper(examId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            examsCollection.document(examId)
                .update("answerPaperUploaded", true)
                .await()
            true
        } catch (e: Exception) {
            println("Error uploading answer paper: ${e.message}")
            false
        }
    }

    /**
     * Updates the correction status of a student's exam.
     * @param examId The ID of the exam
     * @param studentId The ID of the student
     * @param isCorrected Whether the student's exam has been corrected
     * @return true if the status was updated successfully, false otherwise
     */
    suspend fun updateStudentCorrectionStatus(examId: String, studentId: String, isCorrected: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            val exam = examsCollection.document(examId).get().await()
            val correctedStudents = exam.get("correctedStudents") as? Map<String, Boolean> ?: emptyMap()

            val updatedCorrectedStudents = correctedStudents.toMutableMap().apply {
                put(studentId, isCorrected)
            }

            examsCollection.document(examId)
                .update("correctedStudents", updatedCorrectedStudents)
                .await()
            true
        } catch (e: Exception) {
            println("Error updating student correction status: ${e.message}")
            false
        }
    }

    /**
     * Updates a student's mark for an exam.
     * @param examId The ID of the exam
     * @param studentId The ID of the student
     * @param mark The student's mark (score as percentage)
     * @return true if the mark was updated successfully, false otherwise
     */
    suspend fun updateStudentMark(examId: String, studentId: String, mark: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val exam = examsCollection.document(examId).get().await()
            val studentMarks = exam.get("studentMarks") as? Map<String, Int> ?: emptyMap()

            val updatedStudentMarks = studentMarks.toMutableMap().apply {
                put(studentId, mark)
            }

            examsCollection.document(examId)
                .update("studentMarks", updatedStudentMarks)
                .await()
            true
        } catch (e: Exception) {
            println("Error updating student mark: ${e.message}")
            false
        }
    }

    companion object {
        // Singleton instance
        private var INSTANCE: FirebaseExamRepository? = null

        fun getInstance(): FirebaseExamRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = FirebaseExamRepository()
                INSTANCE = instance
                instance
            }
        }
    }
}
