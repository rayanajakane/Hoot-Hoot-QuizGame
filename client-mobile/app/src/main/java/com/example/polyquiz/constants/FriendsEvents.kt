package com.example.polyquiz.constants

enum class FriendsEvents(val value: String) {
    REQUEST_SENT("requestSent"),
    REQUEST_ACCEPTED("requestAccepted"),
    REQUEST_REJECTED("requestRejected"),
    REQUEST_CANCELED("requestCanceled"),
    FRIEND_REMOVED("friendRemoved"),
    RETURN_ALL_USERS("returnAllUsers"),
    RETURN_ALL_FRIENDS("returnAllFriends"),
    RETURN_ALL_PENDING_REQUESTS("returnAllPendingRequests"),
    RETURN_ALL_SENT_REQUESTS("returnAllSentRequests"),
    RETURN_ALL_DATA("returnAllData"),
    UPDATE_DATA("updateData"),
    USER_DELETED("userDeleted"),
    CONNECT("connectUser"),
}
