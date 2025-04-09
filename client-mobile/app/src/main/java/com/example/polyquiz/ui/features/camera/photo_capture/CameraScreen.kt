package com.example.polyquiz.ui.features.camera.photo_capture

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.util.Log
import android.util.Size
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SwitchCamera
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.polyquiz.R
import com.example.polyquiz.auth.domain.AuthState
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.ui.features.camera.CameraState
import com.example.polyquiz.ui.features.camera.CameraViewModel
import com.example.polyquiz.ui.features.camera.QrCodeAnalyzer
import java.util.concurrent.Executor


@Composable
fun CameraScreen(
    authViewModel: AuthViewModel,
    cameraViewModel: CameraViewModel,
    navigateToUserEdit: () -> Unit,
    navigateToJoinRoom: () -> Unit,
    navigateToSignup: () -> Unit
) {
    val cameraState: CameraState by cameraViewModel.state.collectAsStateWithLifecycle()
    val showQrCode: Boolean by cameraViewModel.showQrContent.collectAsState()
    val scannedCode by cameraViewModel.scannedCode.collectAsState()

    // Temp workaround: Set code back to null when opening QR camera
    LaunchedEffect(showQrCode) {
        if (showQrCode) {
            cameraViewModel.setScannedCode(null)
        }
    }

    LaunchedEffect(scannedCode) {
        scannedCode?.let {
            Log.d("Qr", "Scanned code $scannedCode and navigated back to join page")
            navigateToJoinRoom()
        }
    }



    Log.d("CameraScreen", "Current camera state: $cameraState")

    if (showQrCode) {
        QRCameraContent(onQrScanned = cameraViewModel::setScannedCode)
    } else {
        if (cameraState.capturedImage == null) {
            CameraContent(onPhotoCaptured = cameraViewModel::updateCapturedPhotoState)
        } else {
            ImagePreview(
                capturedImage = cameraState.capturedImage!!,
                onRetake = { cameraViewModel.updateCapturedPhotoState(null) },
                onSave = {
                    if (authViewModel.authState.value === AuthState.Authenticated) {
                        navigateToUserEdit()
                    } else {
                        Log.d("CameraViewModel", cameraViewModel.state.value.toString())
                        navigateToSignup()
                    }
                }
            )
        }
    }

}

@Composable
fun QRCameraContent(onQrScanned: (String) -> Unit) {
    var code by remember { mutableStateOf("") }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var hasCamPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCamPermission = granted
        }
    )
    // Only do once
    LaunchedEffect(key1 = true) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (hasCamPermission) {
            AndroidView(
                factory = { context ->
                    val previewView = PreviewView(context)
                    val preview = Preview.Builder().build()
                    val selector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(Size(previewView.width, previewView.height))
                        // If analyze is slower than fps, what should we do?
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(
                        ContextCompat.getMainExecutor(context),
                        QrCodeAnalyzer { result ->
                            code = result
                            onQrScanned(result)
                            Log.d("QR", "Seeing code: $code")
                        }
                    )

                    try {
                        cameraProviderFuture.get().bindToLifecycle(
                            lifecycleOwner,
                            selector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    previewView
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

    }

}


@Composable
fun CameraContent(onPhotoCaptured: (Bitmap) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isFrontCamera = remember { mutableStateOf(false) }
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

        ExtendedFloatingActionButton(
            modifier = Modifier
                .padding(16.dp),
            text = { Text(text = stringResource(R.string.switch_camera)) },
            onClick = {
                isFrontCamera.value = !isFrontCamera.value
                cameraController.cameraSelector =
                    if (isFrontCamera.value) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }
                cameraController.bindToLifecycle(lifecycleOwner)
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.SwitchCamera,
                    contentDescription = stringResource(R.string.switch_camera)
                )
            }
        )
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
