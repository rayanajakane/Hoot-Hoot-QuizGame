package com.example.polyquiz.shop.domain

import android.util.Log
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.constants.Wallpaper
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

object WallpaperService {
    private const val TAG = "WallpaperService"

    private val _currentWallpaper = MutableStateFlow<String>(Wallpaper.None.value)
    val currentWallpaper: StateFlow<String> = _currentWallpaper

    private val _purchasedWallpapers = MutableStateFlow<List<String>>(emptyList())
    val purchasedWallpapers: StateFlow<List<String>> = _purchasedWallpapers

    fun initialize(authViewModel: AuthViewModel) {
        loadCurrentWallpaper(authViewModel)
        loadPurchasedWallpapers(authViewModel)
    }

    private fun loadCurrentWallpaper(authViewModel: AuthViewModel) {
        val wallpaperRef = authViewModel.getCurrentWallpaperRef()

        wallpaperRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val wallpaper = snapshot.getValue(String::class.java) ?: Wallpaper.None.value
                _currentWallpaper.value = wallpaper
                Log.d(TAG, "Current wallpaper: $wallpaper")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to read current wallpaper", error.toException())
            }
        })
    }

    private fun loadPurchasedWallpapers(authViewModel: AuthViewModel) {
        val wallpapersRef = authViewModel.getPurchasedWallpapersRef()

        wallpapersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val wallpapers = mutableListOf<String>()
                snapshot.children.forEach { child ->
                    child.key?.let { wallpapers.add(it) }
                }
                _purchasedWallpapers.value = wallpapers
                Log.d(TAG, "Purchased wallpapers: $wallpapers")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to read purchased wallpapers", error.toException())
            }
        })
    }

    suspend fun setWallpaper(authViewModel: AuthViewModel, wallpaper: String): Boolean {
        return try {
            val wallpaperRef = authViewModel.getCurrentWallpaperRef()
            wallpaperRef.setValue(wallpaper).await()
            _currentWallpaper.value = wallpaper
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set wallpaper", e)
            false
        }
    }

    fun updatePurchasedWallpapers(wallpaperId: String) {
        val currentList = _purchasedWallpapers.value.toMutableList()
        if (!currentList.contains(wallpaperId)) {
            currentList.add(wallpaperId)
            _purchasedWallpapers.value = currentList
        }
    }
}
