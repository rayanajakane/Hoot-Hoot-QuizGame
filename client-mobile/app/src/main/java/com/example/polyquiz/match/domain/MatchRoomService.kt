package com.example.polyquiz.match.domain
import android.annotation.SuppressLint
import com.example.polyquiz.constants.MatchContext
import com.example.polyquiz.constants.MatchEvents
import com.example.polyquiz.constants.MatchStatus
import com.example.polyquiz.chat.domain.Message
import com.example.vanillaprototype.socket.SocketHandler
import com.google.gson.Gson
import io.socket.client.Ack
import org.json.JSONObject
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.polyquiz.constants.Route

@SuppressLint("StaticFieldLeak")
object MatchRoomService {
    var navController : NavController? = null
    var players by mutableStateOf<List<Player>>(emptyList())
    var messages by mutableStateOf<List<Message>>(emptyList())
    var isMatchStarted by mutableStateOf(false)
    var isResults by mutableStateOf(false)
    var isWaitOver by mutableStateOf(false)
    var isBanned by mutableStateOf(false)
    var timeToGoToWaitPage by mutableStateOf(false)
    var isPlaying by mutableStateOf(false)
    var isTimeToNavigate by mutableStateOf(false)
    var isTimeToNavigateToResults by mutableStateOf(false)
    var hasBeenKickedOut by mutableStateOf(false)
    var isLocked by mutableStateOf(false)
    var gameTitle: String = ""
    var gameDuration: Int = 0
    var currentQuestion by mutableStateOf<Question?>(null)
    var isHostPlaying by mutableStateOf(true)
    var isCooldown by mutableStateOf(false)
    var isQuitting by mutableStateOf(false)
    var username by mutableStateOf("")
    var userId by mutableStateOf("")
    var hostId by mutableStateOf("")

    private var matchRoomCode: String = ""
    private var hasEnteredRoom = false

    private val socket = SocketHandler.getSocket()

    val mSocket = SocketHandler.getSocket()

    val socketId: String
        get() = socket.id() ?: ""

    fun getRoomCode(): String = matchRoomCode
    fun retrieveUsername(): String = username

    fun connect() {
        if (!hasEnteredRoom) {
            hasEnteredRoom = true
            resetMatchValues()
            onRedirectAfterDisconnection()
            onFetchPlayersData()
            onMatchStarted()
            onBeginQuiz()
            onNextQuestion()
            onStartCooldown()
            onHostQuit()
            onPlayerKick()
            handleError()
//            onPlayerChatStateToggle()
            onRouteToResultsPage()
            timeToGoToWaitPage = true
        }
    }

    fun disconnectFromRoom() {
        hasEnteredRoom = false
        socket.off(MatchEvents.FETCH_PLAYERS_DATA.value)
        socket.off(MatchEvents.MATCH_STARTING.value)
        socket.off(MatchEvents.BEGIN_QUIZ.value)
        socket.off(MatchEvents.GO_TO_NEXT_QUESTION.value)
        socket.off(MatchEvents.START_COOLDOWN.value)
        socket.off(MatchEvents.HOST_QUIT_MATCH.value)
        socket.off(MatchEvents.KICK_PLAYER.value)
        socket.off(MatchEvents.ERROR.value)
        socket.off(MatchEvents.ROUTE_TO_RESULTS_PAGE.value)
        socket.emit(MatchEvents.DISCONNECT.value)
        MatchContextService.resetContext()
        timeToGoToWaitPage = false
        hasBeenKickedOut = true
    }

    fun createRoom(gameId: String, hostId: String, hostUsername: String, isClassicMode: Boolean = true) {
        val data = JSONObject().apply {
            put("gameId", gameId)
            put("hostId", hostId)
//            put("hostUsername", hostUsername)
            put("isClassicMode", isClassicMode)
        }
        Log.d("NADA CREATE ROOM", "${hostId} et ${hostUsername}")

        socket.emit(MatchEvents.CREATE_ROOM.value, data, Ack { args ->
            if (args.isNotEmpty()) {
                val response = args[0] as JSONObject
                matchRoomCode = response.getString("code")
                username = hostUsername
                userId = hostId
                this.hostId = hostId
                sendPlayersData(matchRoomCode)
            }
        })
    }

    fun getPlayerByUsername(username: String): Player? =
        players.find { it.username == username }

    fun joinRoom(roomCode: String, username: String, userId:String) {
        val sentInfo = JSONObject().apply {
            put("roomCode", roomCode)
            put("username", username)
            put("userId", userId)
        }

        socket.emit(MatchEvents.JOIN_ROOM.value, sentInfo, Ack { args ->
            if (args.isNotEmpty()) {
                val response = args[0] as JSONObject
                matchRoomCode = response.getString("code")
                this.username = response.getString("username")
                this.userId = response.getString("userId")
                sendPlayersData(roomCode)
            }
        })
    }


    fun sendPlayersData(roomCode: String) {
        socket.emit(MatchEvents.SEND_PLAYERS_DATA.value, roomCode)
    }

    fun banUsername(userId: String) {
        if (this.userId == this.hostId) {
            val sentInfo = JSONObject().apply {
                put("roomCode", matchRoomCode)
                put("userId", userId)
            }
            socket.emit(MatchEvents.BAN_USERNAME.value, sentInfo)
        }
    }

    fun handleError() {
        socket.on(MatchEvents.ERROR.value) { args ->
            if (args.isNotEmpty()) {
                val errorMessage = args[0] as? String ?: "Unknown error"
            }
        }
    }

    fun startMatch() {
        isMatchStarted = true
        socket.emit(MatchEvents.START_MATCH.value, matchRoomCode)
    }

    fun onMatchStarted() {
        socket.on(MatchEvents.MATCH_STARTING.value) { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                if (data.optBoolean("start", false)) {
                    isMatchStarted = true
                }
                if (data.has("gameTitle")) {
                    gameTitle = data.getString("gameTitle")
                }
            }
        }
    }

    fun onBeginQuiz() {
        socket.on(MatchEvents.BEGIN_QUIZ.value) { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                isWaitOver = true
                val gson = Gson()
                val firstQuestion = gson.fromJson(
                    data.getJSONObject("firstQuestion").toString(),
                    Question::class.java
                )
                currentQuestion = firstQuestion
                gameDuration = data.getInt("gameDuration")
                isTimeToNavigate = true
            }
        }
    }

    fun goToNextQuestion() {
        socket.emit(MatchEvents.GO_TO_NEXT_QUESTION.value, matchRoomCode)
    }

    fun onStartCooldown() {
        socket.on(MatchEvents.START_COOLDOWN.value) { _ ->
            isCooldown = true
            val context = MatchContextService.getContext()
            if (isCooldown && context != MatchContext.TESTPAGE && context != MatchContext.RANDOMMODE) {
                currentQuestion?.text = MatchStatus.PREPARE.value
            }
        }
    }

    fun onNextQuestion() {
        socket.on(MatchEvents.GO_TO_NEXT_QUESTION.value) { args ->
            if (args.isNotEmpty()) {
                isCooldown = false

                val jsonString = (args[0] as? JSONObject)?.toString() ?: ""

                if (jsonString.isNotEmpty()) {
                    val gson = Gson()
                    currentQuestion = gson.fromJson(jsonString, Question::class.java)
                }
            }
        }
    }

    fun onFetchPlayersData() {
        socket.on(MatchEvents.FETCH_PLAYERS_DATA.value) { args ->
            if (args.isNotEmpty()) {
                val playersJson = args[0] as? String
                playersJson?.let {
                    players = Gson().fromJson(it, Array<Player>::class.java).toList()
                }
            }
        }
    }

    fun onHostQuit() {
        socket.on(MatchEvents.HOST_QUIT_MATCH.value) { _ ->
            isHostPlaying = false
            disconnectFromRoom()
        }
    }

    fun onRedirectAfterDisconnection() {
        socket.on(MatchEvents.DISCONNECT.value) { _ ->
            resetMatchValues()
        }
    }

    fun resetMatchValues() {
        matchRoomCode = ""
        username = ""
        players = emptyList()
        messages = emptyList()
        isResults = false
        isWaitOver = false
        isPlaying = false
        isCooldown = false
    }

    fun routeToResultsPage() {
        socket.emit(MatchEvents.ROUTE_TO_RESULTS_PAGE.value, matchRoomCode)
    }
//    private fun navigateToResultsPage() {
//        navController?.navigate(Route.ResultsPage)
//    }

    fun onRouteToResultsPage() {
        socket.on(MatchEvents.ROUTE_TO_RESULTS_PAGE.value) { _ ->
            isResults = true
            isTimeToNavigateToResults = true
            //navigateToResultsPage()
        }
    }


    fun onPlayerKick() {
        socket.on(MatchEvents.KICK_PLAYER.value) { _ ->
            isBanned = true
            disconnectFromRoom()
        }
    }

    fun toggleLock() {
        socket.emit(MatchEvents.TOGGLE_LOCK.value, matchRoomCode)
        isLocked = !isLocked
    }
}
