package com.example.polyquiz.pages.presentation

import android.annotation.SuppressLint
import android.service.autofill.FieldClassification.Match
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.chat.presentation.ChatComponent
import com.example.polyquiz.constants.MatchButtonActions
import com.example.polyquiz.constants.MatchContext
import com.example.polyquiz.constants.PresetAvatar
import com.example.polyquiz.constants.StartMatchFeedback
import com.example.polyquiz.core.storage.ImageStorage
import com.example.polyquiz.match.domain.Game
import com.example.polyquiz.match.domain.MatchContextService
import com.example.polyquiz.match.domain.MatchRoomService
import com.example.polyquiz.match.domain.MatchRoomService.gameTitle
import com.example.polyquiz.match.domain.MatchRoomService.isLocked
import com.example.polyquiz.match.domain.MatchRoomService.players
import com.example.polyquiz.match.domain.MatchService
import com.example.polyquiz.match.domain.MatchService.matchRoomService
import com.example.polyquiz.match.domain.Player
import com.example.polyquiz.match.domain.TimeService
import com.example.polyquiz.match.presentation.TimerComponent
import com.example.polyquiz.ui.theme.Theme

@SuppressLint("UnrememberedMutableState")
@Composable

fun WaitPage(
    modifier: Modifier,
    navigateToHome: () -> Unit,
    authViewModel: AuthViewModel,
    navigateToMatchRoom: () -> Unit
) {
    // var isLocked: Boolean = false
    val matchService = MatchService
    val timeService = TimeService
    // var isLocked by mutableStateOf(false)

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current


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
//        println(MatchRoomService.hostId)
//        println( MatchRoomService.hostId == authViewModel.getUserId())
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
            Log.d("WaitPage", "IsHost")
        } else {
            MatchContextService.setContext(MatchContext.PLAYERVIEW)
            Log.d("WaitPage", "IsPlayer")
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

    val banPlayerUsername: (String) -> Unit = { userId ->
        if (userId != matchRoomService.hostId) {
            MatchRoomService.banUsername(userId)
        }
    }

    fun startMatch() {
        if(matchRoomService.isCheaterMode){
            matchRoomService.startMatchCheaterMode();
        }
        else {
        matchRoomService.startMatch()
        }
    }

    fun quitMatch() {
        MatchRoomService.disconnectFromRoom()
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            }
    ) {
        ChatComponent(modifier = Modifier, authViewModel = authViewModel)

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

                    Text(text = "Code d'accès: ${MatchRoomService.getRoomCode()}")
                    if (isHost()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(StartMatchFeedback.LOCK_MATCH.value)
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = matchRoomService.isLocked,
                                onCheckedChange = { toggleLock() })
                        }
                        val disabled  = !matchRoomService.isLocked || players.isEmpty() ||  (matchRoomService.partyConfig.isEntryFeeRequired && matchRoomService.players.size <= 1)
                        Button(
                            onClick = {
                                startMatch()
                            },
                            enabled = !disabled

                        ) {
                            Text(MatchButtonActions.START_MATCH.value)
                        }
                    }
                    Button(
                        onClick = { quitMatch() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text(MatchButtonActions.LEAVE_MATCH.value)
                    }
                    players.forEach { player ->
                        PlayerCard(player, (isHost() && player.id != matchRoomService.hostId), banPlayerUsername)
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerCard(player: Player, isHost: Boolean, onClick: (String) -> Unit) {
    val playerAvatarRef = ImageStorage.getAvatarRef(player.id)
    var playerAvatarUrL = ""
    ImageStorage.getImageURL(playerAvatarRef) { url ->
        if (url != null) {
            playerAvatarUrL = url
        }
    }
    var painter: AsyncImagePainter
    Card {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            painter = if (playerAvatarUrL.isEmpty()) {
                rememberAsyncImagePainter(model = PresetAvatar.DEFAULT.value)
            } else {
                rememberAsyncImagePainter(model = playerAvatarUrL)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = painter,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = player.username)

            if (isHost) {
                Button(onClick = { onClick(player.id) }) {
                    Text(MatchButtonActions.BAN_PLAYER.value)
                }
            }
        }
    }
}



