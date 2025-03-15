package com.example.polyquiz.ui.features.camera.photo_capture

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

@Composable
fun CameraScreen() {
    CameraContent()
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CameraContent() {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val cameraController = remember { LifecycleCameraController(context) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = "Take photo") },
                onClick = {
                    val mainExecutor = ContextCompat.getMainExecutor(context)
                    cameraController.takePicture(mainExecutor, object: ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            super.onCaptureSuccess(image)
                            // TODO : Process image
                            image.close()
                        }
                    })
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
                // init preview
                PreviewView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)

                    setBackgroundColor(Color.BLACK) // Only if camera not open yet + loading state
                    scaleType = PreviewView.ScaleType.FILL_START // Avoid cropping issues

                }.also { previewView ->
                    previewView.controller = cameraController
                    cameraController.bindToLifecycle(lifecycleOwner)

                }
            })
    }
}
