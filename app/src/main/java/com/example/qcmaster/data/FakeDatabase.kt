package com.example.qcmaster.data


object FakeDatabase {
    val exams = mutableListOf<String>()  // List of exam names
    val classes = mutableListOf<String>()  // List of class names

    // Map to hold which classes are assigned to each exam
    val examClassMap = mutableMapOf<String, MutableList<String>>()
}