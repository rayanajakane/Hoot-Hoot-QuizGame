package com.example.polyquiz

import com.example.polyquiz.match.domain.Question

data class Game(
    val id: String? ="",
    //TODO: add mode
    val title: String,
    val description: String,
    val questions: List<Question>,
    val lastModification: String?,
    val isVisible: Boolean?,
    val duration: Number,
)
