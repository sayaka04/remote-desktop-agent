package io.github.sayaka04.androidremoteclient.ui.devices

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    onDeviceClick: (String) -> Unit,
    onLogoutClick: () -> Unit,
    vm: DevicesViewModel = viewModel()
) {
    val devices by vm.devices.collectAsState()
    val loading by vm.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        Log.d("DevicesLog", "DevicesScreen Composed - Triggering fetchDevices()")
        vm.fetchDevices()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Target Device") },
                actions = {
                    IconButton(onClick = {
                        Log.d("DevicesLog", "Logout button clicked.")
                        onLogoutClick()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(Modifier.padding(padding).fillMaxSize()) {
                items(devices) { device ->
                    ListItem(
                        // PREVENTS THE CRASH
                        headlineContent = { Text(device.name ?: "Unknown Device") },
                        supportingContent = {
                            Text(if (device.isOnline) "Status: Online" else "Status: Offline")
                        },
                        modifier = Modifier.clickable {
                            val safeId = device.id ?: return@clickable
                            Log.d("DevicesLog", "User clicked on device ID: $safeId")
                            onDeviceClick(safeId)
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}