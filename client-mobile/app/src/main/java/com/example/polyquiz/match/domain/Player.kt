package com.example.polyquiz.match.domain

data class Player (
    val id: String,
    val username: String,
    val score: Number,
    val bonusCount: Number,
    val isPlaying:Boolean,
    val isChatActive: Boolean,
    val state: String
)
