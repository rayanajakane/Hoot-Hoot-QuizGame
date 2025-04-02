package com.example.polyquiz.auth.presentation

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixNormal
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.People
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.polyquiz.R
import com.example.polyquiz.SnackbarController
import com.example.polyquiz.SnackbarEvent
import com.example.polyquiz.auth.domain.AuthState
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.auth.domain.UsernameSuggestionService
import com.example.polyquiz.constants.PresetAvatar
import com.example.polyquiz.constants.SIZE_CONSTANTS
import com.example.polyquiz.ui.features.camera.CameraViewModel
import kotlinx.coroutines.launch

@Composable
fun SignupPage(
    modifier: Modifier,
    navigateToChat: () -> Unit,
    navigateToLogin: () -> Unit,
    navigateToCamera: () -> Unit,
    authViewModel: AuthViewModel,
    cameraViewModel: CameraViewModel
) {
    val context = LocalContext.current
    val email by authViewModel.email.collectAsState()
//    var username by remember { mutableStateOf(authViewModel.getUsername()) }
    val username by authViewModel.username.collectAsState()
    val password by authViewModel.password.collectAsState()

    val emailError by authViewModel.emailError.collectAsState()
    val usernameError by authViewModel.usernameError.collectAsState()
    val passwordError by authViewModel.passwordError.collectAsState()

    var passwordVisible by remember { mutableStateOf(false) }

    val authState = authViewModel.authState.observeAsState()
    val scope = rememberCoroutineScope()

    val keyboardController = LocalSoftwareKeyboardController.current
    val avatarURL by authViewModel.avatarURL.collectAsState()
    val temporaryAvatar by cameraViewModel.temporaryAvatar.collectAsState()
    val avatarToShow =  temporaryAvatar ?: avatarURL
    val onClickAvatar: (String) -> Unit = { url ->
        cameraViewModel.setPresetAvatar(authViewModel, url)
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraViewModel.resetCapturedPhotoState()
        }
    }

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> {
                scope.launch {
                    SnackbarController.sendEvent(
                        event = SnackbarEvent(
                            message = StringValue.StringResource(R.string.sign_up_feedback)
                        )
                    )
                }
                navigateToChat()
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
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .imePadding()

    ) {
        ElevatedCard(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .padding(
                        start = 32.dp,
                        top = 8.dp,
                        end = 32.dp,
                        bottom = 8.dp
                    )
                    .fillMaxWidth(0.5f)
            ) {
                Text(
                    text = stringResource(R.string.signup_title),
                    fontSize = 35.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // TODO: Select avatar
                        if (avatarToShow is Bitmap) {
                            Log.d("Signup page", "Set avatar to show as Bitmap")
                            TemporaryAvatar(128.dp, avatarToShow)
                        } else {
                            if (avatarURL.isNotEmpty()) {
                                AvatarPlaceholder(128.dp, avatarURL)
                            } else {
                                AvatarPlaceholder(128.dp, PresetAvatar.DEFAULT.value)
                            }
                        }
                        Button(
                            onClick =
                            {
                                navigateToCamera()
                            },
                        ) { Text(stringResource(R.string.upload_avatar)) }
                        Text(stringResource(R.string.preset_avatars))
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            ClickableAvatarPlaceholder(32.dp, PresetAvatar.A.value, onClickAvatar)
                            ClickableAvatarPlaceholder(32.dp, PresetAvatar.B.value, onClickAvatar)
                            ClickableAvatarPlaceholder(32.dp, PresetAvatar.C.value, onClickAvatar)
                            ClickableAvatarPlaceholder(32.dp, PresetAvatar.D.value, onClickAvatar)
                            ClickableAvatarPlaceholder(32.dp, PresetAvatar.DEFAULT.value, onClickAvatar)
                        }
                    }
                    Column() {
                        TextField(
                            value = email,
                            onValueChange = {
                                if (it.length <= SIZE_CONSTANTS.MAX_INPUT_LENGTH) authViewModel.updateEmail(
                                    it,
                                    context
                                )
                            },
                            isError = emailError.isNotEmpty(),
                            singleLine = true,
                            label = { Text(stringResource(R.string.email)) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (emailError.isNotEmpty()) {
                            Text(text = emailError, color = Color.Red)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = username,
                            onValueChange = {
                                if (it.length <= SIZE_CONSTANTS.MAX_INPUT_LENGTH) authViewModel.setAndUpdateUsername(
                                    it,
                                    context,
                                )
                            },
                            isError = usernameError.isNotEmpty(),
                            singleLine = true,
                            label = { Text(stringResource(R.string.username)) },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                if (UsernameSuggestionService.showUsernameDialog) {
                                    UsernameSuggestionDialog(
                                        onDismiss = {
                                            UsernameSuggestionService.showUsernameDialog = false
                                        },
                                        onUsernameSelected = { selectedUsername ->
                                            authViewModel.setAndUpdateUsername(
                                                selectedUsername,
                                                context,
                                            )
                                            UsernameSuggestionService.showUsernameDialog = false
                                        }
                                    )
                                }
                                IconButton(
                                    onClick = { UsernameSuggestionService.showUsernameDialog = !UsernameSuggestionService.showUsernameDialog},

                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                )
                                {
                                    Icon(
                                        imageVector = Icons.Default.AutoFixNormal,
                                        contentDescription = null
                                    )
                                }

                            }

                        )

                        if (usernameError.isNotEmpty()) {
                            Text(text = usernameError, color = Color.Red)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextField(
                            value = password,
                            onValueChange = {
                                if (it.length <= SIZE_CONSTANTS.MAX_INPUT_LENGTH) authViewModel.updatePassword(
                                    it,
                                    context
                                )
                            },
                            singleLine = true,
                            keyboardActions = KeyboardActions(onDone = {
                                authViewModel.signUp(email, username, password, context, avatarToShow)
                                keyboardController?.hide()
                            }),
                            label = { Text(stringResource(R.string.password)) },
                            isError = passwordError.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                val image = if (passwordVisible)
                                    Icons.Filled.Visibility
                                else Icons.Filled.VisibilityOff

                                val description =
                                    if (passwordVisible) "Hide password" else "Show password"

                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = image, description)
                                }
                            },
                        )

                        if (passwordError.isNotEmpty()) {
                            Text(text = passwordError, color = Color.Red)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                Row() {
                    ElevatedButton(
                        onClick =
                        {
                            navigateToLogin()
                            authViewModel.resetSignUpFields()
                            authViewModel.resetAuthState()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceBright,
                            contentColor = MaterialTheme.colorScheme.onSurface

                        )
                    ) {
                        Text(stringResource(R.string.return_to_login))
                    }
                    Button(
                        onClick =
                        {
                            authViewModel.signUp(email, username, password, context, avatarToShow)
                            keyboardController?.hide()
                        },
                        enabled = authState.value != AuthState.Loading
                    ) {
                        Text(stringResource(R.string.signup_action))
                    }
                }
            }
        }
    }
}



@Composable()
fun AvatarPlaceholder(avatarSize: Dp, imageUrl: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(avatarSize)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
    ) {
        AsyncImage(
            model = imageUrl,
            contentScale = ContentScale.Crop,
            contentDescription = stringResource(R.string.preset_avatars),
            modifier = Modifier.clip(CircleShape)
        )
    }
}
