package com.example.polyquiz.friends.presentation
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.polyquiz.auth.domain.UserIdName
import com.example.polyquiz.constants.FriendsDisplayText
import com.example.polyquiz.friends.domain.FriendsService
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsSearchScreen(
    currentUserID: String,
    navigateToHome: () -> Unit
) {
    val friendsService = remember { FriendsService() }
    var searchQuery by remember { mutableStateOf("") }

    // Initialize data
    LaunchedEffect(currentUserID) {
        friendsService.initialize(currentUserID)
        friendsService.onReturnUsers()
        friendsService.returnAllData()
    }

    // Collect state from the service
    val allUsers by friendsService.allUsers.collectAsState()
    val friends by friendsService.friends.collectAsState()
    val pendingRequests by friendsService.pendingRequests.collectAsState()
    val sentRequests by friendsService.sentRequests.collectAsState()

    // Filter search results based on query
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
        onDispose { friendsService.stopReturningUsers() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(FriendsDisplayText.SEARCH_FRIENDS.value) },
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
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(FriendsDisplayText.SEARCH_FRIENDS_PLACEHOLDER.value) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Show pending friend requests on top if there are any.
                if (pendingRequests.isNotEmpty()) {
                    Text(
                        text = "Friend Requests",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    LazyRow {
                        items(pendingRequests, key = { it.id }) { request ->
                            FriendRequestItem(
                                user = request,
                                onAccept = { id ->
                                    scope.launch { friendsService.acceptFriendRequest(id) }
                                },
                                onReject = { id ->
                                    scope.launch { friendsService.rejectFriendRequest(id) }
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

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
                                scope.launch { friendsService.sendFriendRequest(id) }
                            },
                            onCancelRequest = { id ->
                                scope.launch { friendsService.cancelRequest(id) }
                            },
                            onAcceptRequest = { id ->
                                scope.launch { friendsService.acceptFriendRequest(id) }
                            },
                            onRejectRequest = { id ->
                                scope.launch { friendsService.rejectFriendRequest(id) }
                            },
                            onRemoveFriend = { id ->
                                scope.launch { friendsService.removeFriend(id) }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    )
}

@Composable
fun FriendRequestItem(
    user: UserIdName,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .width(180.dp)
            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
            .padding(8.dp)
    ) {
        Text(text = user.name, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { onAccept(user.id) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = FriendsDisplayText.ACCEPT_REQUEST.value)
            }
            Button(
                onClick = { onReject(user.id) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = FriendsDisplayText.REJECT_REQUEST.value)
            }
        }
    }
}
