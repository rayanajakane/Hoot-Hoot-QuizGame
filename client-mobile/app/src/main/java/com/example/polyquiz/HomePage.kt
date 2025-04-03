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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polyquiz.auth.domain.AuthState
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.chat.presentation.ChatComponent
import com.example.polyquiz.match.domain.MatchRoomService
import com.example.polyquiz.ui.MenuButton
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
    navigateToRankingsPage: () -> Unit,
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
                // TODO : Top and right padding
                .imePadding()
        ) {
            MenuButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .imePadding()
                    .statusBarsPadding(),
                navigateToHome,
                navigateToCreate,
                navigateToUserEdit,
                navigateToFriendsPage,
                navigateToJoinRoom,
                navigateToRankingsPage,
                signOut = { authViewModel.signOut() }
            )
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
                        ElevatedButton(
                            onClick = { navigateToJoinRoom() },
                            shape = RoundedCornerShape(3.dp),
                            modifier = Modifier
                                .height(55.dp)
                                .width(164.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                        ) {
                            Icon(
                                Icons.Filled.PlayCircleFilled,
                                contentDescription = stringResource(R.string.join_match)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.join_match))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        ElevatedButton(
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
                            Icon(
                                Icons.Filled.AddCircle,
                                contentDescription = stringResource(R.string.host_match)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(R.string.host_match))
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        ElevatedButton(
                            onClick = {
                                navigateToUserEdit()
                            },
                            shape = RoundedCornerShape(3.dp),
                            modifier = Modifier
                                .height(55.dp)
                                .width(164.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceBright,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = stringResource(R.string.my_profile)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(R.string.my_profile))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        ElevatedButton(
                            onClick = {
                                navigateToFriendsPage()
                            },
                            shape = RoundedCornerShape(3.dp),
                            modifier = Modifier
                                .height(55.dp)
                                .width(164.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceBright,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(
                                Icons.Filled.Group,
                                contentDescription = stringResource(R.string.friends)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(R.string.friends))
                        }
                    }
                    ElevatedButton(
                        onClick = {
                            navigateToRankingsPage()
                        },
                        shape = RoundedCornerShape(3.dp),
                        modifier = Modifier
                            .height(55.dp)
                            .width(164.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceBright,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            Icons.Filled.Group,
                            contentDescription = stringResource(R.string.rankings)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.rankings))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.team_name), fontWeight = FontWeight.Bold)
            }
        }
    }
}
