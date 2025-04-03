package com.example.polyquiz.elo.domain

enum class EloEvents (val value: String) {
    GET_ELO("getElo"),
    RETURN_ELO("returnElo"),
    UPDATE_ELO_FOR_MATCH("updateEloForMatch"),
    ERROR("eloError"),
    GET_RANKINGS("getRankings"),
    RETURN_RANKINGS("returnRankings"),
}
