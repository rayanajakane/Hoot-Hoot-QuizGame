package com.example.polyquiz.auth.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polyquiz.R
import com.example.polyquiz.SnackbarController
import com.example.polyquiz.SnackbarEvent
import com.example.polyquiz.auth.domain.AuthState
import com.example.polyquiz.auth.domain.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordPage(
    modifier: Modifier,
    navigateToForgotPasswordFeedback: () -> Unit,
    navigateToLogin: () -> Unit,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }

    val authState = authViewModel.authState.observeAsState()
    val scope = rememberCoroutineScope()

    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.ResetPassword -> {
                navigateToForgotPasswordFeedback()
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(
                        start = 128.dp,
                        top = 32.dp,
                        end = 128.dp,
                        bottom = 32.dp
                    )
                    .fillMaxWidth(0.5f)
            ) {
                Text(
                    text = stringResource(R.string.reset_password),
                    fontSize = 35.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = stringResource(R.string.sent_email_extra_info),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = email,
                    onValueChange = { email = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.email)) },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick =
                    {
                        if (email.isNotEmpty()) {
                            keyboardController?.hide()
                            authViewModel.sendResetPasswordEmail(email);
                        }
                    },
                    enabled = authState.value != AuthState.Loading
                ) {
                    Text(stringResource(R.string.reset_password))
                }

                ElevatedButton(
                    onClick =
                    {
                        navigateToLogin()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceBright,
                        contentColor = MaterialTheme.colorScheme.onSurface

                    )
                ) {
                    Text(stringResource(R.string.return_to_login),)
                }
            }
        }
    }
}
