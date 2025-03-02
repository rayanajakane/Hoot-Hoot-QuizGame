package com.example.polyquiz

import android.net.http.UrlRequest
import com.google.android.gms.common.api.Response
import com.google.gson.Gson
import retrofit2.Call

val base_url: String = "admin/games"
typealias ErrorCallback = (String) -> Unit

class GameService : CommunicationService<Game>(base_url) {

    override val apiService: ApiService = retrofit.create(GameApiService::class.java)

    fun getGames(onSuccess: (List<Game>) -> Unit, onError: (String) -> Unit) {
        getAll(onSuccess, onError)
    }

    fun getGameById(id: String, onSuccess: (Any) -> Unit, onError: (String) -> Unit) {
        getById(id, onSuccess, onError)
    }

    fun modifyGame(id: String, newGame: Game, onSuccess: (Game) -> Unit, onError: (String) -> Unit){
        put(newGame, id, onSuccess, onError);
    }

    fun addGame(newGame: Game, onSuccess: (Any) -> Unit, onError: ErrorCallback) {
        add(newGame, onSuccess, onError)
    }

    fun deleteGame(id: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        delete(id, onSuccess, onError)
    }


    interface GameApiService : ApiService {
    }
}

