package com.example.polyquiz.shop.domain

import android.util.Log
import com.example.polyquiz.auth.domain.AuthViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object PremiumAvatarService {
    private const val TAG = "PremiumAvatarService"

    private val _purchasedAvatars = MutableStateFlow<List<String>>(emptyList())
    val purchasedAvatars: StateFlow<List<String>> = _purchasedAvatars

    fun initialize(authViewModel: AuthViewModel) {
        val avatarsRef = authViewModel.getPurchasedAvatarsRef()

        avatarsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val avatars = mutableListOf<String>()
                snapshot.children.forEach { child ->
                    child.key?.let { avatars.add(it) }
                }
                _purchasedAvatars.value = avatars
                Log.d(TAG, "Purchased avatars: $avatars")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to read purchased avatars", error.toException())
            }
        })
    }

    fun updatePurchasedAvatars(avatarId: String) {
        val currentList = _purchasedAvatars.value.toMutableList()
        if (!currentList.contains(avatarId)) {
            currentList.add(avatarId)
            _purchasedAvatars.value = currentList
        }
    }
}
