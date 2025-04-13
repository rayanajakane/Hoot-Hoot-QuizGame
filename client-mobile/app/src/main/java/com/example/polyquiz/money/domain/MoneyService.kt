package com.example.polyquiz.money.domain
import android.util.Log
import android.content.Context
import com.example.polyquiz.R
import com.example.polyquiz.SnackbarEvent
import com.example.polyquiz.constants.MoneyEvents
import com.example.polyquiz.shop.domain.ShopItem
import com.example.vanillaprototype.socket.SocketHandler
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.polyquiz.SnackbarController
import org.json.JSONObject
import com.google.firebase.auth.FirebaseAuth


class MoneyService() {
    private val TAG = "MoneyService"
    private val mSocket = SocketHandler.getSocket()

    private val _currentBalance = MutableStateFlow(0)
    val currentBalance: StateFlow<Int> get() = _currentBalance

    private fun getUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    fun listenForMoneyEvents(context : Context,
        onAvatarBoughtCallback: (ShopItem) -> Unit = {},
        onThemeBoughtCallback: (ShopItem) -> Unit = {},
        onWallpaperBoughtCallback: (ShopItem) -> Unit = {}
    ) {
        onReturnBalance()
        onDonationGiven()
        onDonationReceived()
        onAvatarBought(onAvatarBoughtCallback)
        onThemeBought(onThemeBoughtCallback)
        onWallpaperBought(onWallpaperBoughtCallback)
        handleError(context)
    }

    fun stopListeningForMoneyEvents() {
        mSocket.off(MoneyEvents.RETURN_BALANCE.value)
        mSocket.off(MoneyEvents.DONATION_GIVEN.value)
        mSocket.off(MoneyEvents.DONATION_RECEIVED.value)
        mSocket.off(MoneyEvents.AVATAR_BOUGHT.value)
        mSocket.off(MoneyEvents.THEME_BOUGHT.value)
        mSocket.off(MoneyEvents.WALLPAPER_BOUGHT.value)
        mSocket.off(MoneyEvents.ERROR.value)
    }

    fun getCurrentBalance(userId: String) {
        mSocket.emit(MoneyEvents.GET_BALANCE.value, userId)
        Log.d(TAG, "Getting balance for $userId")
    }

    private fun onReturnBalance() {
        mSocket.on(MoneyEvents.RETURN_BALANCE.value) { args: Array<Any> ->
            if (args.isNotEmpty()) {
                val balance = args[0].toString().toIntOrNull() ?: 0
                _currentBalance.value = balance
                Log.d(TAG, "Current balance: $balance")
            }
        }
    }

    fun donateMoney(userId:String, friendId: String, amount: Int) {
        val data = JSONObject().apply {
            put("user", userId)
            put("friend", friendId)
            put("amount", amount)
        }
        Log.d(TAG, "Donating $amount to $friendId")
        mSocket.emit(MoneyEvents.DONATE_MONEY.value, data)
    }

    private fun onDonationGiven() {
        mSocket.on(MoneyEvents.DONATION_GIVEN.value) { args: Array<Any> ->
            if (args.isNotEmpty()) {
                val json = args[0].toString()
                try {
                    val donationData = Gson().fromJson(json, DonationGivenData::class.java)
//                    notificationService.displaySuccessMessage("You have donated ${donationData.amount} to ${donationData.to}")
                    Log.d(TAG, "You have donated ${donationData.amount} to ${donationData.to}")
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
                    Log.d(TAG, "${donationData.from} has donated ${donationData.amount} to you")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun buyAvatar(item: ShopItem) {
        val data = JSONObject().apply {
            put("user", getUserId())
            put("item", JSONObject().apply {
                put("id", item.id)
                put("imageUrl", item.imageUrl)
                put("price", item.price)
                put("owned", item.owned)
            })
        }
        Log.d(TAG, "Buying avatar: ${item.id} for ${item.price}")
        mSocket.emit(MoneyEvents.BUY_AVATAR.value, data)
    }

    fun buyTheme(item: ShopItem) {
        val data = JSONObject().apply {
            put("user", getUserId())
            put("item", JSONObject().apply {
                put("id", item.id)
                put("imageUrl", item.imageUrl)
                put("price", item.price)
                put("owned", item.owned)
            })
        }
        Log.d(TAG, "Buying theme: ${item.id} for ${item.price}")
        mSocket.emit(MoneyEvents.BUY_THEME.value, data)
    }

    fun buyWallpaper(item: ShopItem) {
        val data = JSONObject().apply {
            put("user", getUserId())
            put("item", JSONObject().apply {
                put("id", item.id)
                put("imageUrl", item.imageUrl)
                put("price", item.price)
                put("owned", item.owned)
            })
        }
        Log.d(TAG, "Buying wallpaper: ${item.id} for ${item.price}")
        mSocket.emit(MoneyEvents.BUY_WALLPAPER.value, data)
    }

    private fun onAvatarBought(callback: (ShopItem) -> Unit) {
        mSocket.on(MoneyEvents.AVATAR_BOUGHT.value) { args: Array<Any> ->
            if (args.isNotEmpty()) {
                val json = args[0].toString()
                try {
                    val item = Gson().fromJson(json, ShopItem::class.java)
                    callback(item)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun onThemeBought(callback: (ShopItem) -> Unit) {
        mSocket.on(MoneyEvents.THEME_BOUGHT.value) { args: Array<Any> ->
            if (args.isNotEmpty()) {
                val json = args[0].toString()
                try {
                    val item = Gson().fromJson(json, ShopItem::class.java)
                    callback(item)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun onWallpaperBought(callback: (ShopItem) -> Unit) {
        mSocket.on(MoneyEvents.WALLPAPER_BOUGHT.value) { args: Array<Any> ->
            if (args.isNotEmpty()) {
                val json = args[0].toString()
                try {
                    val item = Gson().fromJson(json, ShopItem::class.java)
                    callback(item)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }


    private fun handleError(context: Context) {
        mSocket.on(MoneyEvents.ERROR.value) { args: Array<Any> ->
            if (args.isNotEmpty()) {
                val errors: List<String> = try {
                    Gson().fromJson(args[0].toString(), Array<String>::class.java).toList()
                } catch (e: Exception) {
                    listOf(args[0].toString())
                }

                val displayText = errors.joinToString(separator = "\n") { errorKey: String ->
                    val cleanKey = errorKey.trim()
                    StringValue.dynamicLookup(context, cleanKey).asString(context)
                }

                val event = SnackbarEvent(
                    message = StringValue.DynamicString(displayText)
                )

                CoroutineScope(Dispatchers.Main).launch {
                    SnackbarController.sendEvent(event)
                }
            }
        }
    }


}
