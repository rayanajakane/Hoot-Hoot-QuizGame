package com.example.polyquiz.ui.features.camera

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.core.storage.ImageStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.lang.Thread.State

class CameraViewModel : ViewModel() {
    private val _state = MutableStateFlow(CameraState())
    val state: StateFlow<CameraState> = _state

    private val _temporaryAvatar = MutableStateFlow<Bitmap?>(null)
    val temporaryAvatar: StateFlow<Bitmap?> get() = _temporaryAvatar

    private val _isPresetAvatar = MutableStateFlow<Boolean>(true)
    val isPresetAvatar: StateFlow<Boolean> = _isPresetAvatar

    fun setTemporaryAvatar(capturedImage: Bitmap?) {
        _temporaryAvatar.value = capturedImage
    }

    fun clearTemporaryAvatar() {
        _temporaryAvatar.value = null
    }

    fun updateCapturedPhotoState(updatedPhoto: Bitmap?) {
        Log.d("CameraViewModel", "Updated photo state to $updatedPhoto")
        _state.value = _state.value.copy(capturedImage = updatedPhoto)
        _isPresetAvatar.value = false
        setTemporaryAvatar(updatedPhoto)
    }

    fun setPresetAvatar(authViewModel: AuthViewModel, imageUrl: String) {
        Log.d("CameraViewModel", "Set avatar to preset")
        _isPresetAvatar.value = false
        authViewModel.setAvatarUrl(imageUrl)
    }

    private fun resetCapturedPhotoState() {
        _state.value = _state.value.copy(capturedImage = null)
        clearTemporaryAvatar()
        _isPresetAvatar.value = true
    }

    fun saveCapturedImage(capturedImage: Bitmap, uid: String, authViewModel: AuthViewModel, callback: (String?) -> Unit) {
        updateCapturedPhotoState(capturedImage)
        ImageStorage.uploadAvatar(capturedImage, uid) { url ->
            if (url != null) {
                authViewModel.setAvatarUrl(url)
                callback(url)
            } else {
                // TODO : Error handling
                Log.e("CameraViewModel", "Cannot save captured image")
                callback(null)
            }
        }
        resetCapturedPhotoState()
    }

    override fun onCleared() {
        _state.value.capturedImage?.recycle()
        super.onCleared()
    }
}
