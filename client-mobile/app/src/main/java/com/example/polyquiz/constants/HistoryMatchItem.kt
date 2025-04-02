package com.example.polyquiz.constants

import java.util.Date

data class HistoryMatchItem(
    val id: String,
    val start : Date,
    val end : Date,
    val hasWon: Boolean,
    val hasGivenUp : Boolean,
    val nGoodAnswers: Int,
    val nTotalQuestions: Int,
)
