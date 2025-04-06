package com.example.polyquiz.constants

import java.util.Date

data class UserHistoryInfo (
    val auth : List<HistoryAuthItem> ,
    val match : List<HistoryMatchItem>,
    val stats: MatchStats,
    val intensityGrid: List<IntensityGridItem>
)

data class IntensityGridItem (
    val date: Date,
    val intensity: Number,
    val nMatches: Number
)
