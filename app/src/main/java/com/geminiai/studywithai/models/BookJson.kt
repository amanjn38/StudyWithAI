package com.geminiai.studywithai.models

data class BookJson(
    val title: String,
    val description: String?,
    val covers: List<Int>?,
    val subjects: List<String>?,
    val key: String
)