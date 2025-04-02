package com.example.polyquiz.match.domain

data class Player (
    val username: String,
    val id: String,
    val photoUrl: String,
    val score: Number,
    val bonusCount: Number,
    val isPlaying:Boolean,
    val isChatActive: Boolean,
    val state: String
)
