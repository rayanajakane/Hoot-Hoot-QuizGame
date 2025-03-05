package com.example.polyquiz.pages.presentation

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.example.polyquiz.Choice
import com.example.polyquiz.Game
import com.example.polyquiz.GameList
import com.example.polyquiz.GameService
import com.example.polyquiz.Question
import com.example.polyquiz.SnackbarController
import com.example.polyquiz.SnackbarEvent
import com.example.polyquiz.auth.domain.AuthState
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.chat.presentation.ChatComponent
import com.example.polyquiz.constants.AuthFeedbackText
import com.example.polyquiz.constants.DisplayAuthenticationText
import com.example.polyquiz.http.QuestionService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale


@Composable
fun MatchCreationPage(modifier: Modifier, navigateToLogin: () -> Unit, navigateToHome: () -> Unit, authViewModel: AuthViewModel) {
    val authState = authViewModel.authState.observeAsState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val questionService = QuestionService()
    val gameService = GameService()
    var games by remember { mutableStateOf<List<Game>>(emptyList()) }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }


    fun fetchGames() {
        gameService.getGames(
            onSuccess = { games ->
                val gson = Gson()
                val json = gson.toJson(games)
                val listType = object : TypeToken<List<Game>>() {}.type
                val result: List<Game> = gson.fromJson(json, listType)

                result.forEach { game ->
                    println("Game: ${game.title}")
                }

            },
            onError = { errorMessage -> println("Error: $errorMessage") }
        )
    }

    fun loadGames(service: GameService, onLoad: (List<Game>) -> Unit) {
        service.getGames(
            onSuccess = { onLoad(it) },
            onError = { println("Erreur de chargement: $it") }
        )
    }

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> {
                scope.launch {
                    SnackbarController.sendEvent(
                        event = SnackbarEvent(
                            message = AuthFeedbackText.SIGN_OUT.value,
                        )
                    )
                }
                navigateToLogin()
            }

            is AuthState.Error -> {
                scope.launch {
                    SnackbarController.sendEvent(
                        event = SnackbarEvent(
                            message = (authState.value as AuthState.Error).message,
                        )
                    )
                }
            }

            else -> Unit
        }
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
        ChatComponent(modifier = modifier.weight(1f), authViewModel = authViewModel)


        Column(
           horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 10.dp)
        ) {
            Button(
                onClick = {
                    fetchGames()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(text = "Jouer")
            }

            Surface(
                shadowElevation = 10.dp,
                tonalElevation = 10.dp,
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp),
                modifier = Modifier.padding(10.dp)
            ) {
                Button(
                    onClick = { navigateToHome() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceBright,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                ) {
                    Text(text = "Retourner à la page d'accueil")
                }
            }


            ElevatedButton(
                onClick = { authViewModel.signOut() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceBright,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(text = DisplayAuthenticationText.LOGOUT.value)
            }
            GameList(modifier = modifier.weight(1f).fillMaxHeight(0.2f))
        }

    }

}
