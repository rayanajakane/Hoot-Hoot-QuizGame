package com.example.polyquiz.money.domain

data class DonationGivenData(
    val to: String,
    val amount: Int,
    val newBalance: Int
)

data class DonationReceivedData(
    val from: String,
    val amount: Int,
    val newBalance: Int
)
