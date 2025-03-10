package com.example.polyquiz.constants

import com.example.polyquiz.auth.domain.UserIdName
import com.example.polyquiz.chat.domain.Message

data class MessageInfo (
    val roomCode: String,
    val message: Message
)

data class MessageEmojiInfo (
    val messageId: String,
    val chatEmoji: ChatEmoji,
    val userIdName: UserIdName,
    val roomCode : String?
)
