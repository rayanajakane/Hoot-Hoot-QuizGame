package com.example.polyquiz.elo.domain

enum class EloEvents (val value: String) {
    GETELO("getElo"),
    RETURNELO("returnElo"),
    UPDATEELOFORMATCH("updateEloForMatch"),
    ERROR("eloError"),
    GETRANKINGS("getRankings"),
    RETURNRANKINGS("returnRankings"),
}
