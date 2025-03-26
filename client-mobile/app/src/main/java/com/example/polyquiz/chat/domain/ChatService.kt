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

    fun sendMessage(text: String, userId: String, username: String, photoUrl: String) {
        if (text.filterNot { it.isWhitespace() }.isNotEmpty()) {
            val newMessage = Message("", text.trim(), userId, username, photoUrl, Date.from(
                Instant.now()), listOf(), listOf(), listOf())
            val newMessageStringified = Gson().toJson(newMessage)
            val newMessageJsonObject = JSONObject(newMessageStringified)
            mSocket.emit(ChatEvents.GENERAL_MESSAGE.value, newMessageJsonObject);
        }
    }

    fun handleReceivedMessage() {
        mSocket.on(ChatEvents.SENT_GENERAL_MESSAGE.value) { args ->
            if (args[0] != null) {
                val newMessage = Gson().fromJson(args[0].toString(), Message::class.java) as Message
                addMessage(newMessage)
            }
        }
    }

    fun handleRoomEmoji() {
        mSocket.on(ChatEvents.SENT_ROOM_EMOJI.value) { args ->
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
            }

        }
    }


}
