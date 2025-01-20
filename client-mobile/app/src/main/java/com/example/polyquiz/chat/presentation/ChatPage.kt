package com.example.polyquiz.chat.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.polyquiz.SnackbarController
import com.example.polyquiz.SnackbarEvent
import com.example.polyquiz.auth.domain.AuthState
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.constants.AuthFeedbackText
import com.example.polyquiz.constants.DisplayAuthenticationText
import kotlinx.coroutines.launch

@Composable
fun ChatPage(modifier: Modifier, navigateToLogin: () -> Unit, authViewModel: AuthViewModel) {
    val authState = authViewModel.authState.observeAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(authState.value) {
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
    }

    Row (
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxSize()
    ){
        ChatComponent(modifier = modifier, authViewModel = authViewModel)
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
