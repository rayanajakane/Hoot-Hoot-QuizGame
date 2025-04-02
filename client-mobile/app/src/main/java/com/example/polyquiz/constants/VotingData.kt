package com.example.polyquiz.constants

data class VotingData (
    val username: String = "",
    val numberOfVotes: Number = 0,
    val usersWhoVoted: List<String>
)
