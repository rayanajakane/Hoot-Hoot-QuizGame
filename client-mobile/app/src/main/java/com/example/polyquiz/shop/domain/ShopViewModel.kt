package com.example.polyquiz.shop.domain

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.polyquiz.R
import com.example.polyquiz.SnackbarController
import com.example.polyquiz.SnackbarEvent
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.constants.AVATAR_PRICE
import com.example.polyquiz.constants.PremiumAvatar
import com.example.polyquiz.constants.THEME_PRICE
import com.example.polyquiz.constants.WALLPAPER_PRICE
import com.example.polyquiz.constants.Wallpaper
import com.example.polyquiz.core.ThemeService
import com.example.polyquiz.ui.theme.Theme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import StringValue
import com.example.polyquiz.constants.MoneyEvents
import com.example.polyquiz.money.domain.DonationGivenData
import com.example.polyquiz.money.domain.DonationReceivedData
import com.example.vanillaprototype.socket.SocketHandler
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.delay
import org.json.JSONObject

class ShopViewModel : ViewModel() {
    private val TAG = "ShopViewModel"
    private val _currentBalance = MutableStateFlow(0)
    val currentBalance: StateFlow<Int> get() = _currentBalance

    private val _avatarItems = MutableStateFlow<List<ShopItem>>(emptyList())
    val avatarItems: StateFlow<List<ShopItem>> = _avatarItems

    private val _themeItems = MutableStateFlow<List<ShopItem>>(emptyList())
    val themeItems: StateFlow<List<ShopItem>> = _themeItems

    private val _wallpaperItems = MutableStateFlow<List<ShopItem>>(emptyList())
    val wallpaperItems: StateFlow<List<ShopItem>> = _wallpaperItems

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _dataInitialized = MutableStateFlow(false)
    val dataInitialized: StateFlow<Boolean> = _dataInitialized

    suspend fun initialize(authViewModel: AuthViewModel) {
        _isLoading.value = true

        WallpaperService.initialize(authViewModel)
        PremiumAvatarService.initialize(authViewModel)
        ThemeService.loadPurchasedThemes(authViewModel)

        listenForMoneyEvents(
            onAvatarBoughtCallback = { onItemBought(it, authViewModel, "avatar") },
            onThemeBoughtCallback = { onItemBought(it, authViewModel, "theme") },
            onWallpaperBoughtCallback = { onItemBought(it, authViewModel, "wallpaper") }
        )

        delay(50)

        loadShopItems()
        _dataInitialized.value = true
    }

    private val mSocket = SocketHandler.getSocket()

    private fun getUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    fun listenForMoneyEvents(
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
        handleError()
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

    fun donateMoney(userId: String, friendId: String, amount: Int) {
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

    private fun handleError() {
        mSocket.on(MoneyEvents.ERROR.value) { args: Array<Any> ->
            if (args.isNotEmpty()) {
                val errorMessage = args[0].toString()
                Log.e(TAG, errorMessage)
//                notificationService.displayErrorMessage(errorMessage)
            }
        }
    }

    private fun onItemBought(item: ShopItem, authViewModel: AuthViewModel, type: String) {
        when (type) {
            "avatar" -> {
                Log.d(TAG, "Updating purchased avatars")
                PremiumAvatarService.updatePurchasedAvatars(item.id)
                updateAvatarItem(item.id, true)
            }

            "theme" -> {
                Log.d(TAG, "Updating purchased themes")
                ThemeService.updatePurchasedThemes(item.id)
                updateThemeItem(item.id, true)
            }

            "wallpaper" -> {
                Log.d(TAG, "Updating purchased wallpapers")
                WallpaperService.updatePurchasedWallpapers(item.id)
                updateWallpaperItem(item.id, true)
            }
        }

        viewModelScope.launch {
            try {
                when (type) {
                    "avatar" -> {
                        val avatarRef = authViewModel.getPurchasedAvatarsRef().child(item.id)
                        avatarRef.setValue(true)
                            .addOnSuccessListener {
                                Log.d(TAG, "Avatar purchase saved to Firebase: ${item.id}")
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Failed to save avatar purchase", e)
                            }
                    }

                    "theme" -> {
                        val themeRef = authViewModel.getPurchasedThemesRef().child(item.id)
                        themeRef.setValue(true)
                            .addOnSuccessListener {
                                Log.d(TAG, "Theme purchase saved to Firebase: ${item.id}")
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Failed to save theme purchase", e)
                            }
                    }

                    "wallpaper" -> {
                        val wallpaperRef = authViewModel.getPurchasedWallpapersRef().child(item.id)
                        wallpaperRef.setValue(true)
                            .addOnSuccessListener {
                                Log.d(TAG, "Wallpaper purchase saved to Firebase: ${item.id}")
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Failed to save wallpaper purchase", e)
                            }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving purchase to Firebase", e)
            }

            SnackbarController.sendEvent(
                SnackbarEvent(
                    message = StringValue.StringResource(R.string.purchase_successful)
                )
            )
        }
    }

    fun loadShopItems() {
        viewModelScope.launch {

            // Load premium avatars
            val avatars = PremiumAvatar.entries.map { avatar ->
                ShopItem(
                    id = avatar.name,
                    imageUrl = avatar.value,
                    price = AVATAR_PRICE,
                    owned = PremiumAvatarService.purchasedAvatars.value.contains(avatar.name)
                )
            }
            _avatarItems.value = avatars

            // Load premium themes
            val premiumThemes = arrayOf(Theme.LUIGI, Theme.MARIO, Theme.SONIC, Theme.PIKACHU)
            val themes = premiumThemes.map { theme ->
                ShopItem(
                    id = ThemeService.themeToString(theme),
                    imageUrl = getThemeImage(theme),
                    price = THEME_PRICE,
                    owned = ThemeService.purchasedThemes.value.contains(
                        ThemeService.themeToString(
                            theme
                        )
                    )
                )
            }
            _themeItems.value = themes

            // Load wallpapers
            val wallpapers = Wallpaper.entries
                .filter { it != Wallpaper.None }
                .map { wallpaper ->
                    ShopItem(
                        id = wallpaper.name,
                        imageUrl = wallpaper.value,
                        price = WALLPAPER_PRICE,
                        owned = WallpaperService.purchasedWallpapers.value.contains(wallpaper.name)
                    )
                }
            _wallpaperItems.value = wallpapers

            _isLoading.value = false
        }
    }

    fun buyAvatar(item: ShopItem) {
        if (item.owned) {
            viewModelScope.launch {
                SnackbarController.sendEvent(
                    SnackbarEvent(
                        message = StringValue.StringResource(R.string.already_owned)
                    )
                )
            }
            return
        }

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
        if (item.owned) {
            viewModelScope.launch {
                SnackbarController.sendEvent(
                    SnackbarEvent(
                        message = StringValue.StringResource(R.string.already_owned)
                    )
                )
            }
        }

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
        if (item.owned) {
            viewModelScope.launch {
                SnackbarController.sendEvent(
                    SnackbarEvent(
                        message = StringValue.StringResource(R.string.already_owned)
                    )
                )
            }
            return
        }

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

    private fun updateAvatarItem(id: String, owned: Boolean) {
        _avatarItems.value = _avatarItems.value.map {
            if (it.id == id) it.copy(owned = owned) else it
        }
    }

    private fun updateThemeItem(id: String, owned: Boolean) {
        _themeItems.value = _themeItems.value.map {
            if (it.id == id) it.copy(owned = owned) else it
        }
    }

    private fun updateWallpaperItem(id: String, owned: Boolean) {
        _wallpaperItems.value = _wallpaperItems.value.map {
            if (it.id == id) it.copy(owned = owned) else it
        }
    }

    private fun getThemeImage(theme: Theme): String {
        return when (theme) {
            Theme.LUIGI -> "https://wallpapers.com/images/high/pastel-light-green-plain-t42jcuek6ib3bhiy.webp"
            Theme.MARIO -> "https://wallpapers.com/images/high/pastel-red-background-qjnfkdv8yc64c74a.webp"
            Theme.SONIC -> "https://wallpapers.com/images/high/pastel-blue-aesthetic-desktop-u31lqfendbf31844.webp"
            Theme.PIKACHU -> "https://wallpapers.com/images/high/pastel-yellow-cream-background-iz570fvika661m14.webp"
            else -> ""
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopListeningForMoneyEvents()
    }
}
