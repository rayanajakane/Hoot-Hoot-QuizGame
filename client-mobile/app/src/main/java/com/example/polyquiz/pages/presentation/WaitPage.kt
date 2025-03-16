package com.example.polyquiz.pages.presentation

import MatchContextService
import android.annotation.SuppressLint
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.chat.presentation.ChatComponent
import com.example.polyquiz.constants.HOST_USERNAME
import com.example.polyquiz.constants.MatchButtonActions
import com.example.polyquiz.constants.MatchContext
import com.example.polyquiz.constants.StartMatchFeedback
import com.example.polyquiz.match.domain.MatchRoomService
import com.example.polyquiz.match.domain.MatchRoomService.gameTitle
import com.example.polyquiz.match.domain.MatchRoomService.players
import com.example.polyquiz.match.domain.TimeService
import com.example.polyquiz.match.presentation.TimerComponent

@SuppressLint("UnrememberedMutableState")
@Composable
fun WaitPage(modifier: Modifier, navigateToHome: () -> Unit, authViewModel: AuthViewModel, navigateToMatchRoom: () -> Unit) {
    var isLocked: Boolean = false
    var isHostPlaying: Boolean = false


    fun toggleLock() {
        MatchRoomService.toggleLock()
    }
    fun banPlayerUsername(username: String) {
        if (username === HOST_USERNAME) {return}
        MatchRoomService.banUsername(username)
    }
    fun startMatch() {
        MatchRoomService.startMatch()
    }
    fun quitMatch() { //originellement quitGame sur le client lourd
        MatchRoomService.disconnectFromRoom()
    }

    fun resetWaitPage() {
        isLocked = false
        MatchRoomService.isMatchStarted = false
        //MatchRoomService.isHostPlaying = true
        MatchRoomService.isBanned = false
        MatchRoomService.isQuitting = false
    }
    fun getTime(): Int {
        return TimeService.time
    }
    fun isHost() : Boolean {
        return MatchRoomService.getUsername() === "Organisateur"
    }
//    fun getCurrentGame(): Game {
//        return MatchService.currentGame()
//    }

    LaunchedEffect(Unit, MatchRoomService.isTimeToNavigate, MatchRoomService.hasBeenKickedOut) {
        resetWaitPage()
        TimeService.handleTimer() //pour l'organisateur, il faudrait un listenToTimerEvents()

        if (isHost()) {
            MatchRoomService.gameTitle =
                "CECI EST HARDCODÉ, À IMPLÉMENTER: MATCH SERVICE "
        } else {
                MatchContextService.setContext(MatchContext.PLAYERVIEW)
            }
        when (MatchRoomService.isTimeToNavigate) {
            true -> {
                MatchRoomService.isTimeToNavigate = false
                navigateToMatchRoom()
            }
            else -> Unit
        }
        when (MatchRoomService.hasBeenKickedOut) {
            true -> {
                MatchRoomService.hasBeenKickedOut = false
                navigateToHome()
            }
            else -> Unit
        }
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
            ChatComponent(modifier = Modifier.fillMaxWidth(), authViewModel = authViewModel)
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
                        timeService = TimeService
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
                            Switch(checked = isLocked, onCheckedChange = { toggleLock() })
                        }
                        Button(
                            onClick = { startMatch() },
                            enabled = isLocked && players.isNotEmpty()
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
                            if (isHost() && player.username != HOST_USERNAME) {
                                Button(onClick = { banPlayerUsername(player.username) }) {
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

