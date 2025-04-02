package com.example.polyquiz.match.presentation

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polyquiz.R
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.chat.presentation.ChatComponent
import com.example.polyquiz.match.domain.MatchContextService
import com.example.polyquiz.match.domain.MatchRoomService
import com.example.polyquiz.match.domain.Player

@Composable
fun ResultsPage(
    authViewModel: AuthViewModel,
    matchRoomService: MatchRoomService,
    matchContextService: MatchContextService,
    navigateToHome: () -> Unit,
    players: List<Player>,
    modifier: Modifier = Modifier,
    extraContent: @Composable () -> Unit = {}
) {
    LaunchedEffect(MatchRoomService.isTimeToNavigateToResults) {
        MatchRoomService.isResults = false
    }

    LaunchedEffect(MatchRoomService.hasBeenKickedOut) {
        if (MatchRoomService.hasBeenKickedOut) {
            MatchRoomService.hasBeenKickedOut = false
            navigateToHome()
        }
    }

    val username = matchRoomService.retrieveUsername()

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var sortBy by remember { mutableStateOf("score") }
    var sortOrder by remember { mutableStateOf("descending") }

    val context by remember { mutableStateOf(matchContextService.getContext()) }

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
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.results),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(players) { player ->
                    PlayerCard(
                        Modifier.fillMaxWidth(0.8f),
                        player,
                        player.photoUrl,
                        context = matchContextService,
                        withAvatar = true
                    )
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
