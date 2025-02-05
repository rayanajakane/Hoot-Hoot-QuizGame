package com.example.polyquiz.chat.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.polyquiz.constants.ChatEvents
import com.example.vanillaprototype.socket.SocketHandler
import com.google.gson.Gson
import org.json.JSONObject

import java.time.Instant
import java.util.Date

object ChatService {
    // REFERENCE: https://stackoverflow.com/questions/76115972/how-to-add-a-new-item-to-a-mutablelivedata-mutablelist-android-kotlin
    private var _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val mSocket = SocketHandler.getSocket()

    fun addMessage(newMessage: Message) {
        if (_messages.value != null && _messages.value!!.isNotEmpty()) {
            if (newMessage.equals(messages.value!![_messages.value!!.size - 1])) {
                // TEMPORARY HOTFIX: To avoid double messages
                return
            }
        }
        val newMessages = (_messages.value ?: emptyList()).plus(newMessage)
        // REFERENCE: //https://stackoverflow.com/questions/53304347/mutablelivedata-cannot-invoke-setvalue-on-a-background-thread-from-coroutine
        // Using postValue is asynchronous (unlike setValue)
        _messages.postValue(newMessages)
    }

    fun deleteMessages() {
        _messages.postValue(emptyList())
    }

    fun sendMessage(text: String, username: String) {
        if (text.filterNot { it.isWhitespace() }.isNotEmpty()) {
            val newMessage = Message(text.trim(), username, Date.from(
                Instant.now()))
            val newMessageStringified = Gson().toJson(newMessage)
            val newMessageJsonObject = JSONObject(newMessageStringified)
            mSocket.emit(ChatEvents.PROTOTYPE_MESSAGE.value, newMessageJsonObject);
        }
    }

    fun handleReceivedMessage() {
        mSocket.on(ChatEvents.SENT_PROTOTYPE_MESSAGE.value) { args ->
            if (args[0] != null) {
                val newMessage = Gson().fromJson(args[0].toString(), Message::class.java) as Message
                addMessage(newMessage)
            }
        }
    }
}
