package com.example.polyquiz.ui.features.camera

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.ui.features.camera.no_permission.NoPermissionScreen
import com.example.polyquiz.ui.features.camera.photo_capture.CameraScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainCameraScreen(
    authViewModel: AuthViewModel,
    cameraViewModel: CameraViewModel,
    navigateToUserEdit: () -> Unit
) {
    val cameraPermissionState: PermissionState =
        rememberPermissionState(android.Manifest.permission.CAMERA)

    MainContent(
        hasPermission = cameraPermissionState.status.isGranted,
        authViewModel,
        cameraViewModel,
        onRequestPermission = cameraPermissionState::launchPermissionRequest,
        navigateToUserEdit
    )
}

@Composable
private fun MainContent(
    hasPermission: Boolean,
    authViewModel: AuthViewModel,
    cameraViewModel: CameraViewModel,
    onRequestPermission: () -> Unit,
    navigateToUserEdit: () -> Unit
) {
    if (hasPermission) {
        CameraScreen(authViewModel, cameraViewModel, navigateToUserEdit)
    } else {
        NoPermissionScreen(onRequestPermission)
    }
}
