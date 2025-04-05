package com.example.polyquiz.core

import android.util.Log
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.ui.theme.Theme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ThemeService {
    private const val TAG = "ThemeService"

    // Add the missing MutableStateFlow for purchased themes
    private val _purchasedThemes = MutableStateFlow<List<String>>(emptyList())
    val purchasedThemes: StateFlow<List<String>> = _purchasedThemes

    fun saveThemeToDB(theme: Theme, configsRef: DatabaseReference) {
        configsRef.child("theme").setValue(theme.toString())
        Log.d("Theme service", "Saved $theme to DB")
    }

    fun getThemeFromDB(configsRef: DatabaseReference, callback: (Theme) -> Unit) {
        configsRef.child("theme").get().addOnSuccessListener { dataSnapshot : DataSnapshot ->
            if(dataSnapshot.exists()) {
                Log.d("Theme service", "Got ${dataSnapshot.value.toString()} from DB")
                callback(stringToTheme(dataSnapshot.value.toString())) // return theme in callback
            } else {
                callback(Theme.LIGHT)
            }
        }
    }

    fun loadPurchasedThemes(authViewModel: AuthViewModel) {
        val themesRef = authViewModel.getPurchasedThemesRef()

        themesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val themes = mutableListOf<String>()
                snapshot.children.forEach { child ->
                    child.key?.let { themes.add(it) }
                }
                _purchasedThemes.value = themes
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to read purchased themes", error.toException())
            }
        })
    }

    fun updatePurchasedThemes(themeId: String) {
        val currentList = _purchasedThemes.value.toMutableList()
        if (!currentList.contains(themeId)) {
            currentList.add(themeId)
            _purchasedThemes.value = currentList
        }
    }

    fun isPremiumTheme(theme: Theme): Boolean {
        return when(theme) {
            Theme.LIGHT, Theme.DARK -> false
            else -> true
        }
    }

    fun isThemePurchased(theme: Theme): Boolean {
        if (!isPremiumTheme(theme)) return true
        return _purchasedThemes.value.contains(theme.toString())
    }

    private fun stringToTheme(theme: String) : Theme {
        return when(theme.uppercase()) {
            "LIGHT" -> Theme.LIGHT
            "DARK" -> Theme.DARK
            "LUIGI" -> Theme.LUIGI
            "MARIO" -> Theme.MARIO
            "SONIC" -> Theme.SONIC
            "PIKACHU" -> Theme.PIKACHU
            else -> Theme.LIGHT // default to Light theme
        }
    }

    fun getPremiumThemes(): List<Theme> {
        return listOf(Theme.LUIGI, Theme.MARIO, Theme.SONIC, Theme.PIKACHU)
    }

    fun getStandardThemes(): List<Theme> {
        return listOf(Theme.LIGHT, Theme.DARK)
    }

    fun getAvailableThemes(): List<Theme> {
        val available = getStandardThemes().toMutableList()
        val purchasedThemeNames = _purchasedThemes.value

        getPremiumThemes().forEach { theme ->
            if (purchasedThemeNames.contains(theme.toString())) {
                available.add(theme)
            }
        }

        return available
    }
}
