package com.example.polyquiz.core

import android.util.Log
import com.example.polyquiz.ui.theme.Theme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference

object ThemeService {
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
                // If we reach else block, it means its an account created before configs was a thing
                callback(Theme.LIGHT) // default to Light theme
            }
        }
    }

    private fun stringToTheme(theme: String) : Theme {
        return when(theme.uppercase()) {
            "LIGHT" -> Theme.LIGHT
            "DARK" -> Theme.DARK
            // TODO : Add custom themes later
            else -> Theme.LIGHT // default to Light theme
        }
    }
}
