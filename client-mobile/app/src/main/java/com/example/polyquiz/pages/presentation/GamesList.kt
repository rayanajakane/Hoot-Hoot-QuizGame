package com.example.polyquiz.pages.presentation

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.sp
import com.example.polyquiz.R
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.match.domain.Game
import com.example.polyquiz.constants.MatchContext
import com.example.polyquiz.http.GameService
import com.example.polyquiz.match.domain.MatchContextService
import com.example.polyquiz.match.domain.MatchService
import com.example.polyquiz.match.domain.PartyConfig
import com.example.polyquiz.match.presentation.PartyConfigDialog
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.polyquiz.SnackbarController
import com.example.polyquiz.SnackbarEvent
import com.example.polyquiz.match.domain.MatchRoomService
import com.example.polyquiz.match.domain.Question

@Composable
fun GameList(modifier: Modifier, navigateToWaitPage: () -> Unit, authViewModel: AuthViewModel) {

    val gameService = GameService()
    val matchService = MatchService
    var games by remember { mutableStateOf<List<Game>>(emptyList()) }
    var popularGames by remember { mutableStateOf<List<Game>>(emptyList()) }
    var selectedGame by remember { mutableStateOf<Game?>(null) }
    var gamesIsValid by remember { mutableStateOf(false) }
    var isLoadingSelectedGame by remember { mutableStateOf(false) }
    val username by remember { mutableStateOf(authViewModel.getUsername()) }
    val userId by remember { mutableStateOf(authViewModel.getUserId()) }
    var showPartyConfigDialog by remember { mutableStateOf(false) }
    var gameIsValidCheaterMode by remember { mutableStateOf(false) }
    var partyConfigs by remember { mutableStateOf(PartyConfig(false, false)) }

    var titleQuery by remember { mutableStateOf("") }
    var authorQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Game>>(emptyList()) }

    val N_POPULAR_GAMES = 3
    val contextService = MatchContextService

    fun performSearch() {
        searchResults = games.filter { game ->
            val matchesTitle = titleQuery.isBlank() ||
                game.title?.contains(titleQuery, ignoreCase = true) ?: false
            val matchesAuthor = authorQuery.isBlank() ||
                game.authorName?.contains(authorQuery, ignoreCase = true) ?: false
            matchesTitle && matchesAuthor
        }
    }

    LaunchedEffect(titleQuery, authorQuery, games) {
        performSearch()
    }

    LaunchedEffect(Unit) {
        gameService.getGames(
            onSuccess = { fetchedGames ->
                val gson = Gson()
                val json = gson.toJson(fetchedGames)
                val listType = object : TypeToken<List<Game>>() {}.type
                games = gson.fromJson(json, listType)
                performSearch()
            },
            onError = { errorMessage -> println("Error: $errorMessage") }
        )
    }

    fun sortMostPopularGames() {
        if (games.size <= N_POPULAR_GAMES) {
            popularGames = games
        }

        val sortedGames = games.sortedWith { game1, game2 ->
            (game2.nMatchesPlayed.toInt() - game1.nMatchesPlayed.toInt())
        }

        if (sortedGames.isNotEmpty()) {
            popularGames = sortedGames.slice(0..<N_POPULAR_GAMES)
        }

    }

    fun fetchGames() {
        gameService.getGames(
            onSuccess = { fetchedGames ->
                val gson = Gson()
                val json = gson.toJson(fetchedGames)
                val listType = object : TypeToken<List<Game>>() {}.type
                games = gson.fromJson(json, listType)
                sortMostPopularGames()
                performSearch()
            },
            onError = { errorMessage ->
                println("Error: $errorMessage")
            }
        )
    }

    LaunchedEffect(Unit) {
        fetchGames()
    }

    fun validateGame(selectedGame: Game) {
        if (selectedGame.isVisible!!) {
            gamesIsValid = true
        } else {
            fetchGames()
        }
    }

    fun revalidateGame(partyConfigs: PartyConfig = PartyConfig(false, false)) {
        if (selectedGame?.isVisible!!) {
            gamesIsValid = true
            matchService.currentGame = selectedGame
            matchService.saveBackupGame(selectedGame!!.id!!, userId, username, partyConfigs)
        } else {
            fetchGames()
        }
    }

    fun canStartCheaterMode(questions: List<Question>): Boolean {
        if (questions.isNotEmpty()) {
            for (question in questions) {
                if (question.type == "QRL") {
                    MatchRoomService.canPlayCheaterMode = false
                    gameIsValidCheaterMode = false;
                    return false
                }
            }
        }
        MatchRoomService.canPlayCheaterMode = true
        gameIsValidCheaterMode = true;
        return true
    }

    fun loadSelectedGame(currentGame: Game) {
        isLoadingSelectedGame = true
        gameService.getGameById(currentGame.id!!,
            onSuccess = { response ->
                val gson = Gson()
                val game = gson.fromJson(gson.toJson(response), Game::class.java)
                selectedGame = game
                validateGame(selectedGame!!)
            },
            onError = { errorMessage ->
                println("Error: $errorMessage")
                fetchGames()
            })
    }

    fun reloadSelectedGame(partyConfigs: PartyConfig = PartyConfig(false, false, 0, false)) {
        gameService.getGameById(selectedGame?.id!!,
            onSuccess = { response ->
                val gson = Gson()
                val game = gson.fromJson(gson.toJson(response), Game::class.java)
                selectedGame = game
                revalidateGame(partyConfigs)
                navigateToWaitPage()
            }, onError = { errorMessage ->
                println("Error: $errorMessage")
                fetchGames()
            })
    }

    fun createMatch(
        context: MatchContext,
        partyConfigs: PartyConfig = PartyConfig(false, false, 0, false)
    ) {
        contextService.setContext(context)
        reloadSelectedGame(partyConfigs)
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.games_list),
                    modifier = Modifier.padding(8.dp),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Column {
                    Text(
                        stringResource(R.string.popular_games),
                        modifier = Modifier.padding(8.dp),
                        fontWeight = FontWeight.Bold
                    )
                    Row {
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
            }
            item {
                Text(
                    text = stringResource(R.string.all_games),
                    modifier = Modifier.padding(8.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            if (games.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.no_games_available),
                        modifier = Modifier.padding(8.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                item {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.search_games),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            OutlinedTextField(
                                value = titleQuery,
                                onValueChange = { titleQuery = it },
                                label = { stringResource(R.string.game_title) },
                                modifier = Modifier
                                    .weight(0.5f)
                                    .padding(bottom = 8.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = authorQuery,
                                onValueChange = { authorQuery = it },
                                label = { stringResource(R.string.game_author) },
                                modifier = Modifier.weight(0.5f),
                                singleLine = true
                            )
                        }
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.game_title),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.game_author),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(searchResults) { game ->
                    ElevatedButton(
                        onClick = { selectedGame = game },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(1.dp),
                        shape = RoundedCornerShape(3.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = game.title,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = game.authorName ?: "",
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .weight(1f)
                .fillMaxHeight()
        ) {
            Row(

                modifier = Modifier.padding(2.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = stringResource(R.string.games_details),
                    modifier = Modifier.padding(8.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                )
                if (selectedGame != null) {
                    loadSelectedGame(selectedGame!!)
                    matchService.currentGame = selectedGame
                    Column(modifier = Modifier.fillMaxWidth(.6f)) {
                        Text(
                            text = stringResource(R.string.game_title) + selectedGame!!.title,
                            modifier = Modifier.padding(8.dp),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
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

                        Text(
                            text = stringResource(R.string.questions),
                            modifier = Modifier.padding(8.dp),
                            fontWeight = FontWeight.Bold
                        )
                        selectedGame!!.questions?.forEachIndexed { index, question ->
                            Text(
                                "${index + 1}. ${question.text}",
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }


                    Column() {
//                        Button(
//                            onClick = {
//                                createMatch(MatchContext.HOSTVIEW)
//                            },
//                            shape = RoundedCornerShape(5.dp),
//                            colors = ButtonDefaults.buttonColors(
//                                containerColor = MaterialTheme.colorScheme.primary,
//                                contentColor = MaterialTheme.colorScheme.onPrimary
//                            ),
//                            modifier = Modifier
//                                .padding(10.dp)
//                                .width(160.dp)
//                                .height(60.dp)
//                        ) {
//                            Text(text = stringResource(R.string.play))
//
//                        }
                        Button(
                            onClick = {
                                canStartCheaterMode(selectedGame!!.questions!!);
                                showPartyConfigDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary
                            ),
                            shape = RoundedCornerShape(5.dp),
                            modifier = Modifier
                                .padding(10.dp)
                                .width(160.dp)
                                .height(60.dp)

                        )
                        {
//                            Row(modifier = Modifier.fillMaxWidth()) {
////                                Icon(
////                                    Icons.Filled.Settings,
////                                    contentDescription = stringResource(R.string.play),
////                                    modifier = Modifier
////                                        .offset(x = -18.dp, y = 5.dp),
////                                )
                                Text(
                                    text = stringResource(R.string.play),
                                    textAlign = TextAlign.Center,
                                )
//                            }
                        }

                    }


                } else {
                    Text(
                        text = stringResource(R.string.select_game),
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

            }
            if (showPartyConfigDialog) {
                PartyConfigDialog(
                    initialPartyConfig = partyConfigs,
                    onConfirm = { updatedConfigs ->
                        createMatch(MatchContext.HOSTVIEW, updatedConfigs)
                        partyConfigs = updatedConfigs
                        showPartyConfigDialog = false

                    },
                    onCancel = {
                        showPartyConfigDialog = false
                    }
                )
            }

        }
    }
}


@Composable
fun GameCard(game: Game, onClick: () -> Unit = {}) {
    Spacer(modifier = Modifier.padding(5.dp))
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        shape = RoundedCornerShape(3.dp),
        modifier = Modifier
            .padding()
            .shadow(4.dp, shape = RectangleShape)
            .width(130.dp)
            .height(155.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = game.title, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.matches_played) + game.nMatchesPlayed.toInt())
            }
        }
    }
}

