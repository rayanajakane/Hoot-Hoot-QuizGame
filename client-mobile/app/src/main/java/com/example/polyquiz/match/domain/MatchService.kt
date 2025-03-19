package com.example.polyquiz.match.domain

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.polyquiz.Game
import com.example.polyquiz.http.GameService
import com.example.polyquiz.http.CommunicationService
import com.google.gson.Gson

object MatchService {
    val gameService = GameService()
    var currentGame by mutableStateOf<Game?>(null)
    var matchRoomService = MatchRoomService

    val backupService = object : CommunicationService("match/backups") {
        override val apiService: ApiService = retrofit.create(ApiService::class.java)
    }

    fun createMatch(hostId: String, hostUsername: String){
        matchRoomService.connect()
        Log.d("NADA create match", "${hostId} et ${hostUsername}")
        Log.d("NADA", hostId)

        matchRoomService.createRoom(currentGame!!.id!!, hostId, hostUsername)
    }

    fun getBackupGame(id:String){
        return gameService.getGameById(id, onSuccess = {}, onError = {}, "match/backups/${id}")
    }

    fun getAllGames(){
        return gameService.getGames(onSuccess = {}, onError = {})
    }

    fun saveBackupGame(id: String, hostId: String, hostUsername: String){
            return backupService.add(
                currentGame!!,
                onSuccess = { response ->
                    val gson = Gson()
                    val game = gson.fromJson(gson.toJson(response), Game::class.java)
                    currentGame = game
                    Log.d("NADA MATCHSERVICE SAVEBACKUP", "${hostId} et ${hostUsername}")
                    createMatch(hostId, hostUsername)
                },
                onError = { error -> println(error)

                          },
                id
            )
    }
}
