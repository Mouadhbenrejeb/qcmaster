package com.example.qcmaster.data

import com.example.qcmaster.ClassModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A Firebase-based repository for class operations using coroutines.
 */
class FirebaseClassRepository {
    private val db = Firebase.firestore
    private val classesCollection = db.collection("classes")
    
    private val _classes = MutableStateFlow<List<ClassModel>>(emptyList())
    val classes: StateFlow<List<ClassModel>> = _classes.asStateFlow()

    /**
     * Fetches all classes for the current professor.
     * @param professorId The ID of the professor
     */
    suspend fun fetchClasses(professorId: String) = withContext(Dispatchers.IO) {
        try {
            val snapshot = classesCollection
                .whereEqualTo("professorId", professorId)
                .get()
                .await()
            
            val classesList = snapshot.documents.mapNotNull { document ->
                val name = document.getString("name") ?: return@mapNotNull null
                val code = document.getString("code") ?: return@mapNotNull null
                val description = document.getString("description") ?: ""
                
                ClassModel(name, code, description)
            }
            
            _classes.value = classesList
        } catch (e: Exception) {
            // Handle error
            _classes.value = emptyList()
        }
    }

    /**
     * Adds a new class for the current professor.
     * @param professorId The ID of the professor
     * @param classModel The class to add
     * @return true if the class was added successfully, false otherwise
     */
    suspend fun addClass(professorId: String, classModel: ClassModel): Boolean = withContext(Dispatchers.IO) {
        try {
            val classData = hashMapOf(
                "name" to classModel.name,
                "code" to classModel.code,
                "description" to classModel.description,
                "professorId" to professorId
            )
            
            classesCollection.add(classData).await()
            
            // Refresh the classes list
            fetchClasses(professorId)
            
            true
        } catch (e: Exception) {
            // Handle error
            false
        }
    }

    /**
     * Deletes a class.
     * @param professorId The ID of the professor
     * @param classCode The code of the class to delete
     * @return true if the class was deleted successfully, false otherwise
     */
    suspend fun deleteClass(professorId: String, classCode: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val snapshot = classesCollection
                .whereEqualTo("professorId", professorId)
                .whereEqualTo("code", classCode)
                .get()
                .await()
            
            if (snapshot.documents.isEmpty()) {
                return@withContext false
            }
            
            // Delete the class document
            snapshot.documents.first().reference.delete().await()
            
            // Refresh the classes list
            fetchClasses(professorId)
            
            true
        } catch (e: Exception) {
            // Handle error
            false
        }
    }

    /**
     * Checks if a class with the given name already exists for the professor.
     * @param professorId The ID of the professor
     * @param className The name of the class to check
     * @return true if the class exists, false otherwise
     */
    suspend fun classExists(professorId: String, className: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val snapshot = classesCollection
                .whereEqualTo("professorId", professorId)
                .whereEqualTo("name", className)
                .get()
                .await()
            
            !snapshot.documents.isEmpty()
        } catch (e: Exception) {
            // Handle error
            false
        }
    }

    companion object {
        // Singleton instance
        private var INSTANCE: FirebaseClassRepository? = null

        fun getInstance(): FirebaseClassRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = FirebaseClassRepository()
                INSTANCE = instance
                instance
            }
        }
    }
}