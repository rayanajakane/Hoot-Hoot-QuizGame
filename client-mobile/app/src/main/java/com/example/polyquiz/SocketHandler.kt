package com.example.vanillaprototype.socket

import android.util.Log
import com.example.polyquiz.constants.Environment
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
        mSocket.connect()
    }

    @Synchronized
    fun disconnect() {
        mSocket.disconnect()
    }
}
