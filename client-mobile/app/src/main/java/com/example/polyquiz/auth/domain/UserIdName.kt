package com.example.polyquiz.auth.domain

data class UserIdName(
    val id: String,
    val name: String,
    val photoUrl: String? = null,
    val isOnline: Boolean? = false
)
