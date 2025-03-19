package com.example.polyquiz.friends.domain

import com.example.polyquiz.auth.domain.UserIdName
import com.example.polyquiz.constants.ChoiceInfo
import com.example.vanillaprototype.socket.SocketHandler
import com.example.polyquiz.constants.FriendsEvents
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject

class FriendsService {

    private val _allUsers = MutableStateFlow(listOf<UserIdName>())
    val allUsers: StateFlow<List<UserIdName>> get() = _allUsers

    private val _friends = MutableStateFlow(listOf<UserIdName>())
    val friends: StateFlow<List<UserIdName>> get() = _friends

    private val _pendingRequests = MutableStateFlow(listOf<UserIdName>())
    val pendingRequests: StateFlow<List<UserIdName>> get() = _pendingRequests

    private val _sentRequests = MutableStateFlow(listOf<UserIdName>())
    val sentRequests: StateFlow<List<UserIdName>> get() = _sentRequests

    private val _searchResults = MutableStateFlow<List<UserIdName>>(emptyList())
    val searchResults: StateFlow<List<UserIdName>> get() = _searchResults

    private val mSocket = SocketHandler.getSocket()
    private var userId: String = ""

    fun initialize(userId: String) {
        println("Initializing FriendsService with userId: $userId")
        this.userId = userId
    }

    fun returnAllData() {
        onReturnUsers();
        mSocket.emit(FriendsEvents.RETURN_ALL_DATA.value, userId);
    }

    fun onReturnUsers() {
        mSocket.on(FriendsEvents.RETURN_ALL_USERS.value) { args: Array<Any> ->
            if (args.isNotEmpty()) {
                val type = object : TypeToken<List<UserIdName>>() {}.type
                val data: List<UserIdName> = Gson().fromJson(args[0].toString(), type)
                _allUsers.value = data
                _searchResults.value = _allUsers.value
            }
        }
        mSocket.on(FriendsEvents.RETURN_ALL_FRIENDS.value) { args: Array<Any> ->
            if (args.isNotEmpty()) {
                val type = object : TypeToken<List<UserIdName>>() {}.type
                val data: List<UserIdName> = Gson().fromJson(args[0].toString(), type)
                _friends.value = data
            }
        }
        mSocket.on(FriendsEvents.RETURN_ALL_PENDING_REQUESTS.value) { args: Array<Any> ->
            if (args.isNotEmpty()) {
                val type = object : TypeToken<List<UserIdName>>() {}.type
                val data: List<UserIdName> = Gson().fromJson(args[0].toString(), type)
                _pendingRequests.value = data
            }
        }
        mSocket.on(FriendsEvents.RETURN_ALL_SENT_REQUESTS.value) { args: Array<Any> ->
            if (args.isNotEmpty()) {
                val type = object : TypeToken<List<UserIdName>>() {}.type
                val data: List<UserIdName> = Gson().fromJson(args[0].toString(), type)
                _sentRequests.value = data
            }
        }
    }

    fun stopReturningUsers() {
        mSocket.off(FriendsEvents.RETURN_ALL_USERS.value)
        mSocket.off(FriendsEvents.RETURN_ALL_FRIENDS.value)
        mSocket.off(FriendsEvents.RETURN_ALL_PENDING_REQUESTS.value)
        mSocket.off(FriendsEvents.RETURN_ALL_SENT_REQUESTS.value)
    }

    fun sendFriendRequest(toUserId: String) {
        val friendInfos = JSONObject(Gson().toJson(FriendsInfo(userId, toUserId)))
        mSocket.emit(FriendsEvents.REQUEST_SENT.value, friendInfos)
    }

    fun acceptFriendRequest(friendId: String) {
        val friendInfos = JSONObject(Gson().toJson(FriendsInfo(userId, friendId)))
        mSocket.emit(FriendsEvents.REQUEST_ACCEPTED.value, friendInfos)
    }

    fun rejectFriendRequest(friendId: String) {
        val friendInfos = JSONObject(Gson().toJson(FriendsInfo(userId, friendId)))
        mSocket.emit(FriendsEvents.REQUEST_REJECTED.value, friendInfos)
    }

    fun cancelRequest(friendId: String) {
        val friendInfos = JSONObject(Gson().toJson(FriendsInfo(userId, friendId)))
        mSocket.emit(FriendsEvents.REQUEST_CANCELED.value, friendInfos)
    }

    fun removeFriend(friendId: String) {
        val friendInfos = JSONObject(Gson().toJson(FriendsInfo(userId, friendId)))
        mSocket.emit(FriendsEvents.FRIEND_REMOVED.value, friendInfos)
    }

}
