package com.example.polyquiz.match.domain

import com.example.polyquiz.constants.AnswerCorrectness
import com.example.polyquiz.constants.AnswerEvents
import com.example.polyquiz.constants.Feedback
import com.example.polyquiz.constants.GradesInfo
import com.example.polyquiz.constants.LongAnswerInfo
import com.example.polyquiz.constants.MatchEvents
import com.example.polyquiz.constants.UserInfo
import com.example.vanillaprototype.socket.SocketHandler
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object AnswerService {
    var playersAnswers: List<LongAnswerInfo> = emptyList()
    var feedback: Feedback = Feedback(0, AnswerCorrectness.WRONG, emptyList())
    var gradeAnswers: Boolean = false
    var isGradingComplete: Boolean = false
    var showFeedback: Boolean = false
    var isNextQuestionButtonEnabled: Boolean = false
    var isSelectionEnabled: Boolean = false
    var correctAnswer: List<String> = emptyList()
    var answerCorrectness: AnswerCorrectness = AnswerCorrectness.WRONG
    var playerScore: Int = 0
    var bonusPoints: Int = 0
    var isTimesUp: Boolean = false
    var isEndGame: Boolean = false
    var currentLongAnswer: String = ""
    private val mSocket = SocketHandler.getSocket()

    fun listenToAnswerEvents() {
        onFeedback()
        onBonusPoints()
        onEndGame()
        onTimesUp()
        onGradeAnswers()
        onNextQuestion()
    }
    fun onFeedback() {
        mSocket.on(AnswerEvents.FEEDBACK.value) { args ->
            if (args.isNotEmpty() && args[0] != null) {
                feedback = Gson().fromJson(args[0].toString(), Feedback::class.java)
                showFeedback = true
                isNextQuestionButtonEnabled = true
                processFeedback(feedback)
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
                playersAnswers = Gson().fromJson(args[0].toString(), object : TypeToken<List<LongAnswerInfo>>() {}.type)
            }
        }
    }
    fun onNextQuestion() {
        mSocket.on(MatchEvents.GO_TO_NEXT_QUESTION.value) {
            resetStateForNewQuestion()
        }
    }

    fun submitAnswer(userInfo: UserInfo) {
        isSelectionEnabled = false
        mSocket.emit(AnswerEvents.SUBMIT_ANSWER.value, Gson().toJson(userInfo))
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
        val gradesInfo = GradesInfo(matchRoomCode = MatchRoomService.getRoomCode(), grades = playersAnswers)
        mSocket.emit(AnswerEvents.GRADES.value, Gson().toJson(playersAnswers))
    }

    fun handleGrading() {
        isGradingComplete = playersAnswers.all { it.score != null }
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
