package io.github.sayaka04.androidremoteclient.ui.archive

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import io.github.sayaka04.androidremoteclient.ui.archive.model.ImageCoordinate

@Composable
fun MapCoordinateScreen() {

    var isSelectionMode by remember { mutableStateOf(false) }
    var lastCoordinate by remember { mutableStateOf<ImageCoordinate?>(null) }
    var resetTrigger by remember { mutableStateOf(0) }

    val context = LocalContext.current

    val painter = rememberAsyncImagePainter(
        "https://cdn.hswstatic.com/gif/maps.jpg"
    )

    Box(modifier = Modifier.fillMaxSize()) {

        // MAP
        ZoomableMapView(
            modifier = Modifier.fillMaxSize(),
            painter = painter,
            isSelectionMode = isSelectionMode,
            resetTrigger = resetTrigger,
            onCoordinateSelected = {
                lastCoordinate = it
            }
        )

        // TOP BAR
        TopControlBar(
            isSelectionMode = isSelectionMode,
            lastCoordinate = lastCoordinate,

            onToggleMode = {
                isSelectionMode = !isSelectionMode
            },

            onReset = {
                resetTrigger++
                lastCoordinate = null
            },

            onSet = {
                lastCoordinate?.let {
                    Toast.makeText(
                        context,
                        "(${it.imagePixelX.toInt()}, ${it.imagePixelY.toInt()})",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }
}

@Composable
fun TopControlBar(
    isSelectionMode: Boolean,
    lastCoordinate: ImageCoordinate?,

    onToggleMode: () -> Unit,
    onReset: () -> Unit,
    onSet: () -> Unit,
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {

            Button(
                onClick = onToggleMode,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    if (isSelectionMode)
                        "Selecting"
                    else
                        "Panning"
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onReset,
                modifier = Modifier.weight(1f)
            ) {
                Text("Reset")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onSet,
                modifier = Modifier.weight(1f)
            ) {
                Text("Set")
            }
        }

        lastCoordinate?.let {

            Text(
                text = "X: ${(it.percentX * 100).toInt()}% " +
                        "Y: ${(it.percentY * 100).toInt()}%",
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}