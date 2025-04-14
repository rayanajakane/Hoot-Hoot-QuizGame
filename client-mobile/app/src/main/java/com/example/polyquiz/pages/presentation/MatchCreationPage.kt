package com.example.polyquiz.pages.presentation

import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polyquiz.R
import com.example.polyquiz.SnackbarController
import com.example.polyquiz.SnackbarEvent
import com.example.polyquiz.auth.domain.AuthState
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.chat.presentation.ChatComponent
import com.example.polyquiz.shop.domain.ShopViewModel
import com.example.polyquiz.shop.presentation.BalanceCard
import com.example.polyquiz.ui.MenuButton
import kotlinx.coroutines.launch


@Composable
fun MatchCreationPage(
    modifier: Modifier,
    authViewModel: AuthViewModel,
    shopViewModel: ShopViewModel,
    navigateToHome: () -> Unit,
    navigateToCreate: () -> Unit,
    navigateToUserEdit: () -> Unit,
    navigateToWaitPage: () -> Unit,
    navigateToFriendsPage: () -> Unit,
    navigateToJoinRoom: () -> Unit,
    navigateToLogin: () -> Unit,
    navigateToRankingsPage: () -> Unit,
    navigateToShopPage: () -> Unit
) {
    val currentBalance by shopViewModel.currentBalance.collectAsState()
    val authState = authViewModel.authState.observeAsState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(authState.value) {
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
            .imePadding()
            .statusBarsPadding()
    ) {
        ChatComponent(modifier = modifier, authViewModel = authViewModel)
        Box(
            modifier = Modifier
                .fillMaxSize().navigationBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 36.dp),
                        text = stringResource(R.string.host_match),
                        style = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    )
                    Column(
                        modifier = Modifier
                            .imePadding()
                            .statusBarsPadding()
                    ) {
                        MenuButton(
                            modifier = Modifier,
                            navigateToHome,
                            navigateToCreate,
                            navigateToUserEdit,
                            navigateToFriendsPage,
                            navigateToJoinRoom,
                            navigateToRankingsPage,
                            navigateToShopPage,
                            signOut = { authViewModel.signOut() }
                        )
                        BalanceCard(currentBalance)
                    }
                }
                GameList(
                    modifier = Modifier,
                    navigateToWaitPage,
                    authViewModel
                )
            }
        }
    }
}
