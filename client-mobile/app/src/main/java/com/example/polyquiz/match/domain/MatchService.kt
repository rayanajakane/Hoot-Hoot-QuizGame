package com.example.polyquiz.match.domain

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

    fun createMatch(hostId: String, hostUsername: String, partyConfigs: PartyConfig = PartyConfig(false, false), isClassicMode : Boolean = false){
        matchRoomService.connect()
        matchRoomService.createRoom(currentGame!!.id!!, hostId, hostUsername, isClassicMode, partyConfigs)
    }

    fun getBackupGame(id:String){
        return gameService.getGameById(id, onSuccess = {}, onError = {}, "match/backups/${id}")
    }

    fun getAllGames(){
        return gameService.getGames(onSuccess = {}, onError = {})
    }

    fun saveBackupGame(id: String, hostId: String , hostUsername: String, partyConfigs: PartyConfig = PartyConfig(false, false, 0, false)){
            return backupService.add(
                currentGame!!,
                onSuccess = { response ->
                    val gson = Gson()
                    val game = gson.fromJson(gson.toJson(response), Game::class.java)
                    currentGame = game
                    println("Game saved")
                    createMatch(hostId, hostUsername, partyConfigs )
                },
                onError = { error -> println(error)

                          },
                id
            )
    }
}
