package com.example.polyquiz.match.domain

import android.util.Log
import com.example.polyquiz.constants.AnswerCorrectness
import com.example.polyquiz.constants.AnswerEvents
import com.example.polyquiz.constants.ChoiceInfo
import com.example.polyquiz.constants.Feedback
import com.example.polyquiz.constants.GradesInfo
import com.example.polyquiz.constants.LongAnswerInfo
import com.example.polyquiz.constants.MatchEvents
import com.example.polyquiz.constants.UserInfo
import com.example.vanillaprototype.socket.SocketHandler
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.example.polyquiz.auth.domain.AuthViewModel
import org.json.JSONObject

object AnswerService {
    var playersAnswers by mutableStateOf<List<LongAnswerInfo>>(emptyList())
    var feedback by mutableStateOf(Feedback(0, AnswerCorrectness.WRONG, emptyList()))
    var gradeAnswers by mutableStateOf(false)
    var isGradingComplete by mutableStateOf(false)
    var showFeedback by mutableStateOf(false)
    var isNextQuestionButtonEnabled by mutableStateOf(false)
    var isSelectionEnabled by mutableStateOf(false)
    var correctAnswer by mutableStateOf<List<String>>(emptyList())
    var answerCorrectness by mutableStateOf(AnswerCorrectness.WRONG)
    var playerScore by mutableStateOf(0)
    var bonusPoints by mutableStateOf(0)
    var isTimesUp by mutableStateOf(false)
    var isEndGame by mutableStateOf(false)
    var currentLongAnswer by mutableStateOf("")
    private val mSocket = SocketHandler.getSocket()

    fun listenToAnswerEvents() {
        onFeedback()
        onBonusPoints()
        onEndGame()
        onTimesUp()
        onGradeAnswers()
        onNextQuestion()
    }

    // TODO : fix on feedback pl0x : args is empty and or null
    // server sends : Feedback :  { score: 0, answerCorrectness: 0, correctAnswer: [ '0' ] }
    fun onFeedback() {
        Log.d("answer service", "called onFeedback")
        mSocket.on(AnswerEvents.FEEDBACK.value) { args ->
            if (args.isNotEmpty() && args[0] != null) {
                val jsonObject = JSONObject(args[0].toString())

                val answerCorrectnessValue = jsonObject.optInt("answerCorrectness")
                val mappedCorrectness =
                    AnswerCorrectness.entries.find { it.value == answerCorrectnessValue }
                        ?: AnswerCorrectness.WRONG

                jsonObject.put("answerCorrectness", mappedCorrectness.name)

                feedback = Gson().fromJson(jsonObject.toString(), Feedback::class.java)

                showFeedback = true
                isNextQuestionButtonEnabled = true
                Log.d("answer service", "next question enabled")
                processFeedback(feedback)
            } else {
                Log.e("answer service", "CACA")
            }
        }
    }


    fun onBonusPoints() {
        mSocket.on(AnswerEvents.BONUS.value) { args ->
            if (args.isNotEmpty() && args[0] != null) {
                bonusPoints = args[0].toString().toInt()
            }
        }
    }

    fun onTimesUp() {
        mSocket.on(AnswerEvents.TIMES_UP.value) {
            isTimesUp = true
            this.isSelectionEnabled = false
        }
    }

    fun onEndGame() {
        mSocket.on(AnswerEvents.END_GAME.value) {
            isEndGame = true
        }
    }

    fun onGradeAnswers() {
        mSocket.on(AnswerEvents.GRADE_ANSWERS.value) { args ->
            if (args.isNotEmpty() && args[0] != null) {
                gradeAnswers = true
                playersAnswers = Gson().fromJson(
                    args[0].toString(),
                    object : TypeToken<List<LongAnswerInfo>>() {}.type
                )
            }
        }
    }

    fun onNextQuestion() {
        mSocket.on(MatchEvents.GO_TO_NEXT_QUESTION.value) {
            resetStateForNewQuestion()
        }
    }

    fun resetStateForNewQuestion() {
        feedback = Feedback(0, AnswerCorrectness.WRONG)
        correctAnswer = emptyList()
        gradeAnswers = false
        isGradingComplete = false
        showFeedback = false
        isSelectionEnabled = true
        answerCorrectness = AnswerCorrectness.WRONG
        bonusPoints = 0
        isNextQuestionButtonEnabled = false
        isTimesUp = false
        isEndGame = false
        currentLongAnswer = ""
        TimeService.isTimerPaused = false
    }

    fun sendGrades() {
        gradeAnswers = false
        val gradesInfo =
            GradesInfo(matchRoomCode = MatchRoomService.getRoomCode(), grades = playersAnswers)
        println(gradesInfo)
        val gradesInfoStringified = Gson().toJson(gradesInfo)
        val gradesInfoJsonObject = JSONObject(gradesInfoStringified)
        mSocket.emit(AnswerEvents.GRADES.value, gradesInfoJsonObject)
    }

    fun handleGrading() {
        isGradingComplete = playersAnswers.all { it.score != null }
        // println("isGradingComplete$isGradingComplete")
    }

    fun selectChoice(choice: String, userInfo: UserInfo) {
        val choiceInfo = ChoiceInfo(choice, userInfo)
        val choiceInfoStringified = Gson().toJson(choiceInfo)
        val choiceInfoJsonObject = JSONObject(choiceInfoStringified)
        mSocket.emit(AnswerEvents.SELECT_CHOICE.value, choiceInfoJsonObject)
    }

    fun deselectChoice(choice: String, userInfo: UserInfo) {
        val choiceInfo = ChoiceInfo(choice, userInfo)
        val choiceInfoStringified = Gson().toJson(choiceInfo)
        val choiceInfoJsonObject = JSONObject(choiceInfoStringified)
        mSocket.emit(AnswerEvents.DESELECT_CHOICE.value, choiceInfoJsonObject)
    }

    fun submitAnswer(userInfo: UserInfo) {
        isSelectionEnabled = false
        val userInfoStringified = Gson().toJson(userInfo)
        val userInfoJsonObject = JSONObject(userInfoStringified)
        mSocket.emit(AnswerEvents.SUBMIT_ANSWER.value, userInfoJsonObject)
    }

    fun updateLongAnswer() {
        println("updating long answer")
        if (!isSelectionEnabled) return
        val userInfo =
            UserInfo(userId = MatchRoomService.userId, roomCode = MatchRoomService.getRoomCode())
        println(userInfo)
        val choiceInfo = ChoiceInfo(currentLongAnswer, userInfo)
        val choiceInfoStringified = Gson().toJson(choiceInfo)
        val choiceInfoJsonObject = JSONObject(choiceInfoStringified)
        mSocket.emit(AnswerEvents.UPDATE_LONG_ANSWER.value, choiceInfoJsonObject)
    }

    private fun processFeedback(feedback: Feedback) {
        feedback.correctAnswer?.let {
            correctAnswer = it
        }

        isSelectionEnabled = false
        answerCorrectness = feedback.answerCorrectness
        playerScore = feedback.score

        MatchRoomService.sendPlayersData(MatchRoomService.getRoomCode())
    }
}
