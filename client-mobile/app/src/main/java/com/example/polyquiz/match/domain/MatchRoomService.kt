package com.example.polyquiz.match.domain

import StringValue
import android.annotation.SuppressLint
import android.content.Context
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.example.polyquiz.R
import com.example.polyquiz.SnackbarController
import com.example.polyquiz.SnackbarEvent
import com.example.polyquiz.chat.domain.ChatService
import com.example.polyquiz.constants.AnswerEvents
import com.example.polyquiz.constants.ChatEvents
import com.example.polyquiz.constants.LongAnswerInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.polyquiz.constants.Route
import com.example.polyquiz.constants.VotingData
import com.example.polyquiz.pages.presentation.VotingPage
import com.example.polyquiz.elo.domain.EloService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.polyquiz.match.domain.AnswerService.gradeAnswers
import com.example.polyquiz.match.domain.AnswerService.playersAnswers
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONException
import java.util.Arrays

@SuppressLint("StaticFieldLeak")
object MatchRoomService {
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
    var canPlayCheaterMode by mutableStateOf(false)
    var gameTitle: String = ""
    var gameDuration: Int = 0
    var currentAnswers: MutableList<String> = mutableListOf()
    var partyConfig by mutableStateOf(PartyConfig(false, false, 0, false, false))
    var currentQuestion by mutableStateOf<Question?>(null)
    var isHostPlaying by mutableStateOf(true)
    var isCooldown by mutableStateOf(false)
    var isQuitting by mutableStateOf(false)
    var username by mutableStateOf("")
    var userId by mutableStateOf("")
    var hostId by mutableStateOf("")
    var errorMsg by mutableStateOf("")
    var navigateToVotingPage by mutableStateOf(false)
    var votingUsers = MutableStateFlow<List<String>>(emptyList())
    var playerVoted  by mutableStateOf(false)

    var userVoted by mutableStateOf("")
    var votesResults = mutableMapOf<String, Int>()
    var startedVote = false

    var cheaterPlayer by mutableStateOf(
        Player(
            "", "", "", 0, 0,
            false, false, ""
        )
    )

    var isCheaterMode by mutableStateOf(false)
    var votesData by mutableStateOf(VotingData("", 0, mutableListOf("")))
    var totalVotes: MutableList<VotingData> = mutableListOf()
    private var hasEnteredRoom = false
    private val _matchRoomCode = MutableStateFlow("")
    val matchRoomCode: StateFlow<String> get() = _matchRoomCode

    val socket = SocketHandler.getSocket()

    val socketId: String
        get() = socket.id() ?: ""

    fun getRoomCode(): String = _matchRoomCode.value
    fun retrieveUsername(): String = username

    fun connect(context: Context) {
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
            handleError(context)
//            onPlayerChatStateToggle()
            onRouteToResultsPage()
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
        socket.off(ChatEvents.NEW_MESSAGE.value)
        socket.off(ChatEvents.SENT_ROOM_EMOJI.value)
        socket.off(MatchEvents.SEND_BACK_VOTES_RESULTS.value)
        socket.off(MatchEvents.CURRENT_ANSWERS.value)
        socket.off(MatchEvents.CHEATER_MODE_MATCH_STARTING.value)
        socket.off(MatchEvents.SHOW_VOTING_DIALOG.value)
        socket.off(MatchEvents.SEND_VOTING_USERS.value)
        socket.off(AnswerEvents.END_GAME.value)
        ChatService.deleteRoomMessages()
        socket.emit(MatchEvents.DISCONNECT.value)
        MatchContextService.resetContext()
        hostId = ""
        timeToGoToWaitPage = false
        hasBeenKickedOut = true
        navigateToVotingPage = false
        isCheaterMode = false
        resetMatchValues()
        Log.d("Disconnect from room WaitPage", "Called disconnectFromRoom, hostId=$hostId")
        isTimeToNavigateToResults = false
    }

    fun sendBackVotesResult(voteData: JSONObject) {
        socket.emit(MatchEvents.SEND_VOTES_RESULTS.value, voteData)
    }

    fun onUsersWhoVoted() {
        socket.on(MatchEvents.SEND_VOTING_USERS.value) { args ->
            if (args.isNotEmpty()) {
                val firstArg = args[0]
                if (firstArg is JSONArray) {
                    if (firstArg.length() > 0) {
                        val user = firstArg.getString(0)
                        userVoted = user
                    } else {
                        Log.d("onUsersWhoVoted","JSONArray object is empty.")
                    }
                }
            }
        }
    }


    fun startMatchCheaterMode() {
        isCheaterMode = true;
        isMatchStarted = true;
        socket.emit(MatchEvents.START_MATCH_CHEATER_MODE.value, matchRoomCode.value)

    }

    fun onSelectedCheater() {
        socket.on(MatchEvents.SEND_CHEATER.value) { args ->
            if (args.isNotEmpty()) {
                val jsonString = (args[0] as? JSONObject)?.toString() ?: ""
                val jsonObject = JSONObject(jsonString)
                val playerName = jsonObject.optString("player")
                val player = getPlayerByUsername(playerName);
                cheaterPlayer = player!!;
            }
        }
    }

    fun onMatchCheaterModeStarted() {
        socket.on(MatchEvents.CHEATER_MODE_MATCH_STARTING.value) { args ->
            isCheaterMode = true;
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

    fun onVoting() {
        socket.on(MatchEvents.SHOW_VOTING_DIALOG.value) {
            navigateToVotingPage = true
        }
    }


    fun voteOnCheater() {
        socket.emit(MatchEvents.VOTE_ON_CHEATER.value, matchRoomCode.value)
    }

    fun onCurrentAnswers() {
        socket.on(MatchEvents.CURRENT_ANSWERS.value) { args ->
            if (userId == hostId || userId == cheaterPlayer?.id) {
                if (args.isNotEmpty() && args[0] != null) {
                    currentAnswers = Gson().fromJson(
                        args[0].toString(),
                        object : TypeToken<List<String>>() {}.type
                    )
                }
            }
        }
    }

    fun onVotingResults() {
        socket.on(MatchEvents.SEND_BACK_VOTES_RESULTS.value) { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                val gson = Gson()
                val votingData = gson.fromJson(data.toString(), VotingData::class.java)
                votesData = votingData
                totalVotes.add(votingData)

                val votesMap = mutableMapOf<String, Int>()
                val iterator = data.keys()
                while (iterator.hasNext()) {
                    val key = iterator.next()
                    val value = data.opt(key.toString())
                    if (value is Int) {
                        votesMap[key.toString()] = value
                    } else {
                        votesMap[key.toString()] = 0
                    }
                }
                votesResults = votesMap
            }
        }
    }

    fun createRoom(
        gameId: String,
        hostId: String,
        hostUsername: String,
        isClassicMode: Boolean = true,
        partyConfigs: PartyConfig = PartyConfig(false, false, isCheaterMode = false)
    ) {
        val partyConfisObject = JSONObject().apply {
            put("isFriendsOnly", partyConfigs.isFriendsOnly)
            put("isEntryFeeRequired", partyConfigs.isEntryFeeRequired)
            put("entryFeeAmount", partyConfigs.entryFeeAmount)
            put("isCheaterMode", partyConfigs.isCheaterMode)
        }
        val data = JSONObject().apply {
            put("gameId", gameId)
            put("hostId", hostId)
            put("partyConfig", partyConfisObject)
            put("isClassicMode", isClassicMode)
        }
        println("data : $data")

        socket.emit(MatchEvents.CREATE_ROOM.value, data, Ack { args ->
            if (args.isNotEmpty()) {
                val response = args[0] as JSONObject
                _matchRoomCode.value = response.getString("code")
                username = hostUsername
                userId = hostId
                partyConfig = partyConfigs
                this.hostId = hostId
                sendPlayersData(_matchRoomCode.value)
            }
        })
    }

    fun getPlayerByUsername(username: String): Player? =
        players.find { it.username == username }

    fun sendPlayersData(roomCode: String) {
        socket.emit(MatchEvents.SEND_PLAYERS_DATA.value, roomCode)
    }

    fun joinRoom(roomCode: String, username: String, userId: String) {
        Log.d("Join room", "Hostid : $hostId")
        val sentInfo = JSONObject().apply {
            put("roomCode", roomCode)
            put("username", username)
            put("userId", userId)
        }

        socket.emit(MatchEvents.JOIN_ROOM.value, sentInfo, Ack { args ->
            if (args.isNotEmpty()) {
                ChatService.handleRoomMessage()
                val response = args[0] as JSONObject
                _matchRoomCode.value = response.getString("code")
                this.username = response.getString("username")
                this.userId = response.getString("userId")
                sendPlayersData(roomCode)
                timeToGoToWaitPage = true
            }
        })
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

    fun handleError(context: Context) {
        socket.on(MatchEvents.ERROR.value) { args ->
            if (args.isNotEmpty()) {
                val errors: List<String> = try {
                    Gson().fromJson(args[0].toString(), Array<String>::class.java).toList()
                } catch (e: Exception) {
                    listOf(args[0].toString())
                }

                val displayText = errors.joinToString(separator = "\n") { errorKey: String ->
                    val cleanKey = errorKey.trim()
                    StringValue.dynamicLookup(context, cleanKey).asString(context)
                }

                val event = SnackbarEvent(
                    message = StringValue.DynamicString(displayText)
                )

                CoroutineScope(Dispatchers.Main).launch {
                    SnackbarController.sendEvent(event)
                }
            }
        }
    }

    fun startMatch() {
        isMatchStarted = true
        socket.emit(MatchEvents.START_MATCH.value, matchRoomCode.value)
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
        socket.emit(MatchEvents.GO_TO_NEXT_QUESTION.value, matchRoomCode.value)
    }

    fun onStartCooldown() {
        socket.on(MatchEvents.START_COOLDOWN.value) { _ ->
            isCooldown = true
            val context = MatchContextService.getContext()
            if (isCooldown && context != MatchContext.TESTPAGE && context != MatchContext.RANDOMMODE) {
                currentQuestion?.text = StringValue.StringResource(R.string.prepare).toString()
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
        _matchRoomCode.value = ""
        username = ""
        players = emptyList()
        messages = emptyList()
        isResults = false
        isWaitOver = false
        isPlaying = false
        isCooldown = false
        votingUsers = MutableStateFlow(emptyList())
        userVoted = ""
        votesResults = mutableMapOf<String, Int>()
        startedVote = false
        if (cheaterPlayer.username != "") {
            cheaterPlayer = Player("", "", "", 0, 0, false, false, "")
        }
        playerVoted = false
    }

    fun routeToResultsPage() {
        socket.emit(MatchEvents.ROUTE_TO_RESULTS_PAGE.value, matchRoomCode.value)
    }

    fun onRouteToResultsPage() {
        socket.on(MatchEvents.ROUTE_TO_RESULTS_PAGE.value) { _ ->
            println("Navigating to results page")
            isResults = true
            isTimeToNavigateToResults = true
        }
    }


    fun onPlayerKick() {
        socket.on(MatchEvents.KICK_PLAYER.value) { _ ->
            isBanned = true
            disconnectFromRoom()
        }
    }

    fun toggleLock() {
        socket.emit(MatchEvents.TOGGLE_LOCK.value, matchRoomCode.value)
        isLocked = !isLocked
    }
}
