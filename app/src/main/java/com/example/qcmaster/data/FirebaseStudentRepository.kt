package com.example.qcmaster.data

import com.example.qcmaster.models.Student
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A Firebase-based repository for student operations using coroutines.
 */
class FirebaseStudentRepository {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val studentsCollection = db.collection("students")
    private val classesCollection = db.collection("classes")
    
    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    init {
        // Listen for student changes
        studentsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("Error listening for student changes: ${error.message}")
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val studentsList = snapshot.documents.mapNotNull { doc ->
                    try {
                        val name = doc.getString("name") ?: ""
                        val cin = doc.getString("cin") ?: ""
                        val assignedClass = doc.getString("assignedClass") ?: ""
                        
                        Student(
                            name = name,
                            cin = cin,
                            assignedClass = assignedClass
                        )
                    } catch (e: Exception) {
                        println("Error parsing student document: ${e.message}")
                        null
                    }
                }
                _students.value = studentsList
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
     * Gets students for a specific class.
     * @param className The name of the class
     * @return A list of students in the class
     */
    suspend fun getStudentsForClass(className: String): List<Student> = withContext(Dispatchers.IO) {
        try {
            val snapshot = studentsCollection
                .whereEqualTo("assignedClass", className)
                .get()
                .await()
                
            snapshot.documents.mapNotNull { doc ->
                try {
                    val name = doc.getString("name") ?: ""
                    val cin = doc.getString("cin") ?: ""
                    val assignedClass = doc.getString("assignedClass") ?: ""
                    
                    Student(
                        name = name,
                        cin = cin,
                        assignedClass = assignedClass
                    )
                } catch (e: Exception) {
                    println("Error parsing student document: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            println("Error getting students for class: ${e.message}")
            emptyList()
        }
    }

    /**
     * Adds a new student.
     * @param student The student to add
     * @return true if the student was added successfully, false otherwise
     */
    suspend fun addStudent(student: Student): Boolean = withContext(Dispatchers.IO) {
        try {
            // Check if student with this CIN already exists
            val existingStudent = studentsCollection
                .whereEqualTo("cin", student.cin)
                .get()
                .await()
                
            if (!existingStudent.isEmpty) {
                return@withContext false
            }
            
            // Add the student
            val studentData = hashMapOf(
                "name" to student.name,
                "cin" to student.cin,
                "assignedClass" to student.assignedClass
            )
            
            studentsCollection.document(student.cin).set(studentData).await()
            true
        } catch (e: Exception) {
            println("Error adding student: ${e.message}")
            false
        }
    }

    /**
     * Removes a student.
     * @param cin The CIN of the student to remove
     * @return true if the student was removed successfully, false otherwise
     */
    suspend fun removeStudent(cin: String): Boolean = withContext(Dispatchers.IO) {
        try {
            studentsCollection.document(cin).delete().await()
            true
        } catch (e: Exception) {
            println("Error removing student: ${e.message}")
            false
        }
    }

    companion object {
        // Singleton instance
        private var INSTANCE: FirebaseStudentRepository? = null

        fun getInstance(): FirebaseStudentRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = FirebaseStudentRepository()
                INSTANCE = instance
                instance
            }
        }
    }
}