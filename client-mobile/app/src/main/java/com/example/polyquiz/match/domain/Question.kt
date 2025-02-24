package com.example.polyquiz.match.domain

data class Question(
    val id: String,
    val type: String,
    var text: String,
    val points: Int,
    val choices: List<Choice>? = null,
    val answer: String? = null,
    val lastModification: String
)
