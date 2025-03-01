package com.example.polyquiz

import java.util.Date

data class Question(
    val id: String,
    val type: String,
    val text: String,
    val points: Double,
    val choices: List<Choice>? = null,
    val answer: String? = null,
    val lastModification: Date?,
)

data class Choice(
    val text: String,
    val isCorrect: Boolean? = null,
)
