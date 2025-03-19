package com.example.polyquiz.friends.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.polyquiz.auth.domain.UserIdName
import com.example.polyquiz.constants.FriendsDisplayText
import com.example.polyquiz.friends.domain.FriendsService
import kotlinx.coroutines.launch

@Composable
fun FriendsSearchScreen(
    currentUserID: String
) {
    val friendsService = remember { FriendsService() }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(currentUserID) {
        friendsService.initialize(currentUserID)
        friendsService.onReturnUsers()
        friendsService.returnAllData()
    }

    val allUsers by friendsService.allUsers.collectAsState()
    val friends by friendsService.friends.collectAsState()
    val pendingRequests by friendsService.pendingRequests.collectAsState()
    val sentRequests by friendsService.sentRequests.collectAsState()

    val searchResults by remember(searchQuery, allUsers) {
        derivedStateOf {
            val query = searchQuery.trim().lowercase()
            if (query.isEmpty()) {
                allUsers
            } else {
                allUsers.filter { user ->
                    user.name.lowercase().contains(query)
                }
            }
        }
    }

    val scope = rememberCoroutineScope()

    DisposableEffect(friendsService) {
        onDispose {
            friendsService.stopReturningUsers() // Stop listening to socket events
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(FriendsDisplayText.SEARCH_FRIENDS.value, style = MaterialTheme.typography.headlineLarge)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text(FriendsDisplayText.SEARCH_FRIENDS_PLACEHOLDER.value) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(searchResults, key = { it.id }) { user ->
                val isFriend = friends.any { it.id == user.id }
                val isRequestPending = pendingRequests.any { it.id == user.id }
                val isRequestSent = sentRequests.any { it.id == user.id }
                val isEligible = !(isFriend || isRequestPending || isRequestSent)

                FriendsListItem(
                    user = user,
                    isFriend = isFriend,
                    isRequestPending = isRequestPending,
                    isRequestSent = isRequestSent,
                    isEligible = isEligible,

                    onSendRequest = { id ->
                        scope.launch {
                            friendsService.sendFriendRequest(id)
                        }
                    },
                    onCancelRequest = { id ->
                        scope.launch {
                            friendsService.cancelRequest(id)
                        }
                    },
                    onAcceptRequest = { id ->
                        scope.launch {
                            friendsService.acceptFriendRequest(id)
                        }
                    },
                    onRejectRequest = { id ->
                        scope.launch {
                            friendsService.rejectFriendRequest(id)
                        }
                    },
                    onRemoveFriend = { id ->
                        scope.launch {
                            friendsService.removeFriend(id)
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}
