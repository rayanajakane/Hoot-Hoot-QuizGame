package com.example.polyquiz.match.domain

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.vanillaprototype.socket.SocketHandler
import com.example.polyquiz.constants.TimerEvents
import com.example.polyquiz.constants.TimerInfo
import com.google.gson.Gson

object TimeService {
    var isTimerPaused: Boolean = false
    private var counter: MutableState<Int> = mutableStateOf(0)
    private var initialValue: Int = 0
    private val mSocket = SocketHandler.getSocket()

    val time: Int
        get() = counter.value

    val duration: Int
        get() = initialValue

    var timeSetter: Int
        get() = counter.value
        set(newTime) {
            counter.value = newTime
        }

    fun handleTimer() {
        mSocket.on(TimerEvents.TIMER.value) { args ->
            if (args.isNotEmpty() && args[0] != null) {
                val timerInfo = Gson().fromJson(args[0].toString(), TimerInfo::class.java)
                counter.value = timerInfo.currentTime
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
        val progress = (counter.value.toFloat() / duration.toFloat()) * 100
        return progress
    }

}
