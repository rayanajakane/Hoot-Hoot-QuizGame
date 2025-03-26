package com.example.polyquiz.match.domain

data class PartyConfig(
    val isFriendsOnly: Boolean = false,
    val isEntryFeeRequired: Boolean = false,
    val entryFeeAmount: Number? = null
)
