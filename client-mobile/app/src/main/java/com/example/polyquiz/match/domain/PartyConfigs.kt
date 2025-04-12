package com.example.polyquiz.match.domain

data class PartyConfig(
    val isFriendsOnly: Boolean = false,
    val isEntryFeeRequired: Boolean = false,
    val entryFeeAmount: Number? = 0,
    val isCheaterMode: Boolean = false,
    val canPlayCheaterMode: Boolean = false,
)
