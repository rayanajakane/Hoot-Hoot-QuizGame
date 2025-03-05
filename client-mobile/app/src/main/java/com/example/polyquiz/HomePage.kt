package com.example.polyquiz

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.example.polyquiz.auth.domain.AuthState
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.chat.presentation.ChatComponent
import com.example.polyquiz.constants.AuthFeedbackText
import com.example.polyquiz.constants.DisplayAuthenticationText
import com.example.polyquiz.match.domain.MatchRoomService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.jvm.internal.Intrinsics.Kotlin

@Composable
fun HomePage(
    modifier: Modifier,
    navigateToLogin: () -> Unit,
    navigateToHome: () -> Unit,
    navigateToCreate: () -> Unit,
    navigateToWaitPage: () -> Unit,
    authViewModel: AuthViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var showDialog by remember { mutableStateOf(false) }
    val shouldNavigate = rememberUpdatedState(MatchRoomService.timeToGoToWaitPage)

    LaunchedEffect(authState.value, MatchRoomService.timeToGoToWaitPage) {
        when(authState.value) {
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
        when(shouldNavigate.value) {
            true -> {
                MatchRoomService.timeToGoToWaitPage = false
                navigateToWaitPage()
            }
            else -> Unit
        }
    }


    Row (
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxSize()
            .pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus()
                keyboardController?.hide()
            })
        }
    ){
        ChatComponent(modifier = modifier, authViewModel = authViewModel)
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
//            Button(
//                onClick = {joinGameDialog()},
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = MaterialTheme.colorScheme.primary,
//                    contentColor = MaterialTheme.colorScheme.onPrimary)
//            ) {
//                Text(text = "Joindre une partie")
//            }
            Button(onClick = { showDialog = true }) {
                Text("Joindre une partie")
            }

            JoinGameDialog(
                isOpen = showDialog,
                onDismiss = { showDialog = false },
                onJoin = {
                    showDialog = false
                },
                authViewModel = authViewModel,
                navigateToHome = navigateToHome,
                navigateToMatchPage = navigateToWaitPage,
                navigateToWaitPage = navigateToWaitPage
            )
            Button(
                onClick = {
                    println("Create")
                    navigateToCreate()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary)) {
                Text(text = "Créer une partie")
            }
            Surface(
                shadowElevation = 10.dp,
                tonalElevation = 10.dp,
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp),
                modifier = Modifier.padding(10.dp)
            ){
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceBright,
                        contentColor = MaterialTheme.colorScheme.onSurface),
                ) {
                    Text(text = "Administrer les jeux")
                }
            }
        }
        ElevatedButton(
            onClick = {
                authViewModel.signOut()
            },
            modifier = Modifier.padding(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceBright,
                contentColor = MaterialTheme.colorScheme.onSurface)
        ) {
            Text(text = DisplayAuthenticationText.LOGOUT.value)
        }
    }
}
