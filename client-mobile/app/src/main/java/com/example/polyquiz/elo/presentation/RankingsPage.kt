package com.example.polyquiz.elo.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.polyquiz.R
import com.example.polyquiz.SnackbarController
import com.example.polyquiz.SnackbarEvent
import com.example.polyquiz.auth.domain.AuthState
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.chat.presentation.ChatComponent
import com.example.polyquiz.elo.domain.EloService
import com.example.polyquiz.shop.domain.ShopViewModel
import com.example.polyquiz.shop.presentation.BalanceCard
import com.example.polyquiz.ui.MenuButton
import kotlinx.coroutines.launch

@Composable
fun RankingsPage(
    modifier: Modifier,
    navigateToHome: () -> Unit,
    authViewModel: AuthViewModel,
    shopViewModel: ShopViewModel,
    navigateToCreate: () -> Unit,
    navigateToUserEdit: () -> Unit,
    navigateToFriendsPage: () -> Unit,
    navigateToJoinRoom: () -> Unit,
    navigateToLogin: () -> Unit,
    navigateToRankingsPage: () -> Unit,
    navigateToShop: () -> Unit
) {
    val rankings by EloService._rankings.observeAsState(emptyList())
    val currentRating by EloService.currentRating.observeAsState(0)
    val currentBalance by shopViewModel.currentBalance.collectAsState()

    val authState = authViewModel.authState.observeAsState()
    val scope = rememberCoroutineScope()
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

    LaunchedEffect(Unit) {
        EloService.listenForEloEvents()
        EloService.getElo(authViewModel.getUserId())
        EloService.getRankings()
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .statusBarsPadding()
    ) {
        ChatComponent(modifier = modifier, authViewModel = authViewModel)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            Text(
                stringResource(R.string.rankings),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.TopCenter)
            )
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
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
                    navigateToShop,
                    signOut = { authViewModel.signOut() }
                )
                BalanceCard(currentBalance)
            }
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(top = 100.dp)
            ) {
                if (rankings.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    return
                }

                Text(text = stringResource(R.string.your_elo) + " : " + currentRating)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.rank), modifier = Modifier.weight(1f))
                    Text(text = stringResource(R.string.username), modifier = Modifier.weight(2f))
                    Text(text = stringResource(R.string.elo), modifier = Modifier.weight(1f))
                }

                HorizontalDivider()

                LazyColumn {
                    itemsIndexed(rankings) { index, ranking ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "${index + 1}", modifier = Modifier.weight(1f))
                            Text(text = ranking.username, modifier = Modifier.weight(2f))
                            Text(text = ranking.score.toString(), modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
