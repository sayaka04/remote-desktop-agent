package io.github.sayaka04.androidremoteclient.ui.command

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CommandScreen(commandViewModel: CommandViewModel = viewModel()) {
    val context = LocalContext.current
    val commandState by commandViewModel.state.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Commands",
                style = MaterialTheme.typography.headlineMedium
            )

            // 1. Mouse Click Section
            Column(Modifier.weight(1f)) {
                Text(text = "Mouse Click")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        commandViewModel.insertIntoActionClick(ClickType.LEFT)
                        Toast.makeText(context, "Added Left Click", Toast.LENGTH_SHORT).show()
                    }) { Text("Left") }

                    Button(onClick = {
                        commandViewModel.insertIntoActionClick(ClickType.MIDDLE)
                        Toast.makeText(context, "Added Middle Click", Toast.LENGTH_SHORT).show()
                    }) { Text("Middle") }

                    Button(onClick = {
                        commandViewModel.insertIntoActionClick(ClickType.RIGHT)
                        Toast.makeText(context, "Added Right Click", Toast.LENGTH_SHORT).show()
                    }) { Text("Right") }
                }
            }

            // 2. Keyboard Type Section
            Column(Modifier.weight(1f)) {
                Text(text = "Keyboard Type")
                OutlinedTextField(
                    value = commandState.textFieldInput,
                    onValueChange = { commandViewModel.updateTextField(it) },
                    label = { Text("Text") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        commandViewModel.insertIntoActionTypeText(commandState.textFieldInput)
                        Toast.makeText(context, "Added Text: ${commandState.textFieldInput}", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Submit Text Action")
                }
            }

            // 3. Network Action Section (NEW)
            Column(Modifier.weight(1.5f), verticalArrangement = Arrangement.spacedBy(8.dp)) {

                // Read Local ArrayList
                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Read from ArrayList")
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Send to Server
                Button(
                    onClick = { commandViewModel.sendCommandsToServer("1") }, // "1" is deviceId
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !commandState.isNetworkLoading
                ) {
                    Text("Send Actions to Server")
                }

                // Fetch from Server
                Button(
                    onClick = { commandViewModel.requestDataFromHost("1") }, // "1" is deviceId
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !commandState.isNetworkLoading
                ) {
                    Text("Get Host Response")
                }

                // Display Host Response
                if (commandState.hostMessage.isNotEmpty()) {
                    Text(
                        text = "Host Status: ${commandState.hostMessage}",
                        color = if (commandState.isHostTaskSuccessful) Color(0xFF2E7D32) else Color.Red,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }

    // Dialog for viewing current list
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Queued Commands") },
            text = { Text(commandViewModel.getCommandActionLists()) },
            confirmButton = {
                Button(onClick = { showDialog = false }) { Text("OK") }
            }
        )
    }
}