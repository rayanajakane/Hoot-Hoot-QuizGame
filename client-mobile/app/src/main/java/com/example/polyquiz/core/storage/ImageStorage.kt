package com.example.polyquiz.core.storage

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage

object ImageStorage {
    val storage = Firebase.storage
    val storageRef = storage.reference

    fun getAvatarRef(uid: String): StorageReference {
        return storageRef.child("avatars/${uid}");
    }

    fun uploadImage(image: ByteArray, uid: String) {
        val avatarStorageRef = getAvatarRef(uid)

        val uploadTask = avatarStorageRef.putBytes(image);
        uploadTask.addOnSuccessListener {
            Log.d("CACA", "Uploaded image")
        }.addOnFailureListener {
            Log.e("CACA", "CACA")
        }
    }

    fun getImageURL(ref: StorageReference, callback: (String?) -> Unit) {
        ref.downloadUrl.addOnSuccessListener { uri ->
            callback(uri.toString())
        }.addOnFailureListener {
            callback(null)
        }
    }

}
