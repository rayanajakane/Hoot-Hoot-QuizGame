package com.example.polyquiz.pages.presentation

import android.annotation.SuppressLint
import android.service.autofill.FieldClassification.Match
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.constants.MatchButtonActions
import com.example.polyquiz.constants.MatchContext
import com.example.polyquiz.constants.StartMatchFeedback
import com.example.polyquiz.match.domain.Game
import com.example.polyquiz.match.domain.MatchContextService
import com.example.polyquiz.match.domain.MatchRoomService
import com.example.polyquiz.match.domain.MatchRoomService.gameTitle
import com.example.polyquiz.match.domain.MatchRoomService.isLocked
import com.example.polyquiz.match.domain.MatchRoomService.players
import com.example.polyquiz.match.domain.MatchService
import com.example.polyquiz.match.domain.MatchService.matchRoomService
import com.example.polyquiz.match.domain.TimeService
import com.example.polyquiz.match.presentation.TimerComponent

@SuppressLint("UnrememberedMutableState")
@Composable

fun WaitPage(modifier: Modifier, navigateToHome: () -> Unit, authViewModel: AuthViewModel, navigateToMatchRoom: () -> Unit) {
   // var isLocked: Boolean = false
    var isHostPlaying: Boolean = false
    val matchService = MatchService
    val timeService = TimeService
   // var isLocked by mutableStateOf(false)


    fun resetWaitPage() {
        isLocked = false
        MatchRoomService.isHostPlaying = true
        MatchRoomService.isBanned = false
        MatchRoomService.isQuitting = false
        MatchRoomService.isMatchStarted = false

    }

    fun getTime(): Int {
        return timeService.time
    }

    fun isHost(): Boolean {
        println(MatchRoomService.hostId)
        println( MatchRoomService.hostId == authViewModel.getUserId())
        return MatchRoomService.hostId == authViewModel.getUserId()
    }

    fun getCurrentGame(): Game {
        return matchService.currentGame!!;
    }

    LaunchedEffect(MatchRoomService.isTimeToNavigate, MatchRoomService.username) {
        if (MatchRoomService.isTimeToNavigate) {
            MatchRoomService.isTimeToNavigate = false
            navigateToMatchRoom()
        }
        if (isHost()) {
            gameTitle = getCurrentGame().title
            MatchContextService.setContext(MatchContext.HOSTVIEW)
        } else {
            MatchContextService.setContext(MatchContext.PLAYERVIEW)
        }
    }

    LaunchedEffect(MatchRoomService.hasBeenKickedOut) {
        if (MatchRoomService.hasBeenKickedOut) {
            MatchRoomService.hasBeenKickedOut = false
            navigateToHome()
        }
    }

    LaunchedEffect(Unit) {
        resetWaitPage()
        timeService.listenToTimerEvents()
    }

    fun toggleLock() {
        MatchRoomService.toggleLock()
    }

    fun banPlayerUsername(userId: String) {
        if (userId === matchRoomService.hostId) {
            return
        }
        println(userId)
        MatchRoomService.banUsername(userId)
    }

    fun startMatch() {
        MatchRoomService.startMatch()
    }

    fun quitMatch() { //originellement quitGame sur le client lourd
        MatchRoomService.disconnectFromRoom()
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { quitMatch(); navigateToHome() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(MatchButtonActions.LEAVE_MATCH.value)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            if (MatchRoomService.isMatchStarted) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Le jeu $gameTitle commence dans..",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    TimerComponent(
                        modifier = Modifier.fillMaxWidth(),
                        timeService = timeService
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = StartMatchFeedback.WAITING_TO_START.value,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    if (isHost()) {
                        Text(text = "Code d'accès: ${MatchRoomService.getRoomCode()}")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(StartMatchFeedback.LOCK_MATCH.value)
                            Spacer(modifier = Modifier.width(8.dp))
                             Switch(checked = MatchRoomService.isLocked, onCheckedChange = { toggleLock() })
                        }
                        Button(
                            onClick = { startMatch()
                                },
                            enabled = MatchRoomService.isLocked && players.isNotEmpty()

                        ) {
                            Text(MatchButtonActions.START_MATCH.value)
                        }
                    }
                    players.forEach { player ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(player.username)
                            if (isHost() && player.id != matchRoomService.hostId) {
                                Button(onClick = { banPlayerUsername(player.id) }) {
                                    Text(MatchButtonActions.BAN_PLAYER.value)
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }

}



