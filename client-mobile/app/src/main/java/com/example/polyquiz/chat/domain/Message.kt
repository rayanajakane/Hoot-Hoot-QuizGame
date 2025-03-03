package com.example.polyquiz.chat.domain

import java.util.Date

data class Message(
    val id: String,
    val text: String,
    val authorId: String,
    val authorUsername: String,
    val photoUrl: String,
    val date: Date
)
