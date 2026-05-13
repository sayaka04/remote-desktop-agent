package io.github.sayaka04.androidremoteclient.ui.host.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.sayaka04.androidremoteclient.ui.host.HostState

//  -------------------------------------------------
//  UI control panel for HostScreen interaction.
//  Handles user actions for selection mode toggle, reset, and set coordinate,
//  and displays the current pointer/selection coordinates from HostState.
//  -------------------------------------------------

@Composable
fun HostControlPanel(
    //Formerly lastCoordinate
    hostState: HostState,

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
                    if (hostState.isSelectionMode)
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


        Text(
            text = "X: ${(hostState.percentX * 100).toInt()}% " +
                    "Y: ${(hostState.percentY * 100).toInt()}%",
            modifier = Modifier.padding(top = 8.dp)
        )

    }
}