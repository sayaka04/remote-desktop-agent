package io.github.sayaka04.androidremoteclient.ui.command

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun CommandScreen(commandViewModel: CommandViewModel = viewModel()){

    val context = LocalContext.current

    val commandState by commandViewModel.state.collectAsState()

    var showDialog by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Commands",
                style = MaterialTheme.typography.headlineMedium
            )


            Column(Modifier.weight(1f)){
                Row()
                {
                    Text(
                        text = "Mouse Click",
                    )
                    Button(
                        onClick = {
                            commandViewModel.insertIntoActionClick(ClickType.LEFT)
                            Toast.makeText(
                                context,
                                "Hello, ${ClickType.MIDDLE}.",
                                Toast.LENGTH_SHORT
                            ).show()

                        },
                    ) {
                        Text("Left")
                    }
                    Button(
                        onClick = {
                            commandViewModel.insertIntoActionClick(ClickType.MIDDLE)
                            Toast.makeText(
                                context,
                                "Hello, ${ClickType.MIDDLE}.",
                                Toast.LENGTH_SHORT
                            ).show()

                        },
                    ) {
                        Text("Middle")
                    }
                    Button(
                        onClick = {
                            commandViewModel.insertIntoActionClick(ClickType.RIGHT)
                            Toast.makeText(
                                context,
                                "Hello, ${ClickType.RIGHT}.",
                                Toast.LENGTH_SHORT
                            ).show()

                        },
                    ) {
                        Text("Right")
                    }
                }
            }


            Column(Modifier.weight(1f)) {
                Text(
                    text = "Keyboard Type",
                )
                OutlinedTextField(
                    value = commandState.textFieldInput,
                    onValueChange = { commandViewModel.updateTextField(it) },
                    label = { Text("Text") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        commandViewModel.insertIntoActionTypeText(commandState.textFieldInput)
                        Toast.makeText(
                            context,
                            "Hello, ${commandState.textFieldInput}.",
                            Toast.LENGTH_SHORT
                        ).show()

                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Submit")
                }
            }



            Column(Modifier.weight(1f)) {
                Button(
                    onClick = {
                        Toast.makeText(
                            context,
                            "Command Actions:\n" + commandViewModel.getCommandActionLists(),
                            Toast.LENGTH_SHORT
                        ).show()
                        showDialog = true

                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Read from ArrayList")
                }
            }
        }
    }

    if (showDialog) {

        AlertDialog(

            onDismissRequest = {
                showDialog = false
            },

            title = {
                Text("Commands")
            },

            text = {
                Text(
                    commandViewModel.getCommandActionLists()
                )
            },

            confirmButton = {

                Button(
                    onClick = {
                        showDialog = false
                    }
                ) {

                    Text("OK")
                }
            }
        )
    }

}