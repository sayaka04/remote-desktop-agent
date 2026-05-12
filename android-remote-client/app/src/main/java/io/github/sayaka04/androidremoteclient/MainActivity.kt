package io.github.sayaka04.androidremoteclient

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import kotlin.math.max
import kotlin.math.min

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val composeView = findViewById<ComposeView>(R.id.compose_view)

        composeView.setContent {
            MaterialTheme {
                ImageAnnotationDemo()
            }
        }
    }
}

data class CoordinateData(
    val screenX: Float,
    val screenY: Float,
    val imagePixelX: Float,
    val imagePixelY: Float,
    val percentX: Float,
    val percentY: Float,
    val scale: Float,
    val panX: Float,
    val panY: Float
)

@Composable
fun ImageAnnotationDemo() {
    var isSelectionMode by remember { mutableStateOf(false) }
    var lastCoordinate by remember { mutableStateOf<CoordinateData?>(null) }
    var resetTrigger by remember { mutableStateOf(0) }

    val context = LocalContext.current

    // Stable Wikimedia URL for prototyping
    val mapImageUrl = "https://cdn.hswstatic.com/gif/maps.jpg"
    val painter = rememberAsyncImagePainter(model = mapImageUrl)

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Button(
                onClick = { isSelectionMode = !isSelectionMode },
                modifier = Modifier.weight(1f).padding(end = 4.dp)
            ) {
                Text(if (isSelectionMode) "Selecting" else "Panning")
            }

            Button(
                onClick = {
                    resetTrigger++
                    lastCoordinate = null
                },
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
            ) {
                Text("Reset")
            }

            Button(
                onClick = {
                    if (lastCoordinate != null) {
                        val pctX = (lastCoordinate!!.percentX * 100).toInt()
                        val pctY = (lastCoordinate!!.percentY * 100).toInt()
                        val imgX = lastCoordinate!!.imagePixelX.toInt()
                        val imgY = lastCoordinate!!.imagePixelY.toInt()

                        val message = "Pixels: ($imgX, $imgY)\nPercentage: ($pctX%, $pctY%)"
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Tap the map to pin first!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            ) {
                Text("Set")
            }
        }

        lastCoordinate?.let { coord ->
            Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                Text("Screen Tap: (${coord.screenX.toInt()}, ${coord.screenY.toInt()})")
                Text("Map Percent: X:${(coord.percentX * 100).toInt()}% Y:${(coord.percentY * 100).toInt()}%")
            }
        }

        ZoomableCoordinateImagePicker(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp),
            painter = painter,
            isSelectionMode = isSelectionMode,
            resetTrigger = resetTrigger,
            onCoordinateSelected = { coord ->
                lastCoordinate = coord
            }
        )
    }
}

@Composable
fun ZoomableCoordinateImagePicker(
    modifier: Modifier = Modifier,
    painter: Painter,
    isSelectionMode: Boolean,
    resetTrigger: Int = 0,
    onCoordinateSelected: (CoordinateData) -> Unit
) {
    val context = LocalContext.current

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var selectedPercent by remember { mutableStateOf<Offset?>(null) }

    LaunchedEffect(resetTrigger) {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
        selectedPercent = null
    }

    BoxWithConstraints(
        modifier = modifier.clipToBounds()
    ) {
        val density = LocalDensity.current
        val boxW = with(density) { maxWidth.toPx() }
        val boxH = with(density) { maxHeight.toPx() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(boxW, boxH) {
                    detectTapGestures(
                        onDoubleTap = {
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        },
                        onTap = { tapOffset ->
                            if (!isSelectionMode) return@detectTapGestures

                            val imgIntrinsic = painter.intrinsicSize
                            if (imgIntrinsic == Size.Unspecified || imgIntrinsic.width <= 0f || imgIntrinsic.height <= 0f) {
                                Toast.makeText(context, "Image not fully loaded yet!", Toast.LENGTH_SHORT).show()
                                return@detectTapGestures
                            }

                            val baseScale = min(boxW / imgIntrinsic.width, boxH / imgIntrinsic.height)
                            val dispW = imgIntrinsic.width * baseScale
                            val dispH = imgIntrinsic.height * baseScale
                            val marginX = (boxW - dispW) / 2f
                            val marginY = (boxH - dispH) / 2f

                            val dx = tapOffset.x - boxW / 2
                            val dy = tapOffset.y - boxH / 2
                            val unzoomedDx = (dx - offsetX) / scale
                            val unzoomedDy = (dy - offsetY) / scale
                            val tapBoxX = unzoomedDx + boxW / 2
                            val tapBoxY = unzoomedDy + boxH / 2

                            val imgX = tapBoxX - marginX
                            val imgY = tapBoxY - marginY

                            val pctX = imgX / dispW
                            val pctY = imgY / dispH

                            if (pctX in 0f..1f && pctY in 0f..1f) {
                                selectedPercent = Offset(pctX, pctY)
                                onCoordinateSelected(
                                    CoordinateData(
                                        screenX = tapOffset.x,
                                        screenY = tapOffset.y,
                                        imagePixelX = pctX * imgIntrinsic.width,
                                        imagePixelY = pctY * imgIntrinsic.height,
                                        percentX = pctX,
                                        percentY = pctY,
                                        scale = scale,
                                        panX = offsetX,
                                        panY = offsetY
                                    )
                                )
                            }
                        }
                    )
                }
                .pointerInput(boxW, boxH) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(1f, 10f)
                        val maxX = max(0f, (boxW * newScale - boxW) / 2)
                        val maxY = max(0f, (boxH * newScale - boxH) / 2)

                        scale = newScale
                        offsetX = (offsetX + pan.x).coerceIn(-maxX, maxX)
                        offsetY = (offsetY + pan.y).coerceIn(-maxY, maxY)
                    }
                }
        ) {
            Image(
                painter = painter,
                contentDescription = "Map",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                selectedPercent?.let { pct ->
                    val imgIntrinsic = painter.intrinsicSize
                    if (imgIntrinsic != Size.Unspecified && imgIntrinsic.width > 0 && imgIntrinsic.height > 0) {
                        val canvasW = size.width
                        val canvasH = size.height

                        val baseScale = min(canvasW / imgIntrinsic.width, canvasH / imgIntrinsic.height)
                        val dispW = imgIntrinsic.width * baseScale
                        val dispH = imgIntrinsic.height * baseScale
                        val marginX = (canvasW - dispW) / 2f
                        val marginY = (canvasH - dispH) / 2f

                        val imgX = pct.x * dispW
                        val imgY = pct.y * dispH
                        val boxX = imgX + marginX
                        val boxY = imgY + marginY

                        val unzoomedDx = boxX - canvasW / 2
                        val unzoomedDy = boxY - canvasH / 2
                        val dx = unzoomedDx * scale + offsetX
                        val dy = unzoomedDy * scale + offsetY
                        val screenX = dx + canvasW / 2
                        val screenY = dy + canvasH / 2

                        val drawCenter = Offset(screenX, screenY)

                        drawCircle(color = Color.Cyan, radius = 8.dp.toPx(), center = drawCenter, style = Stroke(width = 2.dp.toPx()))
                        drawCircle(color = Color.Red, radius = 2.dp.toPx(), center = drawCenter)
                        drawLine(color = Color.Cyan, start = Offset(screenX - 16.dp.toPx(), screenY), end = Offset(screenX + 16.dp.toPx(), screenY), strokeWidth = 2.dp.toPx())
                        drawLine(color = Color.Cyan, start = Offset(screenX, screenY - 16.dp.toPx()), end = Offset(screenX, screenY + 16.dp.toPx()), strokeWidth = 2.dp.toPx())
                    }
                }
            }
        }
    }
}