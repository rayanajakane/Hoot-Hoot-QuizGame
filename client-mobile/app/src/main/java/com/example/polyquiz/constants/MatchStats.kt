package com.example.polyquiz.constants

data class MatchStats(
    val nMatchesPlayed: Int,
    val nMatchesWon: Int,
    val averageGoodAnswersPercentage: Number,
    val averageTime: Number,
)
