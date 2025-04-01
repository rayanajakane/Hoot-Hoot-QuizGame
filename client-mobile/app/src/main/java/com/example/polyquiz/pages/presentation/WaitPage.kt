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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.example.polyquiz.R
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
    val matchService = MatchService
    val timeService = TimeService
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

    val onToggleLock: () -> Unit = {
        MatchRoomService.toggleLock()
    }

    val banPlayerUsername: (String) -> Unit = { userId ->
        if (userId != matchRoomService.hostId) {
            MatchRoomService.banUsername(userId)
        }
    }

    fun startMatch() {
        MatchRoomService.startMatch()
    }

    fun quitMatch() {
        MatchRoomService.disconnectFromRoom()
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
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (MatchRoomService.isMatchStarted) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.start_soon, gameTitle),
                        style = MaterialTheme.typography.headlineMedium,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TimerComponent(
                        modifier = Modifier.fillMaxWidth(),
                        timeService = timeService,
                        size = 200.dp,
                        fontSize = 36.sp,
                        stroke = 25.dp
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.waiting_to_start),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(R.string.access_code), fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = MatchRoomService.getRoomCode(),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (isHost()) {
                        LockMatchToggle(onToggleLock)
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
                    ElevatedButton(
                        onClick = { quitMatch() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceBright,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(3.dp),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = stringResource(R.string.leave)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.leave))
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    players.forEach { player ->
                        PlayerCard(
                            player,
                            (isHost() && player.id != matchRoomService.hostId),
                            banPlayerUsername
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LockMatchToggle(onToggleLock: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isLocked) {
            Text(stringResource(R.string.locked_match))
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isLocked,
                onCheckedChange = { onToggleLock() })
        } else {
            Text(stringResource(R.string.unlocked_match))
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isLocked,
                onCheckedChange = { onToggleLock() })

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
    ElevatedCard(colors = CardColors(
        containerColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = MaterialTheme.colorScheme.background,
        disabledContentColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onSurface,
    )) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            painter = if (playerAvatarUrL.isEmpty()) {
                rememberAsyncImagePainter(model = PresetAvatar.DEFAULT.value)
            } else {
                rememberAsyncImagePainter(model = playerAvatarUrL)
            }
            Image(
                painter = painter,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = player.username)

                if (isHost) {
                    IconButton(onClick = { onClick(player.id) }) {
                        Icon(
                            Icons.Filled.Delete,
                            tint = MaterialTheme.colorScheme.error,
                            contentDescription = stringResource(R.string.ban_player)
                        )
                    }
                }
            }
        }
    }
}



