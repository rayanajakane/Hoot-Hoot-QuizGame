package com.example.polyquiz.match.domain

data class Choice (
    var text: String,
    val isCorrect: Boolean? = null
)
