package com.example.polyquiz.elo.domain

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.polyquiz.constants.Ranking
import com.example.vanillaprototype.socket.SocketHandler
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.json.JSONObject
import kotlin.math.roundToInt
data class EloResponse(val mu: Double)

object EloService {
    private val mSocket = SocketHandler.getSocket()
    var _rankings = MutableLiveData<List<Ranking>>()
    var currentRating = MutableLiveData<Int>()

    fun listenForEloEvents() {
        onReturnRankings()
    }

    fun stopListeningForEloEvents() {
        mSocket.off(EloEvents.RETURNRANKINGS.value)
    }


    fun onReturnRankings() {
        mSocket.on(EloEvents.RETURNRANKINGS.value) { args ->
            if (args[0] != null) {
                val rankingsList =
                    Gson().fromJson(args[0].toString(), Array<Ranking>::class.java).toList()
                val sortedRankings = rankingsList
                    .sortedByDescending { it.rating }
                    .map { it.copy(rating = it.rating) }

                _rankings.postValue(sortedRankings)
            }
        }
    }

    fun getRankings() {
        mSocket.emit(EloEvents.GETRANKINGS.value)
    }

    fun getElo(userId: String) {
        mSocket.emit(EloEvents.GETELO.value, userId)
        Log.d("EloService", "Sending userId: $userId")

    }

    fun updateEloForMatch(roomCode: String) {
        mSocket.emit(EloEvents.UPDATEELOFORMATCH.value, roomCode)
    }

fun returnElo() {
    mSocket.on(EloEvents.RETURNELO.value) { args ->
        Log.d("returnElo", "Raw response: ${args[0]}")

        if (args[0] != null) {
            try {
                val jsonString = args[0].toString()

                val response = Gson().fromJson(jsonString, EloResponse::class.java)

                val rating = response.mu
                currentRating.postValue(rating.roundToInt())

            } catch (e: JsonSyntaxException) {
                // Log the error if parsing fails
                Log.e("returnElo", "Error parsing JSON: ${e.message}")
            }
        }
    }
}
}


