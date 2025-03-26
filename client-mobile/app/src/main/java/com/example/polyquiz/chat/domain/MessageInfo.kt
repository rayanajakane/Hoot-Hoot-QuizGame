package com.example.polyquiz.chat.domain
import com.example.polyquiz.auth.domain.UserIdName

data class MessageInfo(
    val roomCode: String,
    val message: Message
)

data class MessageEmojiInfo(
    val messageId: String,
    val chatEmoji: String,
    val userIdName: UserIdName,
    val roomCode: String?
)

data class ChatStateInfo(
    val roomCode: String,
    val playerUsername: String
)
