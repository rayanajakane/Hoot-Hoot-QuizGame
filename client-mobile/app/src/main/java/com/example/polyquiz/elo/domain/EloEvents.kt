package com.example.polyquiz.elo.domain

enum class EloEvents (val value: String) {
    GETELO("getElo"),
    RETURNELO("returnElo"),
    UPDATEELOFORMATCH("updateEloForMatch"),
    ERROR("error"),
    GETRANKINGS("getRankings"),
    RETURNRANKINGS("returnRankings"),
}
