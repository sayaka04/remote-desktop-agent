package io.github.sayaka04.androidremoteclient.ui.commands

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.sayaka04.androidremoteclient.api.DeviceCommand

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandsScreen(
    deviceUuid: String,
    onCommandClick: (String) -> Unit,
    onBackClick: () -> Unit,
    vm: CommandsViewModel = viewModel()
) {
    val commands by vm.commands.collectAsState()
    val loading by vm.isLoading.collectAsState()
    val refreshing by vm.isRefreshing.collectAsState()

    LaunchedEffect(deviceUuid) {
        Log.d("CommandsLog", "CommandsScreen Composed - Fetching commands for $deviceUuid")
        vm.fetchCommands(deviceUuid)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Commands") },
                navigationIcon = {
                    IconButton(onClick = {
                        Log.d("CommandsLog", "Back button clicked.")
                        onBackClick()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                commands.isEmpty() -> {
                    EmptyCommandState(onRefresh = { vm.fetchCommands(deviceUuid, isRefresh = true) })
                }

                // 3. Populated List wrapped in PullToRefreshBox
                else -> {
                    PullToRefreshBox(
                        isRefreshing = refreshing,
                        onRefresh = { vm.fetchCommands(deviceUuid, isRefresh = true) },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(commands) { cmd ->
                                CommandCard(cmd = cmd, onClick = onCommandClick)
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
private fun CommandCard(
    cmd: DeviceCommand,
    onClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val targetUuid = cmd.uuid ?: return@clickable
                Log.d("CommandsLog", "User clicked command ID: $targetUuid")
                onClick(targetUuid)
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
                imageVector = Icons.Default.Terminal,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cmd.name ?: "Unnamed Command",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = cmd.description ?: "No description provided",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyCommandState(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Terminal,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Commands Available",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "This device doesn't have any remote setups configured.",
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