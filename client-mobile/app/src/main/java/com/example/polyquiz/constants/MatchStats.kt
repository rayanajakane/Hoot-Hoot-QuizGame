package com.example.polyquiz.constants

data class MatchStats (
    val nMatchesPlayed: Int,
    val nMatchesWon: Int,
    val averageGoodAnswersPercentage: Float,
    val averageTime: Float,
)
