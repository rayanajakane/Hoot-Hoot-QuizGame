package com.example.polyquiz.match.domain

import com.example.polyquiz.http.CommunicationService

object JoinMatchService : CommunicationService("match") {
    data class UserData(val matchRoomCode: String, val username: String)
    data class MatchRoomData(val matchRoomCode: String)
    var matchRoomCode: String = ""

    override val apiService: ApiService = retrofit.create(JoinMatchApiService::class.java)

    fun validateMatchRoomCode(matchRoomCode: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        check(
            MatchRoomData(matchRoomCode),
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

    fun validateUsername(username: String, navigateToWaitPage: () -> Unit, navigateToHome: () -> Unit, navigateToMatchPage: () -> Unit) {
        postUsername(username,
            onSuccess = {
                val code = matchRoomCode
                matchRoomCode = ""
                addPlayerToMatchRoom(code, username,navigateToHome, navigateToWaitPage, navigateToMatchPage)
            },
            onError = { errorMessage ->
                println("Error: $errorMessage")
            }
        )
    }

    fun addPlayerToMatchRoom(matchRoomCode: String, username: String, navigateToHome: () -> Unit, navigateToWaitPage: () -> Unit, navigateToMatchPage: () -> Unit) {
        MatchRoomService.connect()
        MatchRoomService.joinRoom(matchRoomCode, username)
    }

    interface JoinMatchApiService : ApiService {}
}
