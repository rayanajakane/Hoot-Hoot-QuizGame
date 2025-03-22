package com.example.polyquiz.ui.features.camera.photo_capture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.polyquiz.auth.domain.AuthState
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.ui.features.camera.CameraState
import com.example.polyquiz.ui.features.camera.CameraViewModel
import java.util.concurrent.Executor

@Composable
fun CameraScreen(
    authViewModel: AuthViewModel,
    cameraViewModel: CameraViewModel,
    navigateToUserEdit: () -> Unit,
    navigateToSignup: () -> Unit
) {
    val cameraState: CameraState by cameraViewModel.state.collectAsStateWithLifecycle()

    Log.d("CameraScreen", "Current camera state: $cameraState")

    if (cameraState.capturedImage == null) {
        CameraContent(onPhotoCaptured = cameraViewModel::updateCapturedPhotoState)
    } else {
        ImagePreview(
            capturedImage = cameraState.capturedImage!!,
            onRetake = { cameraViewModel.updateCapturedPhotoState(null) },
            onSave = {
                if(authViewModel.authState.value === AuthState.Authenticated) {
                    navigateToUserEdit()
                } else {
                    navigateToSignup()
                }
            }
        )
    }
}

// TODO : Camera content for QR code only?

@Composable
fun CameraContent(onPhotoCaptured: (Bitmap) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember { LifecycleCameraController(context) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = "Take photo") },
                onClick = {
                    capturePhoto(context, cameraController, onPhotoCaptured)
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "Camera capture icon"
                    )
                }
            )
        }
    ) { paddingValues: PaddingValues ->

        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            factory = { context ->
                PreviewView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)

                    setBackgroundColor(Color.BLACK)
                    scaleType = PreviewView.ScaleType.FILL_START

                }.also { previewView ->
                    previewView.controller = cameraController
                    cameraController.bindToLifecycle(lifecycleOwner)

                }
            })
    }
}

@Composable
fun ImagePreview(capturedImage: Bitmap, onRetake: () -> Unit, onSave: (Bitmap) -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CircularImagePreview(
                capturedImage = capturedImage, modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                ExtendedFloatingActionButton(
                    text = { Text(text = "Retake Photo") },
                    onClick = onRetake,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = "Retake photo icon"
                        )
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                )

                ExtendedFloatingActionButton(
                    text = { Text(text = "Save Photo") },
                    onClick = { onSave(capturedImage) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save photo icon"
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun CircularImagePreview(
    capturedImage: Bitmap,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .size(500.dp)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
    ) {
        Image(
            bitmap = capturedImage.asImageBitmap(),
            contentDescription = "Circular avatar",
            modifier = Modifier.clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}


private fun capturePhoto(
    context: Context,
    cameraController: LifecycleCameraController,
    onPhotoCaptured: (Bitmap) -> Unit
) {
    val mainExecutor: Executor = ContextCompat.getMainExecutor(context)

    cameraController.takePicture(mainExecutor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            val correctedBitmap: Bitmap = image
                .toBitmap()
//                .rotateBitmap(image.imageInfo.rotationDegrees)

            onPhotoCaptured(correctedBitmap)
            image.close()
        }

        override fun onError(exception: ImageCaptureException) {
            Log.e("CameraContent", "Error capturing image", exception)
        }
    })
}

// https://www.youtube.com/watch?v=LRWkQtxGe0E
private fun Bitmap.rotateBitmap(rotationDegrees: Int): Bitmap {
    val matrix = Matrix().apply {
        postRotate(-rotationDegrees.toFloat())
        postScale(-1f, -1f)
    }

    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}
