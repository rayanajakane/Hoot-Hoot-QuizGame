package com.example.polyquiz.elo.domain

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.polyquiz.constants.Ranking
import com.example.polyquiz.match.domain.Player
import com.example.vanillaprototype.socket.SocketHandler
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.json.JSONObject
import kotlin.math.roundToInt
data class EloResponse(val mu: Double)

object EloService {
    private val mSocket = SocketHandler.getSocket()
    var _rankings = MutableLiveData<List<Player>>()
//    var rankings = MutableLiveData<List<Player>>()
    var currentRating = MutableLiveData<Int>()

    fun listenForEloEvents() {
        onReturnRankings()
    }

    fun stopListeningForEloEvents() {
        mSocket.off(EloEvents.RETURN_RANKINGS.value)
    }


    fun onReturnRankings() {
        mSocket.on(EloEvents.RETURN_RANKINGS.value) { args ->
            if (args[0] != null) {
                val rankingsList =
                    Gson().fromJson(args[0].toString(), Array<Ranking>::class.java).toList()
                val sortedRankings = rankingsList
                    .sortedByDescending { it.rating }
                    .map { ranking -> Player(id = "",username = ranking.username, score = ranking.rating.roundToInt(), bonusCount = 0, isPlaying = false, isChatActive = false, state = "") } // Convert to Player


                _rankings.postValue(sortedRankings)

            }
        }
    }


    fun getRankings() {
        mSocket.emit(EloEvents.GET_RANKINGS.value)
    }

    fun getElo(userId: String) {
        mSocket.emit(EloEvents.GET_ELO.value, userId)
        Log.d("EloService", "Sending userId: $userId")

    }

    fun updateEloForMatch(roomCode: String) {
        mSocket.emit(EloEvents.UPDATE_ELO_FOR_MATCH.value, roomCode)
    }

fun returnElo() {
    mSocket.on(EloEvents.RETURN_ELO.value) { args ->
        Log.d("returnElo", "Raw response: ${args[0]}")

        if (args[0] != null) {
            try {
                val jsonString = args[0].toString()

                val response = Gson().fromJson(jsonString, EloResponse::class.java)

                val rating = response.mu
                currentRating.postValue(rating.roundToInt())

            } catch (e: JsonSyntaxException) {
                Log.e("returnElo", "Error parsing JSON: ${e.message}")
            }
        }
    }
}
}


