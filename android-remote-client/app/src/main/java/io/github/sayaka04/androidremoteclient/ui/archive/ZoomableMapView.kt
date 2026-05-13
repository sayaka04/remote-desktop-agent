package io.github.sayaka04.androidremoteclient.ui.archive

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import io.github.sayaka04.androidremoteclient.ui.archive.model.ImageCoordinate

import kotlin.math.max
import kotlin.math.min


// -------------------------
// ZOOMABLE MAP COMPONENT
// -------------------------

@Composable
fun ZoomableMapView(
    modifier: Modifier = Modifier,
    painter: Painter,
    isSelectionMode: Boolean,
    resetTrigger: Int = 0,
    onCoordinateSelected: (ImageCoordinate) -> Unit
) {

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var selectedPercent by remember { mutableStateOf<Offset?>(null) }

    var boxWidth by remember { mutableFloatStateOf(0f) }
    var boxHeight by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(resetTrigger) {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
        selectedPercent = null
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .onSizeChanged {
                boxWidth = it.width.toFloat()
                boxHeight = it.height.toFloat()
            }
    ) {

        val updateSelection: (Offset) -> Unit = { tapOffset ->

            if (boxWidth > 0f && boxHeight > 0f) {

                val imgSize = painter.intrinsicSize

                if (imgSize != Size.Unspecified &&
                    imgSize.width > 0 &&
                    imgSize.height > 0
                ) {

                    val baseScale =
                        min(boxWidth / imgSize.width, boxHeight / imgSize.height)

                    val dispW = imgSize.width * baseScale
                    val dispH = imgSize.height * baseScale

                    val marginX = (boxWidth - dispW) / 2f
                    val marginY = (boxHeight - dispH) / 2f

                    val dx = tapOffset.x - boxWidth / 2
                    val dy = tapOffset.y - boxHeight / 2

                    val unzoomedDx = (dx - offsetX) / scale
                    val unzoomedDy = (dy - offsetY) / scale

                    val boxX = unzoomedDx + boxWidth / 2
                    val boxY = unzoomedDy + boxHeight / 2

                    val imgX = boxX - marginX
                    val imgY = boxY - marginY

                    val pctX = (imgX / dispW).coerceIn(0f, 1f)
                    val pctY = (imgY / dispH).coerceIn(0f, 1f)

                    selectedPercent = Offset(pctX, pctY)

                    onCoordinateSelected(
                        ImageCoordinate(
                            screenX = tapOffset.x,
                            screenY = tapOffset.y,
                            imagePixelX = pctX * imgSize.width,
                            imagePixelY = pctY * imgSize.height,
                            percentX = pctX,
                            percentY = pctY,
                            scale = scale,
                            panX = offsetX,
                            panY = offsetY
                        )
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isSelectionMode, scale, offsetX, offsetY) {
                    if (isSelectionMode) {
                        detectDragGestures(
                            onDragStart = { updateSelection(it) },
                            onDrag = { change, _ ->
                                updateSelection(change.position)
                                change.consume()
                            }
                        )
                    }
                }

                .pointerInput(isSelectionMode, scale, offsetX, offsetY) {
                    if (isSelectionMode) {
                        detectTapGestures { updateSelection(it) }
                    } else {
                        detectTapGestures(
                            onDoubleTap = {
                                scale = 1f
                                offsetX = 0f
                                offsetY = 0f
                            }
                        )
                    }
                }

                .pointerInput(isSelectionMode) {
                    if (!isSelectionMode) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (scale * zoom).coerceIn(1f, 10f)

                            val maxX = max(0f, (boxWidth * newScale - boxWidth) / 2)
                            val maxY = max(0f, (boxHeight * newScale - boxHeight) / 2)

                            scale = newScale
                            offsetX = (offsetX + pan.x).coerceIn(-maxX, maxX)
                            offsetY = (offsetY + pan.y).coerceIn(-maxY, maxY)
                        }
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

//            Canvas(modifier = Modifier.fillMaxSize()) {
//
//                selectedPercent?.let { pct ->
//
//                    val imgSize = painter.intrinsicSize
//
//                    if (imgSize != Size.Unspecified) {
//
//                        val baseScale =
//                            min(size.width / imgSize.width, size.height / imgSize.height)
//
//                        val dispW = imgSize.width * baseScale
//                        val dispH = imgSize.height * baseScale
//
//                        val marginX = (size.width - dispW) / 2f
//                        val marginY = (size.height - dispH) / 2f
//
//                        val imgX = pct.x * dispW
//                        val imgY = pct.y * dispH
//
//                        val boxX = imgX + marginX
//                        val boxY = imgY + marginY
//
//                        val unzoomedDx = boxX - size.width / 2
//                        val unzoomedDy = boxY - size.height / 2
//
//                        val dx = unzoomedDx * scale + offsetX
//                        val dy = unzoomedDy * scale + offsetY
//
//                        val screenX = dx + size.width / 2f
//                        val screenY = dy + size.height / 2f
//
//                        val center = Offset(screenX, screenY)
//
//                        drawCircle(Color.Cyan, 10.dp.toPx(), center, style = Stroke(2.dp.toPx()))
//                        drawCircle(Color.Red, 3.dp.toPx(), center)
//                    }
//                }
//            }

            canvasHandler(
                scale = scale,
                painter = painter,
                offsetX = offsetX,
                offsetY = offsetY,
                selectedPercent = selectedPercent,
            )

        }

    }
}




@Composable
fun canvasHandler(
    scale: Float,
    painter: Painter,
    offsetX: Float,
    offsetY: Float,
    selectedPercent: Offset?
){
    Canvas(modifier = Modifier.fillMaxSize()) {

        selectedPercent?.let { pct ->

            val imgSize = painter.intrinsicSize

            if (imgSize != Size.Unspecified) {

                val baseScale =
                    min(size.width / imgSize.width, size.height / imgSize.height)

                val dispW = imgSize.width * baseScale
                val dispH = imgSize.height * baseScale

                val marginX = (size.width - dispW) / 2f
                val marginY = (size.height - dispH) / 2f

                val imgX = pct.x * dispW
                val imgY = pct.y * dispH

                val boxX = imgX + marginX
                val boxY = imgY + marginY

                val unzoomedDx = boxX - size.width / 2
                val unzoomedDy = boxY - size.height / 2

                val dx = unzoomedDx * scale + offsetX
                val dy = unzoomedDy * scale + offsetY

                val screenX = dx + size.width / 2f
                val screenY = dy + size.height / 2f

                val center = Offset(screenX, screenY)

                drawCircle(Color.Cyan, 10.dp.toPx(), center, style = Stroke(2.dp.toPx()))
                drawCircle(Color.Red, 3.dp.toPx(), center)
            }
        }
    }
}

