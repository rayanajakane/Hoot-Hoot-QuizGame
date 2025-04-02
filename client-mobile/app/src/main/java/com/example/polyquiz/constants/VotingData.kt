package com.example.polyquiz.constants

data class VotingData (
    var username: String = "",
    var numberOfVotes: Int = 0,
    val usersWhoVoted: MutableList<String>
)
