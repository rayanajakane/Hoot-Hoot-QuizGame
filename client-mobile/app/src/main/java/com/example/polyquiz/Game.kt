package com.example.polyquiz

import com.example.polyquiz.match.domain.Question

data class Game(
    val id: String? ="",
    val originalId: String,
    val title: String,
    val description: String,
    val questions: List<Question>? = null,
    val lastModification: String?,
    val isVisible: Boolean?,
    val nMatchesPlayed: Number,
    val duration: Number,
)
