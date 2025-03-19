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
//    val friendsService = FriendsService()
    val friendsService = remember { FriendsService() }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(currentUserID) {
        friendsService.initialize(currentUserID)
        friendsService.onReturnUsers()
        friendsService.returnAllData()
    }

//    var allUsers by remember { mutableStateOf(listOf<UserIdName>()) }
//    var friends by remember { mutableStateOf(listOf<UserIdName>()) }
//    var pendingRequests by remember { mutableStateOf(listOf<UserIdName>()) }
//    var sentRequests by remember { mutableStateOf(listOf<UserIdName>()) }
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

//    val scope = rememberCoroutineScope()
//
//
//
//    fun reloadData() {
//        loadData(friendsService, currentUserID) { all, fr, pend, sent ->
//            allUsers = all
//            friends = fr
//            pendingRequests = pend
//            sentRequests = sent
//        }
//    }

    val scope = rememberCoroutineScope()

//    LaunchedEffect(friendsService) {
//        friendsService.onReturnUsers()
//    }

    DisposableEffect(friendsService) {
        onDispose {
            friendsService.stopReturningUsers() // Stop listening to socket events
        }
    }

//    LaunchedEffect(Unit) {
//        reloadData()
//    }
//
//    LaunchedEffect(Unit) {
//        friendsService.listenToAllFriendEvents { update, event ->
//            scope.launch {
//                reloadData()
//            }
//        }
//    }

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

//fun loadData(
//    friendsService: FriendsService,
//    currentUserID: String,
//    onDataLoaded: (allUsers: List<UserIdName>, friends: List<UserIdName>, pending: List<UserIdName>, sent: List<UserIdName>) -> Unit
//) {
//    friendsService.getAllUsers(currentUserID, { allUsers ->
//        friendsService.getFriendsList(currentUserID, { friends ->
//            friendsService.getPendingRequests(currentUserID, { pending ->
//                friendsService.getSentRequests(currentUserID, { sent ->
//                    onDataLoaded(allUsers, friends, pending, sent)
//                }, { error -> println("Error loading sent: $error") })
//            }, { error -> println("Error loading pending: $error") })
//        }, { error -> println("Error loading friends: $error") })
//    }, { error -> println("Error loading all users: $error") })
//}
