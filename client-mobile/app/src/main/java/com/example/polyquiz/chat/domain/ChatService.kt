package com.example.polyquiz.chat.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.polyquiz.auth.domain.UserIdName
import com.example.polyquiz.constants.ChatEvents
import com.example.vanillaprototype.socket.SocketHandler
import com.google.gson.Gson
import org.json.JSONObject

import java.time.Instant
import java.util.Date

object ChatService {
    // REFERENCE: https://stackoverflow.com/questions/76115972/how-to-add-a-new-item-to-a-mutablelivedata-mutablelist-android-kotlin
    private var _generalMessages = MutableLiveData<List<Message>>()
    val generalMessages: LiveData<List<Message>> = _generalMessages
    private var _matchRoomMessages = MutableLiveData<List<Message>>()
    val matchRoomMessages: LiveData<List<Message>> = _matchRoomMessages

    private val mSocket = SocketHandler.getSocket()

    fun addMessage(newMessage: Message) {
        if (_generalMessages.value != null && _generalMessages.value!!.isNotEmpty()) {
            if (newMessage.equals(generalMessages.value!![_generalMessages.value!!.size - 1])) {
                // TEMPORARY HOTFIX: To avoid double messages
                return
            }
        }
        println("adding message")
        val newMessages = (_generalMessages.value ?: emptyList()).plus(newMessage)
        // REFERENCE: //https://stackoverflow.com/questions/53304347/mutablelivedata-cannot-invoke-setvalue-on-a-background-thread-from-coroutine
        // Using postValue is asynchronous (unlike setValue)
        _generalMessages.postValue(newMessages)
    }

    fun addRoomMessage(newMessage: Message) {
        if (_matchRoomMessages.value != null && _matchRoomMessages.value!!.isNotEmpty()) {
            if (newMessage.equals(matchRoomMessages.value!![_matchRoomMessages.value!!.size - 1])) {
                // TEMPORARY HOTFIX: To avoid double messages
                return
            }
        }
        println("adding room message")
        val newMessages = (_matchRoomMessages.value ?: emptyList()).plus(newMessage)
        // REFERENCE: //https://stackoverflow.com/questions/53304347/mutablelivedata-cannot-invoke-setvalue-on-a-background-thread-from-coroutine
        // Using postValue is asynchronous (unlike setValue)
        _matchRoomMessages.postValue(newMessages)
    }

    fun deleteMessages() {
        _generalMessages.postValue(emptyList())
    }

    fun deleteRoomMessages() {
        _matchRoomMessages.postValue(emptyList())
    }

    fun sendMessage(text: String, userId: String, username: String, photoUrl: String, roomCode: String?) {
        if (text.filterNot { it.isWhitespace() }.isNotEmpty()) {
            if (roomCode != null) {
                val newMessage = Message(
                    "", text.trim(), userId, username, photoUrl, Date.from(
                        Instant.now()
                    ), listOf(), listOf(), listOf()
                )
                val newMessageInfo = MessageInfo(roomCode, newMessage)
                val newMessageInfoStringified = Gson().toJson(newMessageInfo)
                val newMessageInfoJsonObject = JSONObject(newMessageInfoStringified)
                mSocket.emit(ChatEvents.ROOM_MESSAGE.value, newMessageInfoJsonObject)
            }
            else {
                val newMessage = Message(
                    "", text.trim(), userId, username, photoUrl, Date.from(
                        Instant.now()
                    ), listOf(), listOf(), listOf()
                )
                val newMessageStringified = Gson().toJson(newMessage)
                val newMessageJsonObject = JSONObject(newMessageStringified)
                mSocket.emit(ChatEvents.GENERAL_MESSAGE.value, newMessageJsonObject)
            }
        }
    }

    fun handleReceivedMessage() {
        mSocket.on(ChatEvents.SENT_GENERAL_MESSAGE.value) { args ->
            if (args[0] != null) {
                println("connecting to room socket")
                val newMessage = Gson().fromJson(args[0].toString(), Message::class.java) as Message
                addMessage(newMessage)
            }
        }
    }

    fun handleRoomMessage() {
        mSocket.on(ChatEvents.NEW_MESSAGE.value) { args ->
            if (args[0] != null) {
                println("handling room message")
                val newMessage = Gson().fromJson(args[0].toString(), MessageInfo::class.java) as MessageInfo
                addRoomMessage(newMessage.message)
            }
        }
    }

//    fun disconnectFromRoom() {
//        mSocket.off(ChatEvents.NEW_MESSAGE.value)
//    }
}
