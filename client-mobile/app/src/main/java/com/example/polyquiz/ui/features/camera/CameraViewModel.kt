package com.example.polyquiz.ui.features.camera

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.core.storage.ImageStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CameraViewModel : ViewModel() {
    private val _state = MutableStateFlow(CameraState())
    val state: StateFlow<CameraState> = _state

    fun updateCapturedPhotoState(updatedPhoto: Bitmap?) {
        _state.value = _state.value.copy(capturedImage = updatedPhoto)
    }

    private fun resetCapturedPhotoState() {
        _state.value = _state.value.copy(capturedImage = null)
    }

    fun saveCapturedImage(capturedImage: Bitmap, uid: String, authViewModel: AuthViewModel) {
        updateCapturedPhotoState(capturedImage)
        ImageStorage.uploadAvatar(capturedImage, uid) { url ->
            if (url != null) {
                authViewModel.updateAvatarUrl(url)
            } else {
                // TODO : Error handling
                Log.e("CameraViewModel", "Cannot save captured image")
            }
        }
        resetCapturedPhotoState()
    }

    override fun onCleared() {
        _state.value.capturedImage?.recycle()
        super.onCleared()
    }
}
