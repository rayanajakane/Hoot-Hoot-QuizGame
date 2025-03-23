package com.example.polyquiz.money.domain

import com.example.polyquiz.SnackbarController
import com.example.polyquiz.SnackbarEvent
import com.example.polyquiz.constants.MoneyEvents
import com.example.vanillaprototype.socket.SocketHandler
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Data classes for donation events
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

class MoneyService() {

    private val mSocket = SocketHandler.getSocket()

    private val _currentBalance = MutableStateFlow(0)
    val currentBalance: StateFlow<Int> get() = _currentBalance

    fun listenForMoneyEvents() {
        onReturnBalance()
        onDonationGiven()
        onDonationReceived()
        handleError()
    }

    fun stopListeningForMoneyEvents() {
        mSocket.off(MoneyEvents.RETURN_BALANCE.value)
        mSocket.off(MoneyEvents.DONATION_GIVEN.value)
        mSocket.off(MoneyEvents.DONATION_RECEIVED.value)
        mSocket.off(MoneyEvents.ERROR.value)
    }

    fun getCurrentBalance(userId: String) {
        mSocket.emit(MoneyEvents.GET_BALANCE.value, userId)
        println("Getting balance for $userId")
    }

    private fun onReturnBalance() {
        mSocket.on(MoneyEvents.RETURN_BALANCE.value) { args: Array<Any> ->
            if (args.isNotEmpty()) {
                val balance = args[0].toString().toIntOrNull() ?: 0
                _currentBalance.value = balance
                println("Current balance: $balance")
            }
        }
    }

    fun donateMoney(userId:String, friendId: String, amount: Int) {
        val data = JSONObject().apply {
            put("user", userId)
            put("friend", friendId)
            put("amount", amount)
        }
        println("Donating $amount to $friendId")
        mSocket.emit(MoneyEvents.DONATE_MONEY.value, data)
    }

    private fun onDonationGiven() {
        mSocket.on(MoneyEvents.DONATION_GIVEN.value) { args: Array<Any> ->
            if (args.isNotEmpty()) {
                val json = args[0].toString()
                try {
                    val donationData = Gson().fromJson(json, DonationGivenData::class.java)
//                    notificationService.displaySuccessMessage("You have donated ${donationData.amount} to ${donationData.to}")
                    println("You have donated ${donationData.amount} to ${donationData.to}")
                    _currentBalance.value = donationData.newBalance
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun onDonationReceived() {
        mSocket.on(MoneyEvents.DONATION_RECEIVED.value) { args: Array<Any> ->
            if (args.isNotEmpty()) {
                val json = args[0].toString()
                try {
                    val donationData = Gson().fromJson(json, DonationReceivedData::class.java)
//                    notificationService.displaySuccessMessage("${donationData.from} has donated ${donationData.amount} to you")
                    _currentBalance.value = donationData.newBalance
                    println("${donationData.from} has donated ${donationData.amount} to you")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun handleError() {
        mSocket.on(MoneyEvents.ERROR.value) { args: Array<Any> ->
            if (args.isNotEmpty()) {
                val errorMessage = args[0].toString()
                println(errorMessage)
//                notificationService.displayErrorMessage(errorMessage)
            }
        }
    }
}
