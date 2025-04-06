package com.example.polyquiz.match.domain

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.vanillaprototype.socket.SocketHandler
import com.example.polyquiz.constants.TimerEvents
import com.example.polyquiz.constants.TimerInfo
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object TimeService {
    private val _isTimerPaused = MutableStateFlow(false)
    val isTimerPaused: StateFlow<Boolean> get() = _isTimerPaused

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

    fun setIsTimerPaused(value: Boolean) {
        _isTimerPaused.value = value
    }

    fun listenToTimerEvents() {
        handleTimer()
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

    fun pauseTimer(roomCode: String) {
        _isTimerPaused.value = !_isTimerPaused.value
        mSocket.emit(TimerEvents.PAUSE_TIMER.value, roomCode)
    }

    fun computeTimerProgress(): Float {
        val progress = (counter.value.toFloat() / duration.toFloat()) * 100
        return if(progress.isNaN()) {
            0f
        } else {
            progress
        }
    }

}
