package com.example.polyquiz.chat.domain

import com.example.polyquiz.auth.domain.UserIdName
import java.util.Date

data class Message(
    val id: String,
    val text: String,
    val authorId: String,
    val authorUsername: String,
    val photoUrl: String,
    val date: Date,
    val userLikes: List<UserIdName>,
    val userLoves: List<UserIdName>,
    val userDislikes: List<UserIdName>
)
