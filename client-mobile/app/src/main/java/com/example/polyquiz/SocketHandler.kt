package com.example.vanillaprototype.socket

import android.util.Log
import com.example.polyquiz.chat.domain.ChatService
import com.example.polyquiz.constants.ChatEvents
import com.example.polyquiz.constants.Environment
import com.example.polyquiz.elo.domain.EloEvents
import com.example.polyquiz.elo.domain.EloService
import io.socket.client.IO
import io.socket.client.Socket

// REFERENCE: https://medium.com/@thushenarriyam/socket-io-connection-on-android-kotlin-to-node-js-server-71b218c160c9
object SocketHandler {
    lateinit var mSocket: Socket

    @Synchronized
    fun setSocket() {
        try {
            mSocket = IO.socket(Environment.SERVER_LOCAL_ADDRESS.value)
        } catch (e: Exception) {
            Log.e("ERROR", e.toString())
        }
    }

    @Synchronized
    fun getSocket(): Socket {
        return mSocket
    }

    @Synchronized
    fun connect() {
        if (!mSocket.connected()) {
            mSocket.connect()
            ChatService.deleteMessages()
            ChatService.handleReceivedMessage()
            ChatService.handleRoomMessage()
            EloService.returnElo()

        }
    }

    @Synchronized
    fun disconnect() {
        mSocket.off(ChatEvents.SENT_GENERAL_MESSAGE.value)
        mSocket.off(ChatEvents.NEW_MESSAGE.value)
        ChatService.deleteMessages()
        mSocket.off(EloEvents.RETURN_ELO.value)
        mSocket.disconnect()
    }
}
