package com.example.polyquiz.friends.domain

import android.hardware.usb.UsbEndpoint
import com.example.polyquiz.http.CommunicationService
import com.example.polyquiz.auth.domain.UserIdName
import com.example.vanillaprototype.socket.SocketHandler
import com.example.polyquiz.constants.FriendsEvents
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.util.Log

val BASE_URL = "friends"
class FriendsService : CommunicationService(BASE_URL) {
    override val apiService: ApiService = retrofit.create(FriendsApiService::class.java)


    fun getAllUsers(
        currentUserId: String,
        onSuccess: (List<UserIdName>) -> Unit,
        onError: (String) -> Unit,
        endpoint: String = "all/$currentUserId"
    ) {
        getAll(
            { response ->val userList: List<UserIdName> = convertJsonResponseToType(response,object : TypeToken<List<UserIdName>>() {}.type)
                onSuccess(userList)
            },
            { error ->
                onError(error)
            },
            endpoint
        )
    }


    fun getFriendsList(currentUserId: String, onSuccess: (List<UserIdName>) -> Unit, onError: (String) -> Unit) {
        getAll(
            {response -> onSuccess(convertJsonResponseToType(response, object : TypeToken<List<UserIdName>>() {}.type))},
            onError, "list/$currentUserId")

    }

    fun getPendingRequests(currentUserId: String, onSuccess: (List<UserIdName>) -> Unit, onError: (String) -> Unit) {
        getAll(
            {response -> onSuccess(convertJsonResponseToType(response, object : TypeToken<List<UserIdName>>() {}.type))},
            onError, "requests/pending/$currentUserId")
    }

    fun getSentRequests(currentUserId: String, onSuccess: (List<UserIdName>) -> Unit, onError: (String) -> Unit) {
        getAll(
            {response -> onSuccess(convertJsonResponseToType(response, object : TypeToken<List<UserIdName>>() {}.type))},
            onError, "requests/sent/$currentUserId")
    }

    fun sendFriendRequest(fromUserId: String, toUserId: String, onResult: (Boolean, String?) -> Unit) {
        add(
            {},
            { _ -> onResult(true, null) },
            onError = { err ->
                if (err?.contains("EOFException") == true) {
                    onResult(true, null)
                } else {
                    onResult(false, err)
                }
            },
            endpoint = "send/$fromUserId/$toUserId"
        )
    }

    fun acceptFriendRequest(userId: String, friendId: String, onResult: (Boolean, String?) -> Unit) {
        add({}, { _ -> onResult(true, null) }, onError = { err -> onResult(false, err) }, endpoint = "accept/$userId/$friendId")
    }

    fun rejectFriendRequest(userId: String, friendId: String, onResult: (Boolean, String?) -> Unit) {
        add({}, { _ -> onResult(true, null) }, onError = { err -> onResult(false, err) }, endpoint = "reject/$userId/$friendId")
    }

    fun cancelRequest(userId: String, friendId: String, onResult: (Boolean, String?) -> Unit) {
        delete("$userId/$friendId", onSuccess = { onResult(true, null) }, onError = { err -> onResult(false, err) }, endpoint = "cancel")
    }

    fun removeFriend(userId: String, friendId: String, onResult: (Boolean, String?) -> Unit) {
        delete("$userId/$friendId", onSuccess = { onResult(true, null) }, onError = { err -> onResult(false, err) }, endpoint = "remove")
    }

    fun onRequestSent(callback: (FriendsInfo) -> Unit) {
        SocketHandler.getSocket().on(FriendsEvents.REQUEST_SENT.value) { args ->
            if (args.isNotEmpty()) {
                val update = Gson().fromJson(args[0].toString(), FriendsInfo::class.java)
                callback(update)
            }
        }
    }

    fun onRequestAccepted(callback: (FriendsInfo) -> Unit) {
        SocketHandler.getSocket().on(FriendsEvents.REQUEST_ACCEPTED.value) { args ->
            if (args.isNotEmpty()) {
                val update = Gson().fromJson(args[0].toString(), FriendsInfo::class.java)
                callback(update)
            }
        }
    }

    fun onRequestRejected(callback: (FriendsInfo) -> Unit) {
        SocketHandler.getSocket().on(FriendsEvents.REQUEST_REJECTED.value) { args ->
            if (args.isNotEmpty()) {
                val update = Gson().fromJson(args[0].toString(), FriendsInfo::class.java)
                callback(update)
            }
        }
    }

    fun onRequestCanceled(callback: (FriendsInfo) -> Unit) {
        SocketHandler.getSocket().on(FriendsEvents.REQUEST_CANCELLED.value) { args ->
            if (args.isNotEmpty()) {
                val update = Gson().fromJson(args[0].toString(), FriendsInfo::class.java)
                callback(update)
            }
        }
    }

    fun onFriendRemoved(callback: (FriendsInfo) -> Unit) {
        SocketHandler.getSocket().on(FriendsEvents.FRIEND_REMOVED.value) { args ->
            if (args.isNotEmpty()) {
                val update = Gson().fromJson(args[0].toString(), FriendsInfo::class.java)
                callback(update)
            }
        }
    }

    fun listenToAllFriendEvents(callback: (FriendsInfo, String) -> Unit) {
        onRequestSent { callback(it, FriendsEvents.REQUEST_SENT.value) }
        onRequestAccepted { callback(it, FriendsEvents.REQUEST_ACCEPTED.value) }
        onRequestRejected { callback(it, FriendsEvents.REQUEST_CANCELLED.value) }
        onRequestCanceled { callback(it, FriendsEvents.REQUEST_CANCELLED.value) }
        onFriendRemoved { callback(it, FriendsEvents.FRIEND_REMOVED.value) }
    }

    interface FriendsApiService : ApiService {
    }
}
