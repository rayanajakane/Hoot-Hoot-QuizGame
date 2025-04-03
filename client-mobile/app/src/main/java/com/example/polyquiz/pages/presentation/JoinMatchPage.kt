package com.example.polyquiz.pages.presentation

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.rounded.People
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polyquiz.R
import com.example.polyquiz.SnackbarController
import com.example.polyquiz.SnackbarEvent
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.chat.presentation.ChatComponent
import com.example.polyquiz.constants.MatchPageInfo
import com.example.polyquiz.match.domain.JoinMatchService
import com.example.polyquiz.match.domain.JoinMatchService.matchInfos
import com.example.polyquiz.match.domain.JoinMatchService.matchesInfos
import com.example.polyquiz.match.domain.MatchRoomService
import com.example.polyquiz.ui.MenuButton
import kotlinx.coroutines.launch

@SuppressLint("MutableCollectionMutableState")
@Composable
fun JoinMatchPage(
    modifier: Modifier,
    authViewModel: AuthViewModel,
    navigateToHome: () -> Unit,
    navigateToCreate: () -> Unit,
    navigateToUserEdit: () -> Unit,
    navigateToWaitPage: () -> Unit,
    navigateToFriendsPage: () -> Unit,
    navigateToJoinRoom: () -> Unit,
    navigateToMatchPage: () -> Unit,
    navigateToLogin: () -> Unit,
    navigateToRankingsPage: () -> Unit,
) {
    var room by remember { mutableStateOf("") }
    val username by remember { mutableStateOf(authViewModel.getUsername()) }
    val userId by remember { mutableStateOf(authViewModel.getUserId()) }
    val scope = rememberCoroutineScope()
    val shouldNavigate = rememberUpdatedState(MatchRoomService.timeToGoToWaitPage)
    val errorMessage by remember {
        derivedStateOf { MatchRoomService.errorMsg }
    }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val joinMatchService = JoinMatchService

    fun unlockedMatches(): List<MatchPageInfo> {
        return matchInfos.filter { !it.isLocked && !it.isPlaying }
    }

    fun lockedMatches(): List<MatchPageInfo> {
        return matchInfos.filter { it.isLocked && !it.isPlaying }
    }

    fun playingMatches(): List<MatchPageInfo> {
        return matchInfos.filter { it.isPlaying }
    }

    LaunchedEffect(Unit) {
        joinMatchService.getAllMatches()
    }

    LaunchedEffect(MatchRoomService.timeToGoToWaitPage) {
        when (shouldNavigate.value) {
            true -> {
                MatchRoomService.timeToGoToWaitPage = false
                navigateToWaitPage()
            }

            else -> Unit
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotEmpty()) {
            SnackbarController.sendEvent(
                event = SnackbarEvent(
                    message = StringValue.DynamicString(errorMessage),
                )
            )
            MatchRoomService.errorMsg = ""
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            joinMatchService.stopReturningAllMatches()
        }
    }

    fun submitCode(matchRoomCode: String) {
        JoinMatchService.matchRoomCode = "";
        JoinMatchService.validateMatchRoomCode(
            matchRoomCode,
            onSuccess = {
                JoinMatchService.matchRoomCode = matchRoomCode
                JoinMatchService.validateUsername(
                    username,
                    userId,
                    navigateToHome,
                    navigateToWaitPage,
                    navigateToMatchPage,

                    )
            },
            onError = { errorMessage ->
                println("Error: $errorMessage")
                scope.launch {
                    SnackbarController.sendEvent(
                        event = SnackbarEvent(
                            message = StringValue.DynamicString(errorMessage),
                        )
                    )
                }

                JoinMatchService.matchRoomCode = ""
            }
        )
    }

    fun joinRoom(code: String) {
        submitCode(code)
//        navigateToWaitPage()
    }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            }
    ) {
        ChatComponent(modifier = modifier, authViewModel = authViewModel)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            Column(modifier = modifier.verticalScroll(rememberScrollState())) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.join_match),
                        modifier = Modifier.padding(horizontal = 26.dp),
                        style = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    )
                    MenuButton(
                        modifier = Modifier,
                        navigateToHome,
                        navigateToCreate,
                        navigateToUserEdit,
                        navigateToFriendsPage,
                        navigateToJoinRoom,
                        navigateToRankingsPage,
                        signOut = {
                            authViewModel.signOut()
                            navigateToLogin()
                        }
                    )
                }


                Column(modifier = Modifier.padding(26.dp, 1.dp)) {
                    Row(horizontalArrangement = Arrangement.End) {
                        TextField(
                            value = room,
                            modifier = Modifier.padding(bottom = 30.dp),
                            onValueChange = { room = it },
                            label = { Text("Code") },
                            maxLines = 1,
                            keyboardActions = KeyboardActions(onDone = {
                                submitCode(room)
                            })
                        )
                        Button(
                            modifier = Modifier
                                .width(120.dp)
                                .height(55.dp),
                            onClick = { joinRoom(room) },
                            shape = RoundedCornerShape(3.dp)
                        ) {
                            Text(text = stringResource(R.string.join_action))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            enabled = false,
                            onClick = {
                                TODO()
                            }, shape = RoundedCornerShape(3.dp), modifier = Modifier.height(55.dp)
                        ) {
                            Text(text = stringResource(R.string.scan_qr))
                        }
                    }

                }

                Column(
                    modifier = Modifier.padding(26.dp, 0.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.pending_matches), style = TextStyle(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = stringResource(R.string.unlocked_matches),
                        style = TextStyle(
                            fontWeight = FontWeight.Bold
                        ),
                        fontSize = 24.sp,
                    )
                    if (matchInfos.isEmpty()) {
                        Text(text = stringResource(R.string.no_match))
                    }
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        unlockedMatches().forEach { match ->
                            MatchCard(match = match, onClick = { joinRoom(match.code) })
                        }
                    }
                    Spacer(modifier = Modifier.padding(5.dp))
                    Text(
                        text = stringResource(R.string.locked_matches),
                        style = TextStyle(
                            fontWeight = FontWeight.Bold
                        ),
                        fontSize = 24.sp,
                    )
                    if (lockedMatches().isNotEmpty()) {
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            lockedMatches().forEach { match ->
                                MatchCard(match = match, onClick = { joinRoom(match.code) })
                            }
                        }
                    } else {
                        Text(text = stringResource(R.string.no_match), fontStyle = FontStyle.Italic)
                    }
                    Column(modifier = Modifier.padding(2.dp, 20.dp)) {
                        Text(
                            text = stringResource(R.string.playing_matches), fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                        // TODO : Change if. Must show aucune partie even if one is unlocked but not en cours
                        if (matchInfos.isEmpty()) {
                            Text(
                                text = stringResource(R.string.no_match),
                                fontStyle = FontStyle.Italic
                            )
                        } else {
                            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                playingMatches().forEach { match ->
                                    MatchCard(match = match, onClick = {})
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun MatchCard(match: MatchPageInfo, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        shape = RectangleShape,
        modifier = Modifier
            .padding(5.dp)
            .width(140.dp)
            .height(220.dp)
            .shadow(4.dp, shape = RectangleShape)
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = match.gameTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.People,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = match.nPlayers.toString())
            }
            Column {
                if (match.partyConfig?.isFriendsOnly == true) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Group,
                            contentDescription = "Friends Only",
                            // TODO : Remove hardcoded color here
                            tint = Color(0xFF1976d2),

                            modifier = Modifier.requiredSize(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.friends),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                }
                if (match.partyConfig?.isEntryFeeRequired == true) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AttachMoney,
                            contentDescription = "Entry Fee",
                            // TODO : Remove hardcoded color
                            tint = Color(0xFF2e7d32),
                            modifier = Modifier.requiredSize(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = match.partyConfig.entryFeeAmount.toString(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            if (onClick != {} && !match.isLocked) {
                Button(
                    onClick = onClick,
                    modifier = Modifier.padding(top = 10.dp),
                    shape = RoundedCornerShape(3.dp)
                ) {
                    Text(text = stringResource(R.string.join_action))
                }
            }
        }
    }
}
