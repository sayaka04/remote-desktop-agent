package io.github.sayaka04.androidremoteclient.ui.remote.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import io.github.sayaka04.androidremoteclient.ui.commands.Action
import kotlin.math.min

@Composable
fun InteractiveStreamViewer(
    fullImageUrl: String,
    actions: List<Action>,
    onPositionSelected: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var canvasHeight by remember { mutableFloatStateOf(0f) }

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(fullImageUrl)
            .crossfade(true)
            .build()
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                canvasWidth = coords.size.width.toFloat()
                canvasHeight = coords.size.height.toFloat()
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, gesturePan, gestureZoom, _ ->
                    scale = (scale * gestureZoom).coerceIn(1f, 5f)
                    if (scale > 1f) {
                        offsetX += gesturePan.x
                        offsetY += gesturePan.y
                    } else {
                        offsetX = 0f
                        offsetY = 0f
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (canvasWidth > 0 && canvasHeight > 0 && painter.state is AsyncImagePainter.State.Success) {
                        val imgSize = painter.intrinsicSize
                        if (imgSize != Size.Unspecified && imgSize.width > 0) {
                            val baseScale = min(canvasWidth / imgSize.width, canvasHeight / imgSize.height)
                            val dispW = imgSize.width * baseScale
                            val dispH = imgSize.height * baseScale
                            val marginX = (canvasWidth - dispW) / 2f
                            val marginY = (canvasHeight - dispH) / 2f

                            val unzoomedDx = (offset.x - canvasWidth / 2f - offsetX) / scale
                            val unzoomedDy = (offset.y - canvasHeight / 2f - offsetY) / scale
                            val boxX = unzoomedDx + canvasWidth / 2f
                            val boxY = unzoomedDy + canvasHeight / 2f

                            val imgX = boxX - marginX
                            val imgY = boxY - marginY

                            val pctX = (imgX / dispW).coerceIn(0f, 1f)
                            val pctY = (imgY / dispH).coerceIn(0f, 1f)

                            onPositionSelected(pctX, pctY)
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(scaleX = scale, scaleY = scale, translationX = offsetX, translationY = offsetY)
        ) {
            if (fullImageUrl.isNotBlank()) {
                Image(
                    painter = painter,
                    contentDescription = "Live Remote Screencast",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text("Awaiting live stream...", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
            }

            if (painter.state is AsyncImagePainter.State.Success && canvasWidth > 0) {
                val imgSize = painter.intrinsicSize
                if (imgSize != Size.Unspecified && imgSize.width > 0) {
                    val baseScale = min(canvasWidth / imgSize.width, canvasHeight / imgSize.height)
                    val dispW = imgSize.width * baseScale
                    val dispH = imgSize.height * baseScale
                    val marginX = (canvasWidth - dispW) / 2f
                    val marginY = (canvasHeight - dispH) / 2f

                    actions.forEachIndexed { index, action ->
                        if (action is Action.Move) {
                            val dotX = marginX + (action.x * dispW)
                            val dotY = marginY + (action.y * dispH)

                            Box(
                                modifier = Modifier
                                    .offset(
                                        x = with(density) { dotX.toDp() } - 12.dp,
                                        y = with(density) { dotY.toDp() } - 12.dp
                                    )
                                    .size(24.dp)
                                    .background(Color.Red, CircleShape)
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${index + 1}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}