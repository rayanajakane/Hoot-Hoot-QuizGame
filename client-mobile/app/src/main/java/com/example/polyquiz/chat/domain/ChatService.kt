package com.example.polyquiz.chat.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.polyquiz.auth.domain.UserIdName
import com.example.polyquiz.constants.ChatChannel
import com.example.polyquiz.constants.ChatEmoji
import com.example.polyquiz.constants.ChatEvents
import com.example.polyquiz.constants.MatchContext
import com.example.polyquiz.constants.MessageEmojiInfo
import com.example.polyquiz.constants.MessageInfo
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
    var channel = ChatChannel.GENERAL

    private val mSocket = SocketHandler.getSocket()

    fun addGeneralMessage(newMessage: Message) {
        if (_generalMessages.value != null && _generalMessages.value!!.isNotEmpty()) {
            if (newMessage.equals(generalMessages.value!![_generalMessages.value!!.size - 1])) {
                // TEMPORARY HOTFIX: To avoid double messages
                return
            }


            val newMessages = (_generalMessages.value ?: emptyList()).plus(newMessage)
            // REFERENCE: //https://stackoverflow.com/questions/53304347/mutablelivedata-cannot-invoke-setvalue-on-a-background-thread-from-coroutine
            // Using postValue is asynchronous (unlike setValue)
            _generalMessages.postValue(newMessages)
            println("should be posting the message")
        }

    }

    fun addRoomMessage(newMessage: MessageInfo) {
        if (_matchRoomMessages.value != null && _matchRoomMessages.value!!.isNotEmpty()) {
            if (newMessage.message.equals(matchRoomMessages.value!![_matchRoomMessages.value!!.size - 1])) {
                // TEMPORARY HOTFIX: To avoid double messages
                return
            }


            val newMessages = (_matchRoomMessages.value ?: emptyList()).plus(newMessage.message)
            // REFERENCE: //https://stackoverflow.com/questions/53304347/mutablelivedata-cannot-invoke-setvalue-on-a-background-thread-from-coroutine
            // Using postValue is asynchronous (unlike setValue)
            _matchRoomMessages.postValue(newMessages)
        }

    }

    fun sendMessage(message: Message, roomCode: String?) {
        if(channel === ChatChannel.GENERAL || roomCode.isNullOrBlank()) {
            sendGeneralMessage(message)
            addGeneralMessage(message)
        } else if (channel === ChatChannel.ROOM && MatchContextService.getContext() !== MatchContext.Null) {
            sendRoomMessage(roomCode, message)
            addRoomMessage(MessageInfo(roomCode, message))
        }
    }
    fun sendGeneralMessage(newMessage: Message) {
        mSocket.emit(ChatEvents.GENERAL_MESSAGE.value, newMessage)
    }

    fun sendRoomMessage(roomCode: String, newMessage: Message) {
        val messageInfo = MessageInfo(roomCode, newMessage)
        val messageInfoStringified = Gson().toJson(messageInfo)
        val messageInfoJson = JSONObject(messageInfoStringified)
        mSocket.emit(ChatEvents.ROOM_MESSAGE.value, messageInfoJson)
    }


    fun deleteMessages() {
        _generalMessages.postValue(emptyList())
    }

    fun sendMessage(text: String, userId: String, username: String, photoUrl: String, roomCode: String?) {
        if (text.filterNot { it.isWhitespace() }.isNotEmpty()) {
            val newMessage = Message("", text.trim(), userId, username, photoUrl, Date.from(
                Instant.now()), listOf(), listOf(), listOf())
            sendMessage(newMessage, roomCode)
            val newMessageStringified = Gson().toJson(newMessage)
            val newMessageJsonObject = JSONObject(newMessageStringified)
            mSocket.emit(ChatEvents.GENERAL_MESSAGE.value, newMessageJsonObject)
        }
    }
    fun reactToMessage(messageId: String, chatEmoji: ChatEmoji, userId: String, username: String, roomCode: String?) {
        val userIdName = UserIdName(userId, username)
        val messageEmojiInfo: MessageEmojiInfo
        if (channel === ChatChannel.GENERAL) {
            messageEmojiInfo = MessageEmojiInfo(messageId, chatEmoji, userIdName, null)
            val messageEmojiInfoStringified = Gson().toJson(messageEmojiInfo)
            val messageEmojiInfoJson = JSONObject(messageEmojiInfoStringified)
            mSocket.emit(ChatEvents.GENERAL_EMOJI.value, messageEmojiInfoJson)
        } else if (channel === ChatChannel.ROOM && MatchContextService.getContext() !== MatchContext.Null) {
            messageEmojiInfo = MessageEmojiInfo(messageId, chatEmoji, userIdName, roomCode)
            val messageEmojiInfoStringified = Gson().toJson(messageEmojiInfo)
            val messageEmojiInfoJson = JSONObject(messageEmojiInfoStringified)
            mSocket.emit(ChatEvents.ROOM_EMOJI.value, messageEmojiInfoJson)
        }

    }

    fun handleReceivedMessages() {
        mSocket.on(ChatEvents.SENT_GENERAL_MESSAGE.value) { args ->
            if (args[0] != null) {
                val newMessage = Gson().fromJson(args[0].toString(), Message::class.java) as Message
                addGeneralMessage(newMessage)
            }
        }
    }

    fun handleRoomMessages() {
        mSocket.on(ChatEvents.NEW_MESSAGE.value) { args ->
            if (args[0] != null) {
                val newMessageInfo = Gson().fromJson(args[0].toString(), MessageInfo::class.java) as MessageInfo
                addRoomMessage(newMessageInfo)
            }
        }
    }

    fun clearMessages() {
        _generalMessages.postValue(emptyList())
    }

    fun clearMatchRoomMessages() {
        _matchRoomMessages.postValue(emptyList())
    }
    fun handleGeneralEmoji() {
        mSocket.on(ChatEvents.SENT_GENERAL_EMOJI.value) { args ->
            if (args.isNotEmpty() && args[0] != null) {
                val updatedMessage = Gson().fromJson(args[0].toString(), Message::class.java)

                _generalMessages.value?.let { currentMessages ->
                    val updatedList = currentMessages.map { message ->
                        if (message.id == updatedMessage.id) updatedMessage else message
                    }
                    _generalMessages.postValue(updatedList)
                }
            }
        }
    }

    fun handleRoomEmoji() {
        mSocket.on(ChatEvents.SENT_ROOM_EMOJI.value) { args ->
            if (args.isNotEmpty() && args[0] != null) {
                val updatedMessage = Gson().fromJson(args[0].toString(), Message::class.java)

                _matchRoomMessages.value?.let { currentMessages ->
                    val updatedList = currentMessages.map { message ->
                        if (message.id == updatedMessage.id) updatedMessage else message
                    }
                    _matchRoomMessages.postValue(updatedList)
                }
            }
        }
    }

}

