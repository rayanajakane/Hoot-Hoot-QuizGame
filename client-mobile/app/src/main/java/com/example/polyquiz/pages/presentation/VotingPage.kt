package com.example.polyquiz.pages.presentation

import android.service.autofill.FieldClassification.Match
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.polyquiz.R
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.chat.presentation.ChatComponent
import com.example.polyquiz.constants.MatchContext
import com.example.polyquiz.constants.MatchEvents
import com.example.polyquiz.constants.PresetAvatar
import com.example.polyquiz.constants.VotingData
import com.example.polyquiz.match.domain.AnswerService
import com.example.polyquiz.match.domain.MatchContextService
import com.example.polyquiz.match.domain.MatchRoomService
import com.example.polyquiz.match.domain.Player
import com.example.polyquiz.match.presentation.PlayersListComponent
import com.example.polyquiz.ui.theme.AndroidGreen
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun VotingPage(
    authViewModel: AuthViewModel,
    players: List<Player>,
    matchRoomService: MatchRoomService,
    matchContextService: MatchContextService,
    onVote: (VotingData) -> Unit,
    navigateToResultsPage: () -> Unit,
    navigateToHome: () -> Unit,
) {
    var selectedPlayer by remember { mutableStateOf<String?>(null) }
    var voteCounts by remember { mutableStateOf(VotingData("", 0, mutableListOf())) }
    var totalVotes by remember { mutableStateOf(0) }
    val playersPlaying = remember(players) { players.filter { it.isPlaying } }
    val currentVotes = remember(matchRoomService.votesResults) {
        matchRoomService.votesResults.values.sum()
    }
    val votedPlayers = remember { mutableStateListOf<String>() }

    var totalVotesOfActivePlayers: Int = matchRoomService.votesResults.values.sum()


    var isVotingDisabled by remember { mutableStateOf(true) }
    var context = MatchContextService.getContext()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit, MatchRoomService.isTimeToNavigateToResults) {
        when (MatchRoomService.isTimeToNavigateToResults) {
            true -> {
                navigateToResultsPage();
            }

            else -> Unit
        }
    }

    LaunchedEffect(MatchRoomService.userVoted) {
        if (MatchRoomService.userVoted.isNotEmpty()) {
            votedPlayers.add(MatchRoomService.userVoted)
        }
    }


    Row(
        horizontalArrangement = Arrangement.spacedBy(26.dp),
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            }
            .statusBarsPadding()
    ) {
        ChatComponent(modifier = Modifier, authViewModel = authViewModel)

        Column(
            modifier = Modifier
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (context != MatchContext.HOSTVIEW) {
                Text(
                    text = stringResource(R.string.cheater_mode_vote),
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(players) { player ->
                        if (player.username != matchRoomService.retrieveUsername()) {
                            PlayerVoteCard(
                                player = player,
                                selectedPlayer = selectedPlayer,
                                onPlayerSelected = {
                                    selectedPlayer = it
                                    isVotingDisabled = false
                                },
                                isVotingDisabled = false,
                                voteCounts = voteCounts
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        selectedPlayer?.let {
                            voteCounts.username = it
                            voteCounts.numberOfVotes += 1
                            voteCounts.usersWhoVoted.add("'${matchRoomService.retrieveUsername()}'")
                            totalVotes++
                            onVote(voteCounts)
                            isVotingDisabled = true
                            val usersWhoVotedArray = JSONArray(voteCounts.usersWhoVoted)
                            val sentInfo = JSONObject().apply {
                                put("username", voteCounts.username)
                                put("numberOfVotes", voteCounts.numberOfVotes)
                                put("usersWhoVoted", usersWhoVotedArray)
                            }
                            matchRoomService.sendBackVotesResult(sentInfo)
                        }
                    }, modifier = Modifier, enabled = !isVotingDisabled
                ) {
                    Text(stringResource(R.string.cheater_mode_vote))
                }
            } else {
                if (playersPlaying.size != totalVotesOfActivePlayers) {
                    Text(stringResource(R.string.cheater_mode_players_still_voting))
                } else {
                    Text(stringResource(R.string.cheater_mode_players_voted))
                }
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(votedPlayers) { player ->
                        PlayerVotedCard(player = player)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
            ) {
                if (context === MatchContext.HOSTVIEW && !matchRoomService.isCooldown) {
                    Button(
                        onClick = {
                            MatchRoomService.socket.emit(
                                MatchEvents.SEND_UPDATED_SCORES.value,
                                MatchRoomService.matchRoomCode.value
                            )
                            matchRoomService.routeToResultsPage();
                            navigateToResultsPage();
                        },
                        shape = RoundedCornerShape(5.dp),
                    ) {
                        Icon(
                            Icons.Filled.BarChart,
                            contentDescription = stringResource(R.string.show_final)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(R.string.show_final))
                    }
                }
            }
        }
        PlayersListComponent(
            matchRoomService = matchRoomService,
            context = matchContextService,
            players = matchRoomService.players,
            modifier = Modifier
                .width(250.dp)
                .fillMaxHeight()
                .navigationBarsPadding(),
            extraContent = {
                Column {
                    Button(
                        onClick = {
                            matchRoomService.isQuitting = true
                            matchRoomService.disconnectFromRoom()
                            navigateToHome()
                        },
                        shape = RoundedCornerShape(3.dp),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = stringResource(R.string.leave)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(R.string.leave))
                    }
                }
            }
        )
    }
}

@Composable
fun PlayerVotedCard(player: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceBright,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = player + stringResource(R.string.cheater_mode_player_voted),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}


@Composable
fun PlayerVoteCard(
    player: Player,
    selectedPlayer: String?,
    onPlayerSelected: (String) -> Unit,
    voteCounts: VotingData,
    isVotingDisabled: Boolean =true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .padding(vertical = 4.dp)
            .clickable { onPlayerSelected(player.username) },
        shape = RoundedCornerShape(3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceBright,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selectedPlayer == player.username,
                onClick = { onPlayerSelected(player.username) },
                colors = RadioButtonDefaults.colors(
                    selectedColor = AndroidGreen, unselectedColor = Color.Gray
                ),
                enabled = isVotingDisabled
            )
            PlayerInfo(player = player)
        }
    }
}

@Composable
fun PlayerInfo(player: Player) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = player.photoUrl,
            contentDescription = "Player Avatar",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            placeholder = rememberAsyncImagePainter(model = PresetAvatar.DEFAULT.value)
        )
        Spacer(modifier = Modifier.width(8.dp))
        if (!player.isPlaying) {
            Text(
                text = player.username,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                style = TextStyle(textDecoration = TextDecoration.LineThrough)
            )
        } else {
            Text(
                text = player.username,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
