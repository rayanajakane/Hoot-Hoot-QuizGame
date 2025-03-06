package com.example.polyquiz.auth.presentation
import android.graphics.fonts.FontStyle
import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polyquiz.SnackbarController
import com.example.polyquiz.SnackbarEvent
import com.example.polyquiz.auth.domain.AuthState
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.constants.AuthFeedbackText
import com.example.polyquiz.constants.DisplayAuthenticationText
import com.example.polyquiz.constants.SIZE_CONSTANTS
import kotlinx.coroutines.launch

@Composable
fun LoginPage(
    modifier: Modifier,
    navigateToSignup: () -> Unit,
    navigateToForgotPassword: () -> Unit,
    navigateToHome: () -> Unit,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }


    val authState = authViewModel.authState.observeAsState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Authenticated -> {
                scope.launch {
                    SnackbarController.sendEvent(
                        event = SnackbarEvent(
                            message = AuthFeedbackText.SIGN_IN.value,
                        )
                    )
                }
                navigateToHome()
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
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            },
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
                        top = 16.dp,
                        end = 128.dp,
                        bottom = 16.dp
                    )
                    .fillMaxWidth(0.5f)
                    .padding(60.dp)
            ) {
                Text(
                    text = DisplayAuthenticationText.LOGIN_TITLE.value,
                    fontSize = 35.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )


                TextField(
                    value = email,
                    onValueChange = { if (it.length <= SIZE_CONSTANTS.MAX_INPUT_LENGTH) email = it },
                    singleLine = true,
                    label = { Text(DisplayAuthenticationText.EMAIL.value) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = password,
                    onValueChange = { if (it.length <= SIZE_CONSTANTS.MAX_INPUT_LENGTH) password = it },
                    singleLine = true,
                    label = { Text(DisplayAuthenticationText.PASSWORD.value) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardActions = KeyboardActions(onDone = {
                        authViewModel.signIn(email, password)
                        keyboardController?.hide()
                    }),
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
                TextButton(
                    onClick = {
                        navigateToForgotPassword()
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(text = DisplayAuthenticationText.FORGOT_PASSWORD.value,
                    //    fontStyle = FontStyle.Italic
                    )
                }

                Button(
                    onClick =
                    {
                        authViewModel.signIn(email, password)
                        keyboardController?.hide()
                    },
                    enabled = authState.value != AuthState.Loading
                ) {
                    Text(DisplayAuthenticationText.LOGIN_ACTION.value)
                }

                ElevatedButton(
                    onClick =
                    {
                        navigateToSignup()
                        authViewModel.resetAuthState()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceBright,
                        contentColor = MaterialTheme.colorScheme.onSurface

                    )
                ) {
                    Text(DisplayAuthenticationText.SIGNUP_ACTION.value)
                }
            }
        }
    }
}
