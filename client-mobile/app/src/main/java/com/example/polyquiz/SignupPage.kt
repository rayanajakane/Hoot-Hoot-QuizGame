package com.example.polyquiz

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polyquiz.auth.AuthState
import com.example.polyquiz.auth.AuthViewModel
import com.example.polyquiz.constants.DisplayAuthenticationText

@Composable
fun SignupPage(
    modifier: Modifier,
    navigateToChat: () -> Unit,
    navigateToLogin: () -> Unit,
    authViewModel: AuthViewModel
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val authState = authViewModel.authState.observeAsState()
    val successSignUpMessage = "Connected successfully!"
    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Authenticated -> navigateToChat()
            is AuthState.Error -> Toast.makeText(context, (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT).show()
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
                    .fillMaxWidth(0.5f)
                    .padding(60.dp)
            ) {
                Text(
                    text = DisplayAuthenticationText.SIGNUP_TITLE.value,
                    fontSize = 35.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                TextField(
                    value = username,
                    onValueChange = { username = it },
                    singleLine = true,
                    label = { Text(DisplayAuthenticationText.USERNAME.value) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    singleLine = true,
                    keyboardActions = KeyboardActions(onDone = {
                        authViewModel.signUp(username, password)
                    }),
                    label = { Text(DisplayAuthenticationText.PASSWORD.value) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff

                        val description = if (passwordVisible) "Hide password" else "Show password"

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, description)
                        }
                    },
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {  authViewModel.signUp(username, password) },
                    enabled = authState.value != AuthState.Loading
                ) {
                    Text(DisplayAuthenticationText.SIGNUP_ACTION.value)
                }

                ElevatedButton(
                    onClick = { navigateToLogin() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceBright,
                        contentColor = MaterialTheme.colorScheme.onSurface

                    )
                ) {
                    Text(DisplayAuthenticationText.RETURN_TO_LOGIN.value)
                }
            }
        }
    }
}
