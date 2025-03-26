package com.example.polyquiz.constants;

import com.example.polyquiz.match.domain.PartyConfig


data class MatchPageInfo (
    val code: String,
    val isLocked: Boolean,
    val isPlaying: Boolean,
    val gameTitle: String,
    val nPlayers: Number,
    val partyConfig: PartyConfig
)


