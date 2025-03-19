package com.example.polyquiz.pages.presentation

import android.annotation.SuppressLint
import android.graphics.Paint.Join
import android.graphics.drawable.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.constants.MatchPageInfo
import com.example.polyquiz.match.domain.JoinMatchService
import com.example.polyquiz.match.domain.JoinMatchService.matchInfos
import com.example.polyquiz.match.domain.JoinMatchService.matchesInfos

@SuppressLint("MutableCollectionMutableState")
@Composable
fun JoinMatchPage(modifier: Modifier, authViewModel: AuthViewModel,navigateToHome: () -> Unit,
                  navigateToMatchPage: () -> Unit,
                  navigateToWaitPage: () -> Unit) {
    var room by remember { mutableStateOf("") }
    val username by remember { mutableStateOf(authViewModel.getUsername()) }

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
                    navigateToHome,
                    navigateToWaitPage,
                    navigateToMatchPage
                )
            },
            onError = { errorMessage ->
                println("Error: $errorMessage")
                JoinMatchService.matchRoomCode = ""
            }
        )
    }

    fun joinRoom(code: String) {
        submitCode(code)
        navigateToWaitPage()
    }
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
    Column() {
        Text(
            text = "Joindre une partie",
            style = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(onClick = { navigateToHome() }) {
                Text(text = "Page d'accueil")
            }

        }
    }

        Column(modifier = Modifier.padding(26.dp, 1.dp)) {
            TextField(
                value = room,
                onValueChange = { room = it },
                label = { Text("Code") },
                maxLines = 1,
                keyboardActions = KeyboardActions(onDone = {
                    submitCode(room)
                })
            )
            Button(
                modifier = Modifier.width(120.dp),
                onClick = { joinRoom(room) },
            ) {
                Text(text = "Joindre")
            }
        }

        Column(modifier = Modifier.padding(26.dp, 0.dp)) {
            Text(
                text = "Parties en attente", style = TextStyle(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            if (matchInfos.isEmpty()) {
                Text(text = "Aucune partie à afficher")
            }
            if (unlockedMatches().isEmpty()) {
                Text(text = "Parties Verrouillées")
            } else {
                Text(text = "Parties deverrouillées")
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    unlockedMatches().forEach { match ->
                        MatchCard(match = match, onClick = { joinRoom(match.code) })
                    }
                }
            }
            Spacer(modifier = Modifier.padding(5.dp))
            Text(
                text = "Parties Verrouillées", style = TextStyle(
                    fontWeight = FontWeight.Bold
                )
            )
            if (lockedMatches().isNotEmpty()) {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    lockedMatches().forEach { match ->
                        MatchCard(match = match, onClick = { joinRoom(match.code) })
                    }
                }
            } else {
                Text(text = "Aucune partie à afficher")
            }
            Column(modifier = Modifier.padding(2.dp, 20.dp)) {
                Text(
                    text = "Parties en cours", fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
                if (matchInfos.isEmpty()) {
                    Text(text = "Aucune partie à afficher")
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

@Composable
fun MatchCard(match: MatchPageInfo, onClick: () -> Unit = {}) {
    Spacer(modifier = Modifier.padding(5.dp))
    Card(modifier = Modifier.padding()
        .shadow(4.dp, shape = RectangleShape)
        .background(Color.White)
        , onClick = onClick, shape =RectangleShape) {
            Column(modifier = Modifier.padding(15.dp)) {
                Text(text = match.gameTitle)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.People,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = match.nPlayers.toString())
                }
                if (onClick != {}  ) {
                    Button(onClick = onClick, modifier = Modifier.padding(top = 10.dp)) {
                        Text(text = "Joindre")
                    }
                }
           }
    }
}


