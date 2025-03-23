package com.example.polyquiz.match.domain

data class Game(
    val id: String? ="",
    val originalId: String,
    val title: String,
    val description: String,
    val questions: List<Question>? = null,
    val lastModification: String?,
    val isVisible: Boolean?,
    var nMatchesPlayed: Number,
    val duration: Number,
)
