package com.example.polyquiz.classicmode.domain

import com.example.vanillaprototype.socket.SocketHandler
import com.example.polyquiz.constants.TimerEvents
import com.example.polyquiz.constants.TimerInfo
import com.google.gson.Gson

object TimeService {
    var isTimerPaused: Boolean = false
    //var isPanicModeDisabled: Boolean = false
    //var isPanicking: Boolean = false
    //var isAlertDisplayed: Boolean = false
   // var alertSymbol: String = ""
    private var counter: Int = 0
    private var initialValue: Int = 0

    private val mSocket = SocketHandler.getSocket()

    val time: Int
        get() = counter

    val duration: Int
        get() = initialValue

    var timeSetter: Int
        get() = counter
        set(newTime) {
            counter = newTime
        }

    fun handleTimer() {
        mSocket.on(TimerEvents.TIMER.value) { args ->
            if (args.isNotEmpty() && args[0] != null) {
                val timerInfo = Gson().fromJson(args[0].toString(), TimerInfo::class.java)
                counter = timerInfo.currentTime
                initialValue = timerInfo.duration
            }
        }
    }
    fun startTimer(roomCode: String, time: Int) {
        val timeCodeObject = mapOf("roomCode" to roomCode, "time" to time)
        val timeJsonObject = Gson().toJson(timeCodeObject)
        mSocket.emit(TimerEvents.START_TIMER.value, timeJsonObject);
    }
    fun stopTimer(roomCode: String) {
        val roomCodeObject = mapOf("roomCode" to roomCode)
        val roomJsonObject = Gson().toJson(roomCodeObject)
        mSocket.emit(TimerEvents.STOP_TIMER.value, roomJsonObject);
    }

    fun computeTimerProgress(): Float {
        val progress = (counter.toFloat() / duration.toFloat()) * 100
        return progress
    }

//    fun joinRoom(roomCode: String, username: String) {
//        val roomusernameObject = mapOf("roomCode" to roomCode, "username" to username)
//        val roomCodeJsonObject = Gson().toJson(roomusernameObject)
//        mSocket.emit("joinRoom", roomCodeJsonObject)
//        val roomCodeObject = mapOf("roomCode" to roomCode)
//        val roomJsonObject = Gson().toJson(roomCodeObject)
//        mSocket.emit("sendPlayersData", roomJsonObject)
//    }
}
