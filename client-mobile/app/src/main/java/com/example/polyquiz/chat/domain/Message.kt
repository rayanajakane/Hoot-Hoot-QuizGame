package com.example.polyquiz.chat.domain

import java.util.Date

data class Message(
    val text: String,
    val author: String,
    val date: Date
)
