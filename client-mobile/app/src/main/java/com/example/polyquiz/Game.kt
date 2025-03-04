package com.example.polyquiz

data class Game(
    val id: String? ="",
    // en cours ou en attente
    //TODO: add mode
    val title: String,
    val description: String,
    val questions: List<Question>? = null,
    val lastModification: String?,
    val isVisible: Boolean?,
    val duration: Number,
)
