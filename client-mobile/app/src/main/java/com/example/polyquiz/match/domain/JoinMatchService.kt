package com.example.polyquiz.match.domain

import com.example.polyquiz.constants.MatchEvents
import com.example.polyquiz.constants.MatchPageInfo
import com.example.polyquiz.http.CommunicationService
import com.example.vanillaprototype.socket.SocketHandler

object JoinMatchService : CommunicationService("match") {
    data class UserData(val matchRoomCode: String, val username: String)
    data class MatchRoomData(val matchRoomCode: String)
    var matchRoomCode: String = ""

    private val mSocket = SocketHandler.getSocket()
    val matchRoomService = MatchRoomService

    override val apiService: ApiService = retrofit.create(JoinMatchApiService::class.java)

    fun getAllMatches(){
        onReturnAllMatches()
        matchRoomService.mSocket.send(MatchEvents.GET_ALL_MATCHES)
    }

    fun stopReturningAllMatches(){
        matchRoomService.mSocket.off(MatchEvents.RETURN_ALL_MATCHES.value)
    }

    fun onReturnAllMatches() {
        matchRoomService.mSocket.on(MatchEvents.RETURN_ALL_MATCHES.value) { data ->
            val matchList = data as List<MatchPageInfo>
            val match = matchList.first()
            var matchPageInfo = MatchPageInfo(
                code = match.code,
                isLocked = match.isLocked,
                isPlaying = match.isPlaying,
                gameTitle = match.gameTitle,
                nPlayers = match.nPlayers
            )
        }
    }

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
