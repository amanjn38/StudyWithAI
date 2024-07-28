package com.geminiai.studywithai.models

data class Book(
    val subject: String,
    val classLevel: String,
    val publisher: String,
    val index: List<String>,
    val chapters: List<Chapter>
)