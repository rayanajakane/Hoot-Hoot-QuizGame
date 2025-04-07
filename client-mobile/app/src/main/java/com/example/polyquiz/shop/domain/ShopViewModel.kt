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
import com.example.polyquiz.money.domain.MoneyService
import com.example.polyquiz.ui.theme.Theme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import StringValue
import kotlinx.coroutines.delay

class ShopViewModel : ViewModel() {
    private val TAG = "ShopViewModel"

    private val _avatarItems = MutableStateFlow<List<ShopItem>>(emptyList())
    val avatarItems: StateFlow<List<ShopItem>> = _avatarItems

    private val _themeItems = MutableStateFlow<List<ShopItem>>(emptyList())
    val themeItems: StateFlow<List<ShopItem>> = _themeItems

    private val _wallpaperItems = MutableStateFlow<List<ShopItem>>(emptyList())
    val wallpaperItems: StateFlow<List<ShopItem>> = _wallpaperItems

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val moneyService = MoneyService()

    private val _dataInitialized = MutableStateFlow(false)
    val dataInitialized: StateFlow<Boolean> = _dataInitialized

    suspend fun initialize(authViewModel: AuthViewModel) {
        _isLoading.value = true

        WallpaperService.initialize(authViewModel)
        PremiumAvatarService.initialize(authViewModel)
        ThemeService.loadPurchasedThemes(authViewModel)

        moneyService.listenForMoneyEvents(
            onAvatarBoughtCallback = { onItemBought(it, authViewModel, "avatar") },
            onThemeBoughtCallback = { onItemBought(it, authViewModel, "theme") },
            onWallpaperBoughtCallback = { onItemBought(it, authViewModel, "wallpaper") }
        )

        delay(500)

        loadShopItems()
        _dataInitialized.value = true
    }

    private fun onItemBought(item: ShopItem, authViewModel: AuthViewModel, type: String) {
        when (type) {
            "avatar" -> {
                println("Updating purchased avatars")
                PremiumAvatarService.updatePurchasedAvatars(item.id)
                updateAvatarItem(item.id, true)
            }
            "theme" -> {
                println("Updating purchased themes")
                ThemeService.updatePurchasedThemes(item.id)
                updateThemeItem(item.id, true)
            }
            "wallpaper" -> {
                println("Updating purchased wallpapers")
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
                    owned = ThemeService.purchasedThemes.value.contains(ThemeService.themeToString(theme))
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

        moneyService.buyAvatar(item)
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

        moneyService.buyTheme(item)
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

        moneyService.buyWallpaper(item)
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
        moneyService.stopListeningForMoneyEvents()
    }
}
