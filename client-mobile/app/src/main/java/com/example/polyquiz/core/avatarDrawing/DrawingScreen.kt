package com.example.polyquiz.core.avatarDrawing

import android.graphics.Bitmap
import android.graphics.Paint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.polyquiz.R
import com.example.polyquiz.ui.features.camera.CameraViewModel
import com.plcoding.drawinginjetpackcompose.CanvasControls
import com.plcoding.drawinginjetpackcompose.DrawingAction
import com.plcoding.drawinginjetpackcompose.DrawingCanvas
import com.plcoding.drawinginjetpackcompose.DrawingViewModel
import com.plcoding.drawinginjetpackcompose.PathData
import com.plcoding.drawinginjetpackcompose.allColors


@Composable
fun DrawingScreen(viewModel: DrawingViewModel, navigateToUserEdit: () -> Unit, uid: String, cameraViewModel: CameraViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current



    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Center, // Center children horizontally
        verticalAlignment = Alignment.CenterVertically // Center children vertically
    ) {
        var canvasSize by remember { mutableStateOf(IntSize.Zero) }

            DrawingCanvas(
                paths = state.paths,
                currentPath = state.currentPath,
                onAction = viewModel::onAction,
                modifier = Modifier
                    .size(800.dp)
                    .onSizeChanged { canvasSize = it }
            )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            CanvasControls(
                selectedColor = state.selectedColor,
                colors = allColors,
                onSelectColor = { viewModel.onAction(DrawingAction.OnSelectColor(it)) },
                onClearCanvas = { viewModel.onAction(DrawingAction.OnClearCanvasClick) }
            )

            Button(onClick = {
                if (canvasSize.width > 0 && canvasSize.height > 0) {
                    val bitmap = captureCanvasAsBitmap(state.paths, canvasSize.width, canvasSize.height)
                    cameraViewModel.setDrawingAvatar(bitmap)
                    navigateToUserEdit()
                }
            }) {
                Text(stringResource(R.string.save_canvas))
            }

            Button(onClick = navigateToUserEdit) {
                Text(text = stringResource(R.string.return_to_user_edit))
            }
        }

    }

}
fun captureCanvasAsBitmap(paths: List<PathData>, width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    canvas.drawColor(Color.White.toArgb())

    val paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }

    for (pathData in paths) {
        paint.color = pathData.color.toArgb()
        val path = android.graphics.Path()

        if (pathData.path.isNotEmpty()) {
            path.moveTo(pathData.path.first().x, pathData.path.first().y)
            pathData.path.forEach { point ->
                path.lineTo(point.x, point.y)
            }
            canvas.drawPath(path, paint)
        }
    }

    return bitmap
}
