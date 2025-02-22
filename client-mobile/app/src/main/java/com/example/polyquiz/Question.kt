package com.example.polyquiz

data class Question(
    val id: String,
    val type: String,
    val text: String,
    val points: Int,
    val choices: List<Choice>? = null,
    val answer: String? = null,
    val lastModification: String
)

data class Choice(
    val id: String,
    val isCorrect: Boolean? = null,
)
