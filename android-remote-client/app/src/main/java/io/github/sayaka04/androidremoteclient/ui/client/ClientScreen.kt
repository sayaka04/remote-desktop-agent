package io.github.sayaka04.androidremoteclient.ui.client

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.sayaka04.androidremoteclient.ui.command.ClickType
import io.github.sayaka04.androidremoteclient.util.PreferenceDatastoreUtil
import kotlinx.coroutines.launch

@Composable
fun ClientScreen(clientViewModel: ClientViewModel = viewModel()) {

    val context = LocalContext.current

    val clientState by clientViewModel.state.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    var showDialog by remember {
        mutableStateOf(false)
    }

    var showDialog2 by remember {
        mutableStateOf(false)
    }
    var datastoreContent by remember {
        mutableStateOf("")
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
                text = "Config",
                style = MaterialTheme.typography.headlineMedium
            )

            Column(Modifier.weight(1f)) {
                Text(
                    text = "Client Details",
                )
                OutlinedTextField(
                    value = clientState.apiBaseURL,
                    onValueChange = { clientViewModel.updateApiBaseUrl(it) },
                    label = { Text("API Base URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = clientState.apiDeviceId,
                    onValueChange = { clientViewModel.updateApiDeviceId(it) },
                    label = { Text("Device ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                        value = clientState.apiCommandId,
                onValueChange = { clientViewModel.updateApiCommandId(it) },
                label = { Text("Command ID") },
                modifier = Modifier.fillMaxWidth()
                )

            }



            Column(Modifier.weight(1f)) {
                Button(
                    onClick = {
                        coroutineScope.launch {

                            PreferenceDatastoreUtil.saveString(
                                context = context,
                                key = "apiBaseURL",
                                value = clientState.apiBaseURL
                            )

                            PreferenceDatastoreUtil.saveString(
                                context = context,
                                key = "apiDeviceId",
                                value = clientState.apiDeviceId
                            )

                            PreferenceDatastoreUtil.saveString(
                                context = context,
                                key = "apiCommandId",
                                value = clientState.apiCommandId
                            )

                            showDialog = true

                        }
                        showDialog = true

                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }

                Button(
                    onClick = {
                        coroutineScope.launch {

                            datastoreContent = PreferenceDatastoreUtil.fetchAll(context)

                            showDialog2 = true

                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Read from Datastore")
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
                Text("Config Client Details")
            },

            text = {
                Text(
                    clientViewModel.getClientDetails()
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


    if (showDialog2) {


        AlertDialog(

            onDismissRequest = {
                showDialog2 = false
            },

            title = {
                Text("From Datastore print test")
            },

            text = {
                Text(
                    datastoreContent
                )
            },

            confirmButton = {

                Button(
                    onClick = {
                        showDialog2 = false
                    }
                ) {

                    Text("OK")
                }
            }
        )
    }

}