package com.example.polyquiz.pages.presentation

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.compose.ui.text.font.FontWeight
import com.example.polyquiz.R
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.match.domain.Game
import com.example.polyquiz.constants.MatchContext
import com.example.polyquiz.http.GameService
import com.example.polyquiz.match.domain.MatchContextService
import com.example.polyquiz.match.domain.MatchService



@Composable
fun GameList(modifier: Modifier, navigateToWaitPage: () -> Unit, authViewModel: AuthViewModel) {

    val gameService = GameService()
    val matchService = MatchService
    var games by remember { mutableStateOf<List<Game>>(emptyList()) }
    var popularGames by remember { mutableStateOf<List<Game>>(emptyList()) }
    var selectedGame by remember { mutableStateOf<Game?>(null) }
    var gamesIsValid by remember { mutableStateOf(false) }
    var isLoadingSelectedGame by remember { mutableStateOf(false) }
    val username by remember { mutableStateOf(authViewModel.getUsername() )}
    val userId by remember { mutableStateOf(authViewModel.getUserId()) }
    val isFriendsOnly by remember { mutableStateOf(false) }

    var N_POPULAR_GAMES = 3


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

    fun sortMostPopularGames(){
        if (games.size <= N_POPULAR_GAMES){
            popularGames = games
        }

        val sortedGames = games.sortedWith {game1, game2 ->
            (game2.nMatchesPlayed.toInt() - game1.nMatchesPlayed.toInt())
        }

        if(sortedGames.isNotEmpty())
        {
            popularGames = sortedGames.slice(0..<N_POPULAR_GAMES)
        }

    }
    fun validateGame(selectedGame: Game){
        if(selectedGame.isVisible!!){
            gamesIsValid = true
        }
    }

    fun revalidateGame(isFriendsOnly:Boolean = false){
        if(selectedGame?.isVisible!!){
            gamesIsValid = true
            matchService.currentGame = selectedGame
            matchService.saveBackupGame(selectedGame!!.id!!, userId, username, isFriendsOnly)
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

    fun reloadSelectedGame(isFriendsOnly:Boolean = false){
        gameService.getGameById(selectedGame?.id!!, onSuccess = {
            response ->
            val gson = Gson()
            val game = gson.fromJson(gson.toJson(response), Game::class.java)
            selectedGame = game
            revalidateGame(isFriendsOnly)
        }, onError = {})

    }

    fun createMatch(context: MatchContext, isFriendsOnly:Boolean = false){
        contextService.setContext(context)
        reloadSelectedGame(isFriendsOnly)
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
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(R.string.games_list),
                modifier = Modifier.padding(8.dp),
                fontWeight = FontWeight.Bold
            )
            Column {
                Text(
                    stringResource(R.string.popular_games),
                    modifier = Modifier.padding(8.dp),
                    fontWeight = FontWeight.Bold
                )
                Row {
                    sortMostPopularGames()
                    if (popularGames.isEmpty()) {
                        Text(
                            stringResource(R.string.no_games_available),
                            modifier = Modifier.padding(8.dp),
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        popularGames.forEach({ game ->
                            GameCard(game, onClick = { selectedGame = game })
                        })
                    }
                }
            }
            Text(text = stringResource(R.string.all_games), modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
            if (games.isEmpty()) {
                Text(text = stringResource(R.string.no_games_available), modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
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
                Text(text = stringResource(R.string.game_title) + selectedGame!!.title, modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = stringResource(R.string.games_description),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = selectedGame!!.description,
                    )
                }

                Row(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = stringResource(R.string.games_time),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${selectedGame!!.duration} minutes",
                        fontWeight = FontWeight.Normal
                    )
                }

                Text(text = stringResource(R.string.questions), modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
                selectedGame!!.questions?.forEachIndexed { index, question ->
                    Text("${index + 1}. ${question.text}", modifier = Modifier.padding(8.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))


                Row {
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
                    Button(
                        onClick = {
                            createMatch(MatchContext.HOSTVIEW, true)
                            navigateToWaitPage()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        Text(text = "Jouer avec amis")

                    }

                }


            } else {
                Text(text = stringResource(R.string.select_game), modifier = Modifier.padding(8.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))

        }
    }
}


@Composable
fun GameCard(game: Game, onClick: () -> Unit = {}) {
    Spacer(modifier = Modifier.padding(5.dp))
    Card(colors = CardDefaults.cardColors(
        containerColor = Color.White),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = Modifier.padding()
        .shadow(4.dp, shape = RectangleShape)
        .width(130.dp)
        .height(100.dp)
        ,onClick = onClick, shape = RectangleShape
    ) {
        Column(modifier = Modifier.padding(5.dp)) {
            Text(text = game.title, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.matches_played) + game.nMatchesPlayed.toInt())
            }
        }
    }
}
