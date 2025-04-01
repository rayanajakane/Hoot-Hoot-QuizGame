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

        // Create a proper JSON object
//        val userIdJsonObject = JSONObject().apply {
//            put(userId) // Correctly adds it as {"userId": "12345"}
//        }

        // Send the JSON object to the server
//        Log.d("EloService", "Sending JSON object: $userIdJsonObject")
        mSocket.emit(EloEvents.GETELO.value, userId)
        Log.d("EloService", "Sending userId: $userId")

    }

    fun updateEloForMatch(roomCode: String) {
        val roomCodeStringified = Gson().toJson(roomCode)
        val roomCodeJsonObject = JSONObject(roomCodeStringified)
        mSocket.emit(EloEvents.UPDATEELOFORMATCH.value, roomCodeJsonObject)
    }

//    fun returnElo() {
//        mSocket.on(EloEvents.RETURNELO.value) { args ->
//            if (args[0] != null) {
////                val rating = Gson().fromJson(args[0].toString(), Double::class.java)
////                currentRating.postValue(args[0].roundToInt())
//                Log.d("rating: ${args[0]}")
//            }
//        }
//fun returnElo() {
//    mSocket.on(EloEvents.RETURNELO.value) { args ->
//        Log.d("returnElo", "Raw response: ${args[0]}")
//        if (args[0] != null) {
//            val rating = Gson().fromJson(args[0].toString(), Double::class.java)
//            Log.d("rating","rating: ${rating}")
//            currentRating.postValue(rating.roundToInt())
//        }
//    }
//}
fun returnElo() {
    mSocket.on(EloEvents.RETURNELO.value) { args ->
        Log.d("returnElo", "Raw response: ${args[0]}")

        if (args[0] != null) {
            try {
                // Log the string version of the argument before parsing
                val jsonString = args[0].toString()
                Log.d("returnElo", "Received JSON string: $jsonString")

                // Parse the response into the EloResponse data class
                val response = Gson().fromJson(jsonString, EloResponse::class.java)
                Log.d("returnElo", "Parsed response: $response")

                // Extract the 'mu' value and update the rating
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


