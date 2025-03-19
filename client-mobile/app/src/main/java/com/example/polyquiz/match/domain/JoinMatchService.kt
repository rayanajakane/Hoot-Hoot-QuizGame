package com.example.polyquiz.match.domain

import com.example.polyquiz.Game
import com.example.polyquiz.constants.MatchEvents
import com.example.polyquiz.constants.MatchPageInfo
import com.example.polyquiz.http.CommunicationService
import com.example.polyquiz.match.domain.MatchRoomService.currentQuestion
import com.example.vanillaprototype.socket.SocketHandler
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject

object JoinMatchService : CommunicationService("match") {
    data class UserData(val matchRoomCode: String, val username: String)
    data class MatchRoomData(val matchRoomCode: String)

    var matchRoomCode: String = ""
    var matchInfos: MutableList<MatchPageInfo> = mutableListOf()

//    var matchInfos: MatchPageInfo = MatchPageInfo(
//        code = "",
//        isLocked = false,
//        isPlaying = false,
//        gameTitle = "",
//        nPlayers = 0
//    )

    private val mSocket = SocketHandler.getSocket()
    val matchRoomService = MatchRoomService

    override val apiService: ApiService = retrofit.create(JoinMatchApiService::class.java)

    fun getAllMatches(){
        onReturnAllMatches()
        matchRoomService.mSocket.send(MatchEvents.GET_ALL_MATCHES.value)
    }

    fun stopReturningAllMatches(){
        matchRoomService.mSocket.off(MatchEvents.RETURN_ALL_MATCHES.value)
    }

    fun onReturnAllMatches() {
        matchRoomService.mSocket.on(MatchEvents.RETURN_ALL_MATCHES.value) { data ->
            if (data != null) {
                val matchesArray = data[0] as JSONArray
                val gson = Gson()
                val matchListType = object : TypeToken<MutableList<MatchPageInfo>>() {}.type
                matchInfos = gson.fromJson(matchesArray.toString(), matchListType)
            }
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

    fun validateUsername(username: String, userId: String, navigateToWaitPage: () -> Unit, navigateToHome: () -> Unit, navigateToMatchPage: () -> Unit) {
        postUsername(username,
            onSuccess = {
                val code = matchRoomCode
                matchRoomCode = ""
                addPlayerToMatchRoom(code, username,userId, navigateToHome, navigateToWaitPage, navigateToMatchPage)
            },
            onError = { errorMessage ->
                println("Error: $errorMessage")
            }
        )
    }

    fun addPlayerToMatchRoom(matchRoomCode: String, username: String, userId:String, navigateToHome: () -> Unit, navigateToWaitPage: () -> Unit, navigateToMatchPage: () -> Unit) {
        MatchRoomService.connect()
        MatchRoomService.joinRoom(matchRoomCode, username, userId)
    }

    interface JoinMatchApiService : ApiService {}
}
