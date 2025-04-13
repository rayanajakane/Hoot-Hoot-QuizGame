package com.example.polyquiz.chat.domain

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.polyquiz.auth.domain.UserIdName
import com.example.polyquiz.constants.ChatEvents
import com.example.polyquiz.constants.MatchContext
import com.example.polyquiz.match.domain.MatchContextService
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
    var channel = ChatChannel.GENERAL.value


    private val mSocket = SocketHandler.getSocket()

    fun addMessage(newMessage: Message) {
        if (_generalMessages.value != null && _generalMessages.value!!.isNotEmpty()) {
            if (newMessage.equals(_generalMessages.value!![_generalMessages.value!!.size - 1])) {
                // TEMPORARY HOTFIX: To avoid double messages
                return
            }
        }
        val newMessages = (_generalMessages.value ?: emptyList()).plus(newMessage)
        // REFERENCE: //https://stackoverflow.com/questions/53304347/mutablelivedata-cannot-invoke-setvalue-on-a-background-thread-from-coroutine
        // Using postValue is asynchronous (unlike setValue)
        _generalMessages.postValue(newMessages)
    }

    fun deleteMessages() {
        _generalMessages.postValue(emptyList())
    }

    fun sendMessage(text: String, userId: String, username: String, photoUrl: String, roomCode: String?) {
        val startTime = System.currentTimeMillis()
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
                val elapsedTime = System.currentTimeMillis() - startTime
                Log.d("Send message", "Elapsed time : $elapsedTime")
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
                val elapsedTime = System.currentTimeMillis() - startTime
                Log.d("Send message", "Elapsed time : $elapsedTime")
            }
        }
    }


    fun handleReceivedMessage() {
        mSocket.on(ChatEvents.SENT_GENERAL_MESSAGE.value) { args ->
            val startTime = System.currentTimeMillis()
            if (args[0] != null) {
                handleGeneralEmoji()
                val newMessage = Gson().fromJson(args[0].toString(), Message::class.java) as Message
                addMessage(newMessage)
                val elapsedTime = System.currentTimeMillis() - startTime
                Log.d("Receive message", "Elapsed time : $elapsedTime")
            }
        }
    }

    fun handleRoomEmoji() {
        mSocket.on(ChatEvents.SENT_ROOM_EMOJI.value) { args ->
            val startTime = System.currentTimeMillis()
            if (args[0] != null) {
                val updatedMessage =
                    Gson().fromJson(args[0].toString(), Message::class.java) as Message
                _matchRoomMessages.value?.let { messages ->
                    val messageIndex = messages.indexOfFirst { it.id == updatedMessage.id }
                    if (messageIndex > -1) {
                        val updatedMessages =
                            messages.toMutableList().apply { this[messageIndex] = updatedMessage }
                        _matchRoomMessages.postValue(updatedMessages)
                    }
                }
                val elapsedTime = System.currentTimeMillis() - startTime
                Log.d("handle emoji", "Elapsed time : $elapsedTime")
            }

        }
    }

    fun addRoomMessage(newMessage: Message) {
        if (_matchRoomMessages.value != null && _matchRoomMessages.value!!.isNotEmpty()) {
            if (newMessage.equals(matchRoomMessages.value!![_matchRoomMessages.value!!.size - 1])) {
                // TEMPORARY HOTFIX: To avoid double messages
                return
            }
        }
        val newMessages = (_matchRoomMessages.value ?: emptyList()).plus(newMessage)
        // REFERENCE: //https://stackoverflow.com/questions/53304347/mutablelivedata-cannot-invoke-setvalue-on-a-background-thread-from-coroutine
        // Using postValue is asynchronous (unlike setValue)
        _matchRoomMessages.postValue(newMessages)
    }

    fun deleteRoomMessages() {
        _matchRoomMessages.postValue(emptyList())
    }

    fun handleRoomMessage() {
        mSocket.on(ChatEvents.NEW_MESSAGE.value) { args ->
            val startTime = System.currentTimeMillis()
            if (args[0] != null) {
                handleRoomEmoji()
                val newMessage = Gson().fromJson(args[0].toString(), MessageInfo::class.java) as MessageInfo
                addRoomMessage(newMessage.message)
            }
            val elapsedTime = System.currentTimeMillis() - startTime
            Log.d("handleRoomMessage", "Elapsed time : $elapsedTime")
        }
    }

    fun reactToMessage(messageId: String, chatEmoji: ChatEmoji, userId: String, username: String, roomCode: String?) {
        val userIdName = UserIdName(userId, username)
        if (channel == ChatChannel.GENERAL.value) {
            val messageEmojiInfo = MessageEmojiInfo(messageId, chatEmoji.value, userIdName, null)
            val messageEmojiInfoStringified = Gson().toJson(messageEmojiInfo)
            val messageEmojiInfoJsonObject = JSONObject(messageEmojiInfoStringified)
            mSocket.emit(ChatEvents.GENERAL_EMOJI.value, messageEmojiInfoJsonObject)
        } else if (channel == ChatChannel.ROOM.value && MatchContextService.context.value != MatchContext.Null) {
            val messageEmojiInfo = MessageEmojiInfo(messageId, chatEmoji.value, userIdName, roomCode)
            val messageEmojiInfoStringified = Gson().toJson(messageEmojiInfo)
            val messageEmojiInfoJsonObject = JSONObject(messageEmojiInfoStringified)
            mSocket.emit(ChatEvents.ROOM_EMOJI.value, messageEmojiInfoJsonObject)
        }
    }
    fun handleGeneralEmoji() {
        mSocket.on(ChatEvents.SENT_GENERAL_EMOJI.value) { args ->
            val startTime = System.currentTimeMillis()
            if (args[0] != null) {
                val updatedMessage =
                    Gson().fromJson(args[0].toString(), Message::class.java) as Message
                _generalMessages.value?.let { messages ->
                    val messageIndex = messages.indexOfFirst { it.id == updatedMessage.id }
                    if (messageIndex > -1) {
                        val updatedMessages =
                            messages.toMutableList().apply { this[messageIndex] = updatedMessage }
                        _generalMessages.postValue(updatedMessages)
                    }
                }
            }
            val elapsedTime = System.currentTimeMillis() - startTime
            Log.d("handleGeneralEmoji", "Elapsed time : $elapsedTime")

        }
    }
}
