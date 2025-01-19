package com.example.polyquiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.ui.theme.PolyQuizTheme
import com.example.polyquiz.chat.domain.ChatService
import com.example.vanillaprototype.socket.SocketHandler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // TODO: Check if we want to make a separate activity for the chat
        SocketHandler.setSocket()
        SocketHandler.connect() // TODO: Find most appropriate place to disconnect (probably after logout)
        ChatService.handleReceivedMessage() // TODO: Find a more appropriate place to do this (probably after login/signup; it should be guarded by auth)
        val authViewModel : AuthViewModel by viewModels()
        setContent {
            PolyQuizTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Navigation(modifier = Modifier.padding(innerPadding), authViewModel = authViewModel)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PolyQuizTheme {
        // Can be used to preview composable
    }
}
