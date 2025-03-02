package com.example.polyquiz.match.domain

import com.example.polyquiz.http.CommunicationService

object JoinMatchService : CommunicationService("match") {
    data class UserData(val matchRoomCode: String, val username: String)
    var matchRoomCode: String = ""

    override val apiService: ApiService = retrofit.create(JoinMatchApiService::class.java)

    fun validateMatchRoomCode(matchRoomCode: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        check(
            matchRoomCode,
            onSuccess,
            onError,
            "validate-code",
        )
    }

    fun postUsername(username: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        check(
            UserData(matchRoomCode, username),
            onSuccess,
            onError,
            "validate-username",
        )
    }

    fun validateUsername(username: String) {
        postUsername(username,
            onSuccess = {
                val code = matchRoomCode
                matchRoomCode = ""
                addPlayerToMatchRoom(code, username)
            },
            onError = { errorMessage ->
                println("Error: $errorMessage")
            }
        )
    }

    fun addPlayerToMatchRoom(matchRoomCode: String, username: String) {
        MatchRoomService.connect()
        MatchRoomService.joinRoom(matchRoomCode, username)
    }

    interface JoinMatchApiService : ApiService {}
}
