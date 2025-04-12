package com.example.polyquiz.friends.presentation

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polyquiz.R
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.auth.domain.UserIdName
import com.example.polyquiz.chat.presentation.ChatComponent
import com.example.polyquiz.friends.domain.FriendsService
import com.example.polyquiz.shop.domain.ShopViewModel
import com.example.polyquiz.shop.presentation.BalanceCard
import com.example.polyquiz.ui.MenuButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun FriendsSearchScreen(
    navigateToHome: () -> Unit,
    navigateToCreate: () -> Unit,
    navigateToUserEdit: () -> Unit,
    navigateToFriendsPage: () -> Unit,
    navigateToJoinRoom: () -> Unit,
    authViewModel: AuthViewModel,
    shopViewModel: ShopViewModel,
    navigateToRankingsPage: () -> Unit,
    navigateToShopPage: () -> Unit,
    currentUserID: String,
) {
    val friendsService = remember { FriendsService() }
    var searchQuery by remember { mutableStateOf("") }
    val currentBalance by shopViewModel.currentBalance.collectAsState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(currentUserID) {
        friendsService.initialize(currentUserID)
        friendsService.returnAllData()
        shopViewModel.getCurrentBalance(currentUserID)
    }

    val pendingRequests by friendsService.pendingRequests.collectAsState()
    val sentRequests by friendsService.sentRequests.collectAsState()
    val friends by friendsService.friends.collectAsState()
    val allUsers by friendsService.allUsers.collectAsState()

    val searchResults by remember(searchQuery, allUsers) {
        derivedStateOf {
            val query = searchQuery.trim().lowercase()
            if (query.isEmpty()) allUsers else allUsers.filter {
                it.name.lowercase().contains(query)
            }
        }
    }

    var showDonationDialog by remember { mutableStateOf(false) }
    var selectedFriendId by remember { mutableStateOf("") }
    var donationAmount by remember { mutableStateOf("") }

    if (showDonationDialog) {
        AlertDialog(
            onDismissRequest = { showDonationDialog = false },
            title = { Text(text = "Enter Donation Amount") },
            text = {
                OutlinedTextField(
                    value = donationAmount,
                    onValueChange = { donationAmount = it },
                    label = { Text(text = "Amount") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    val amountInt = donationAmount.toIntOrNull() ?: 0
                    if (amountInt > 0) {
                        shopViewModel.donateMoney(currentUserID, selectedFriendId, amountInt)
                    }
                    showDonationDialog = false
                    donationAmount = ""
                }) {
                    Text(text = "OK")
                }
            },
            dismissButton = {
                Button(onClick = { showDonationDialog = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
    DisposableEffect(friendsService) {
        onDispose {
            friendsService.stopReturningUsers()
            shopViewModel.stopListeningForMoneyEvents()
        }
    }
    Row(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            }
            .navigationBarsPadding()
            .statusBarsPadding()
    ) {
        ChatComponent(modifier = Modifier, authViewModel = authViewModel)

        if (pendingRequests.isEmpty() || sentRequests.isEmpty() || friends.isEmpty() || allUsers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            Text(
                stringResource(R.string.friends),
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
                    navigateToShopPage,
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
                LazyColumn {
                    if (pendingRequests.isNotEmpty()) {
                        item {
                            PendingRequestsCard(pendingRequests, scope, friendsService)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    if (sentRequests.isNotEmpty()) {
                        item {
                            SentRequestsCard(sentRequests, scope, friendsService)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    if (friends.isNotEmpty()) {
                        item {
                            ElevatedCard() {
                                Box {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = stringResource(R.string.my_friends),
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .padding(16.dp)
                                        )
                                        friends.forEach { friend ->
                                            FriendsListItem(
                                                user = friend,
                                                isFriend = true,
                                                isRequestPending = false,
                                                isRequestSent = false,
                                                isEligible = false,
                                                onSendRequest = { },
                                                onCancelRequest = { },
                                                onAcceptRequest = { },
                                                onRejectRequest = { },
                                                onRemoveFriend = { id ->
                                                    scope.launch {
                                                        friendsService.removeFriend(
                                                            id
                                                        )
                                                    }
                                                },
                                                onDonate = { friendId ->
                                                    selectedFriendId = friendId
                                                    showDonationDialog = true
                                                }
                                            )
                                        }
                                    }

                                }

                            }
                        }

                    }

                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(16.dp)
                                .fillParentMaxWidth()
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.search_friend),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text(text = stringResource(R.string.search)) },
                                modifier = Modifier.fillMaxWidth(0.60f)
                            )
                        }
                    }
                    if (searchResults.isNotEmpty()) {
                        item {
                            SearchResultsCard(
                                searchResults = searchResults,
                                friends = friends,
                                pendingRequests = pendingRequests,
                                sentRequests = sentRequests,
                                scope = scope,
                                friendsService = friendsService
                            )
                        }

                    }
                }


            }
        }
    }

}

@Composable
fun PendingRequestsCard(
    pendingRequests: List<UserIdName>,
    scope: CoroutineScope,
    friendsService: FriendsService
) {
    ElevatedCard() {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.friend_requests_received),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(16.dp)
            )
            pendingRequests.forEach { user ->
                FriendsListItem(
                    user = user,
                    isFriend = false,
                    isRequestPending = true,
                    isRequestSent = false,
                    isEligible = false,
                    onSendRequest = { },
                    onCancelRequest = { },
                    onAcceptRequest = { id ->
                        scope.launch {
                            friendsService.acceptFriendRequest(
                                id
                            )
                        }
                    },
                    onRejectRequest = { id ->
                        scope.launch {
                            friendsService.rejectFriendRequest(
                                id
                            )
                        }
                    },
                    onRemoveFriend = { },
                    onDonate = { }
                )
            }
        }
    }
}

@Composable
fun SentRequestsCard(
    sentRequests: List<UserIdName>,
    scope: CoroutineScope,
    friendsService: FriendsService
) {
    ElevatedCard() {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(R.string.friend_requests_sent),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(16.dp)
            )
            sentRequests.forEach { user ->
                FriendsListItem(
                    user = user,
                    isFriend = false,
                    isRequestPending = false,
                    isRequestSent = true,
                    isEligible = false,
                    onSendRequest = { },
                    onCancelRequest = { id ->
                        scope.launch {
                            friendsService.cancelRequest(
                                id
                            )
                        }
                    },
                    onAcceptRequest = { },
                    onRejectRequest = { },
                    onRemoveFriend = { },
                    onDonate = { }
                )
            }
        }

    }
}

@Composable
fun SearchResultsCard(
    searchResults: List<UserIdName>,
    friends: List<UserIdName>,
    pendingRequests: List<UserIdName>,
    sentRequests: List<UserIdName>,
    scope: CoroutineScope,
    friendsService: FriendsService
) {

    ElevatedCard() {
        Box() {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.search_results),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(16.dp)
                )
                searchResults.forEach { user ->
                    FriendsListItem(
                        user = user,
                        isFriend = friends.any { it.id == user.id },
                        isRequestPending = pendingRequests.any { it.id == user.id },
                        isRequestSent = sentRequests.any { it.id == user.id },
                        isEligible = !(friends.any { it.id == user.id } || pendingRequests.any { it.id == user.id } || sentRequests.any { it.id == user.id }),
                        onSendRequest = { id -> scope.launch { friendsService.sendFriendRequest(id) } },
                        onCancelRequest = { id -> scope.launch { friendsService.cancelRequest(id) } },
                        onAcceptRequest = { id ->
                            scope.launch {
                                friendsService.acceptFriendRequest(
                                    id
                                )
                            }
                        },
                        onRejectRequest = { id ->
                            scope.launch {
                                friendsService.rejectFriendRequest(
                                    id
                                )
                            }
                        },
                        onRemoveFriend = { id -> scope.launch { friendsService.removeFriend(id) } },
                        onDonate = { }
                    )
                }
            }
        }

    }
}
