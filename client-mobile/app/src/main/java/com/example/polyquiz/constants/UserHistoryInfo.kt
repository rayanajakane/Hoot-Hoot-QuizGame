package com.example.polyquiz.constants

data class UserHistoryInfo (
    val auth : List<HistoryAuthItem> ,
    val match : List<HistoryMatchItem>,
    val stats: MatchStats,
    val intensityGrid: List<Number>
)
