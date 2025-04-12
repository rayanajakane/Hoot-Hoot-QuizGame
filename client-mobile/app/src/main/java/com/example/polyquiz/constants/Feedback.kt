package com.example.polyquiz.constants

data class Feedback(
    val score: Int,
    val answerCorrectness: AnswerCorrectness,
    val correctAnswer: List<String>? = emptyList()
)
