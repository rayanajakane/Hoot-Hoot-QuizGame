package com.example.polyquiz.core.storage

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import java.io.ByteArrayOutputStream

object ImageStorage {
    val storage = Firebase.storage
    val storageRef = storage.reference

    fun getAvatarRef(uid: String): StorageReference {
        return storageRef.child("avatars/${uid}");
    }

    fun deleteAvatar(uid: String) {
        val avatarRef = getAvatarRef(uid)
        avatarRef.delete().addOnSuccessListener {
            Log.d("Avatar storage", "Deleted avatar")
        }.addOnFailureListener {
            Log.e("Avatar storage", "Error while deleting avatar. Avatar does not exist in storage.")
        }
    }

    // https://firebase.google.com/docs/storage/android/upload-files#upload_from_data_in_memory
    fun uploadAvatar(capturedImage: Bitmap, uid: String, callback: (String?) -> Unit) {
        // TODO : Put in constants
        val IMAGE_MAX_FILE_SIZE: Long = 1024 * 1024

        val baos = ByteArrayOutputStream()
        capturedImage.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val byteArray = baos.toByteArray()

        if (byteArray.size > IMAGE_MAX_FILE_SIZE) {
            Log.e("Avatar storage", "File too large")
            callback(null)
            return
        }

        val imageRef = getAvatarRef(uid)

        val uploadTask = imageRef.putBytes(byteArray)

        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                callback(imageUrl)
                Log.d("Avatar storage", "Image uploaded successfully! URL: $imageUrl")
            }
        }.addOnFailureListener { exception ->
            callback(null)
            // TODO : Handle failure
            Log.e("Avatar storage", "Error uploading image: ${exception.message}")
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
