package com.example.polyquiz.constants

data class VotingData (
    var username: String,
    var numberOfVotes: Int,
    val usersWhoVoted: MutableList<String>
)
