package com.example.polyquiz.pages.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.constants.HOST_USERNAME
import com.example.polyquiz.constants.MatchContext
import com.example.polyquiz.match.domain.MatchContextService
import com.example.polyquiz.match.domain.MatchRoomService
import com.example.polyquiz.match.domain.MatchRoomService.gameTitle
import com.example.polyquiz.match.domain.MatchRoomService.players
import com.example.polyquiz.match.domain.TimeService
import com.example.polyquiz.match.presentation.TimerComponent

@Composable
fun WaitPage(modifier: Modifier, navigateToHome: () -> Unit, authViewModel: AuthViewModel) {
    var isLocked : Boolean = false
    var isHostPlaying: Boolean
    var isTimeToNavigate: Boolean = false

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

    LaunchedEffect(Unit) {
        resetWaitPage()
        TimeService.handleTimer() //pour l'organisateur, il faudrait un listenToTimerEvents()

        if (isHost()) {
            MatchRoomService.gameTitle =
                "CECI EST HARDCODÉ, À IMPLÉMENTER: MATCH SERVICE "
        } else {
                MatchContextService.setContext(MatchContext.PLAYERVIEW)
            }
        }
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
        MatchRoomService.disconnectFromRoom(navigateToHome)
    }

    fun getToGame() {

    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { quitMatch(); navigateToHome() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("QUITTER")
        }

        if (MatchRoomService.isMatchStarted) {
            getToGame()
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Le jeu $gameTitle commence dans..", style = MaterialTheme.typography.headlineMedium)
                TimerComponent(
                    modifier = Modifier.fillMaxWidth(),
                    timeService = TimeService
                )
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "La partie va bientôt commencer...", style = MaterialTheme.typography.headlineMedium)
                if (isHost()) {
                    Text(text = "Code d'accès: ${MatchRoomService.getRoomCode()}")
                    Switch(checked = isLocked, onCheckedChange = { toggleLock() })
                    Button(
                        onClick = {startMatch()},
                        enabled = isLocked && players.isNotEmpty()
                    ) {
                        Text("Commencer la partie")
                    }
                }
                players.forEach { player ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(player.username)
                        if (isHost() && player.username != "Organisateur") {
                            Button(onClick = { banPlayerUsername(player.username) }) {
                                Text("Bannir")
                            }
                        }
                    }
                }
            }
        }
    }

}

