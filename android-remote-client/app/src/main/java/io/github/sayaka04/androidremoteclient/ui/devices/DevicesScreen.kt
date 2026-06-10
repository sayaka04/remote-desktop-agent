package io.github.sayaka04.androidremoteclient.ui.devices

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.sayaka04.androidremoteclient.api.Device

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    onDeviceClick: (String) -> Unit,
    onLogoutClick: () -> Unit,
    vm: DevicesViewModel = viewModel()
) {
    val devices by vm.devices.collectAsState()
    val loading by vm.isLoading.collectAsState()
    val refreshing by vm.isRefreshing.collectAsState()

    // Initial load
    LaunchedEffect(Unit) {
        Log.d("DevicesLog", "DevicesScreen Composed - Triggering initial fetchDevices()")
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                // 1. Initial Loading State
                loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                // 2. Empty State
                devices.isEmpty() -> {
                    EmptyDeviceState(onRefresh = { vm.fetchDevices(isRefresh = true) })
                }

                // 3. Populated List wrapped in the new PullToRefreshBox
                else -> {
                    PullToRefreshBox(
                        isRefreshing = refreshing,
                        onRefresh = { vm.fetchDevices(isRefresh = true) },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(devices) { device ->
                                DeviceCard(device = device, onClick = onDeviceClick)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENTS
// ==========================================

@Composable
private fun DeviceCard(
    device: Device,
    onClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val safeUuid = device.uuid ?: return@clickable
                onClick(safeUuid)
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Computer,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                // primary color adapts automatically now
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name ?: "Unknown Device",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (device.isOnline) "Online" else "Offline",
                    style = MaterialTheme.typography.bodyMedium,
                    // Keep green for online, but use standard error for offline
                    color = if (device.isOnline) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun EmptyDeviceState(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Computer,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Devices Found",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Make sure your target machine is online and connected.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRefresh) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Refresh")
        }
    }
}