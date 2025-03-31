package com.example.polyquiz

import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polyquiz.auth.domain.AuthState
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.chat.presentation.ChatComponent
import com.example.polyquiz.constants.Route
import com.example.polyquiz.match.domain.MatchRoomService
import com.example.polyquiz.match.domain.MatchRoomService.navController
import kotlinx.coroutines.launch

@Composable
fun HomePage(
    modifier: Modifier,
    navigateToLogin: () -> Unit,
    navigateToHome: () -> Unit,
    navigateToCreate: () -> Unit,
    navigateToUserEdit: () -> Unit,
    navigateToWaitPage: () -> Unit,
    navigateToFriendsPage: () -> Unit,
    navigateToJoinRoom: () -> Unit,
    authViewModel: AuthViewModel,
) {
    val authState = authViewModel.authState.observeAsState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val shouldNavigate = rememberUpdatedState(MatchRoomService.timeToGoToWaitPage)
    val errorMessage by remember {
        derivedStateOf { MatchRoomService.errorMsg }
    }

    LaunchedEffect(authState.value, MatchRoomService.timeToGoToWaitPage) {
        when (authState.value) {
            is AuthState.Unauthenticated -> {
                scope.launch {
                    SnackbarController.sendEvent(
                        event = SnackbarEvent(
                            message = StringValue.StringResource(R.string.sign_out_feedback)
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
        when (shouldNavigate.value) {
            true -> {
                Log.d("Should navigate", "Navigating again")
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
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            ElevatedButton(
                onClick = {
                    authViewModel.signOut()
                },
                modifier = Modifier
                    .padding(20.dp)
                    .align(Alignment.TopEnd),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceBright,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(3.dp)
            ) {
                Text(text = stringResource(R.string.logout_action))
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxHeight()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.flutter_dash_24px),
                    contentDescription = stringResource(R.string.hoot),
                    modifier = Modifier.size(128.dp)
                )
                Text(
                    stringResource(R.string.hoot),
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
                Row {
                    Column {
                        Button(
                            onClick = { navigateToJoinRoom() },
                            shape = RoundedCornerShape(3.dp),
                            modifier = Modifier
                                .height(55.dp)
                                .width(164.dp)
                        ) {
                            Text(stringResource(R.string.join_match))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                navigateToCreate()
                            },
                            shape = RoundedCornerShape(3.dp),
                            modifier = Modifier
                                .height(55.dp)
                                .width(164.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),

                            ) {
                            Text(text = stringResource(R.string.host_match))
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Button(
                            onClick = {
                                navigateToUserEdit()
                            },
                            shape = RoundedCornerShape(3.dp),
                            modifier = Modifier
                                .height(55.dp)
                                .width(164.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(text = stringResource(R.string.edit_profile))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                navigateToFriendsPage()
                            },
                            shape = RoundedCornerShape(3.dp),
                            modifier = Modifier
                                .height(55.dp)
                                .width(164.dp),
                        ) {
                            Text(text = stringResource(R.string.friends))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.team_name), fontWeight = FontWeight.Bold)
            }
        }


    }
}
