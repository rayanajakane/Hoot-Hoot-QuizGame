package com.example.polyquiz.http

import com.example.polyquiz.match.domain.Game
import com.google.gson.reflect.TypeToken

val BASE_URL: String = "admin/games"
typealias ErrorCallback = (String) -> Unit

class GameService : CommunicationService(BASE_URL) {

    override val apiService: ApiService = retrofit.create(GameApiService::class.java)

    fun getGames(onSuccess: (List<Game>) -> Unit, onError: (String) -> Unit, endpoint: String="") {
        getAll(
            { response ->
                onSuccess(
                    convertJsonResponseToType(
                        response,
                        object : TypeToken<List<Game>>() {}.type
                    )
                )
            },
            onError
        )
    }

        fun getGameById(id: String, onSuccess: (Any) -> Unit, onError: (String) -> Unit, endpoint: String="") {
            getById(
                id,
                { response ->
                    onSuccess(convertJsonResponseToType(
                        response,
                        Game::class.java)
                    )
                },
                onError
            )
        }

        fun addGame(newGame: Game, onSuccess: (Any) -> Unit, onError: ErrorCallback, endpoint: String = "") {
            add(newGame,
                { response ->
                    onSuccess(convertJsonResponseToType(
                        response,
                        Game::class.java)
                    )
                },
                onError, endpoint
            )
        }

        fun modifyGame(id: String, newGame: Game, onSuccess: () -> Unit, onError: (String) -> Unit) {
            put(newGame, id, onSuccess, onError);
        }


        fun deleteGame(id: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
            delete(id, onSuccess, onError)
        }

    interface GameApiService : ApiService {
    }
}

