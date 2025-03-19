package com.example.polyquiz.pages.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.compose.ui.text.font.FontWeight
import com.example.polyquiz.Game
import com.example.polyquiz.constants.MatchContext
import com.example.polyquiz.http.GameService
import com.example.polyquiz.match.domain.MatchContextService
import com.example.polyquiz.match.domain.MatchService



@Composable
fun GameList(modifier: Modifier, navigateToWaitPage: () -> Unit) {

    val gameService = GameService()
    val matchService = MatchService
    var games by remember { mutableStateOf<List<Game>>(emptyList()) }
    var selectedGame by remember { mutableStateOf<Game?>(null) }
    var gamesIsValid by remember { mutableStateOf(false) }
    var isLoadingSelectedGame by remember { mutableStateOf(false) }

    val contextService = MatchContextService

    LaunchedEffect(Unit) {
        gameService.getGames(
            onSuccess = { fetchedGames ->
                val gson = Gson()
                val json = gson.toJson(fetchedGames)
                val listType = object : TypeToken<List<Game>>() {}.type
                games = gson.fromJson(json, listType)
            },
            onError = { errorMessage -> println("Error: $errorMessage") }
        )
    }
    fun validateGame(selectedGame: Game){
        if(selectedGame.isVisible!!){
            gamesIsValid = true
        }
    }

    fun revalidateGame(){
        if(selectedGame?.isVisible!!){
            gamesIsValid = true
            matchService.currentGame = selectedGame
            matchService.saveBackupGame(selectedGame!!.id!!)
        }
    }

    fun loadSelectedGame(currentGame: Game){
        isLoadingSelectedGame =true
        gameService.getGameById(currentGame.id!!, onSuccess = {
            response ->
            val gson = Gson()
            val game = gson.fromJson(gson.toJson(response), Game::class.java)
            selectedGame = game
            validateGame(selectedGame!!)
        }, onError = {})
    }

    fun reloadSelectedGame(){
        gameService.getGameById(selectedGame?.id!!, onSuccess = {
            response ->
            val gson = Gson()
            val game = gson.fromJson(gson.toJson(response), Game::class.java)
            selectedGame = game
            revalidateGame()
        }, onError = {})

    }

    fun createMatch(context: MatchContext){
        contextService.setContext(context)
        reloadSelectedGame()
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Liste des jeux", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
            if (games.isEmpty()) {
                Text("Aucun jeu disponible", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
            } else {
                games.forEach { game ->
                    ElevatedButton(
                        onClick = {selectedGame = game },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(1.dp),
                        shape = RoundedCornerShape(16.dp)
                    )
                    {
                        Text(text = game.title)
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            if (selectedGame != null) {
                loadSelectedGame(selectedGame!!)
                matchService.currentGame = selectedGame
                Text("Détails du jeu:  ${selectedGame!!.title}", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Description: ",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = selectedGame!!.description,
                    )
                }

                Row(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Durée: ",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${selectedGame!!.duration} minutes",
                        fontWeight = FontWeight.Normal
                    )
                }

                Text("Questions :", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
                selectedGame!!.questions?.forEachIndexed { index, question ->
                    Text("${index + 1}. ${question.text}", modifier = Modifier.padding(8.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))


                Button(
                    onClick = {
                        createMatch(MatchContext.HOSTVIEW)
                        navigateToWaitPage()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                        Text(text = "Jouer")

                }

            } else {
                Text("Sélectionner un jeu dans la liste des jeux", modifier = Modifier.padding(8.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))

        }
    }
}
