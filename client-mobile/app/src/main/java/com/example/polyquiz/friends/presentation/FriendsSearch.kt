package com.example.polyquiz.friends.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.polyquiz.auth.domain.UserIdName
import com.example.polyquiz.constants.FriendsDisplayText
import com.example.polyquiz.friends.domain.FriendsService
import com.example.polyquiz.money.domain.MoneyService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsSearchScreen(
    currentUserID: String,
    navigateToHome: () -> Unit
) {
    val friendsService = remember { FriendsService() }
    val moneyService = remember { MoneyService() }
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentUserID) {
        friendsService.initialize(currentUserID)
        friendsService.returnAllData()
        moneyService.getCurrentBalance(currentUserID)
        moneyService.listenForMoneyEvents()
    }

    val pendingRequests by friendsService.pendingRequests.collectAsState()
    val sentRequests by friendsService.sentRequests.collectAsState()
    val friends by friendsService.friends.collectAsState()
    val allUsers by friendsService.allUsers.collectAsState()

    val searchResults by remember(searchQuery, allUsers) {
        derivedStateOf {
            val query = searchQuery.trim().lowercase()
            if (query.isEmpty()) allUsers else allUsers.filter { it.name.lowercase().contains(query) }
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
                        moneyService.donateMoney(currentUserID, selectedFriendId, amountInt)
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
            moneyService.stopListeningForMoneyEvents()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = FriendsDisplayText.SEARCH_FRIENDS.value) },
                navigationIcon = {
                    IconButton(onClick = navigateToHome) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retourner à la page d'accueil"
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                val currentBalance by moneyService.currentBalance.collectAsState()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalanceWallet,
                        contentDescription = "Wallet",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Balance: $currentBalance",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(text = FriendsDisplayText.SEARCH_FRIENDS_PLACEHOLDER.value) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (pendingRequests.isNotEmpty()) {
                    Text(text = "WILL U BE MY AMIGO?")
                    LazyColumn {
                        items(pendingRequests, key = { it.id }) { user ->
                            FriendsListItem(
                                user = user,
                                isFriend = false,
                                isRequestPending = true,
                                isRequestSent = false,
                                isEligible = false,
                                onSendRequest = { },
                                onCancelRequest = { },
                                onAcceptRequest = { id -> scope.launch { friendsService.acceptFriendRequest(id) } },
                                onRejectRequest = { id -> scope.launch { friendsService.rejectFriendRequest(id) } },
                                onRemoveFriend = { },
                                onDonate = { }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (sentRequests.isNotEmpty()) {
                    Text(text = "SENT REQUESTS")
                    LazyColumn {
                        items(sentRequests, key = { it.id }) { user ->
                            FriendsListItem(
                                user = user,
                                isFriend = false,
                                isRequestPending = false,
                                isRequestSent = true,
                                isEligible = false,
                                onSendRequest = { },
                                onCancelRequest = { id -> scope.launch { friendsService.cancelRequest(id) } },
                                onAcceptRequest = { },
                                onRejectRequest = { },
                                onRemoveFriend = { },
                                onDonate = { }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (friends.isNotEmpty()) {
                    Text(text = "AMIGOS")
                    LazyColumn {
                        items(friends, key = { it.id }) { user ->
                            FriendsListItem(
                                user = user,
                                isFriend = true,
                                isRequestPending = false,
                                isRequestSent = false,
                                isEligible = false,
                                onSendRequest = { },
                                onCancelRequest = { },
                                onAcceptRequest = { },
                                onRejectRequest = { },
                                onRemoveFriend = { id -> scope.launch { friendsService.removeFriend(id) } },
                                onDonate = { friendId ->
                                    selectedFriendId = friendId
                                    showDonationDialog = true
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (searchResults.isNotEmpty()) {
                    Text(text = FriendsDisplayText.SEARCH_FRIENDS_PLACEHOLDER.value)
                    LazyColumn {
                        items(searchResults, key = { it.id }) { user ->
                            FriendsListItem(
                                user = user,
                                isFriend = friends.any { it.id == user.id },
                                isRequestPending = pendingRequests.any { it.id == user.id },
                                isRequestSent = sentRequests.any { it.id == user.id },
                                isEligible = !(friends.any { it.id == user.id } || pendingRequests.any { it.id == user.id } || sentRequests.any { it.id == user.id }),
                                onSendRequest = { id -> scope.launch { friendsService.sendFriendRequest(id) } },
                                onCancelRequest = { id -> scope.launch { friendsService.cancelRequest(id) } },
                                onAcceptRequest = { id -> scope.launch { friendsService.acceptFriendRequest(id) } },
                                onRejectRequest = { id -> scope.launch { friendsService.rejectFriendRequest(id) } },
                                onRemoveFriend = { id -> scope.launch { friendsService.removeFriend(id) } },
                                onDonate = { }
                            )
                        }
                    }
                } else if (searchQuery.isNotEmpty()) {
                    Text(text = "NO AMIGOS for '$searchQuery'")
                }
            }
        }
    )
}
