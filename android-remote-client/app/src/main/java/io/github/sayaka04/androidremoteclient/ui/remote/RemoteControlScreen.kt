package io.github.sayaka04.androidremoteclient.ui.remote

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.sayaka04.androidremoteclient.api.ApiClient
import io.github.sayaka04.androidremoteclient.ui.commands.Action
import io.github.sayaka04.androidremoteclient.ui.remote.components.*
import io.github.sayaka04.androidremoteclient.util.PreferenceDatastoreUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun RemoteControlScreen(
    deviceId: String,
    viewModel: RemoteViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var screenshotPath by remember { mutableStateOf<String?>(null) }
    var lastUpdatedAt by remember { mutableStateOf("") }
    var timestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var savedBaseUrl by remember { mutableStateOf("") }
    var isFullscreen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        savedBaseUrl = PreferenceDatastoreUtil.getString(context, "base_url") ?: ""
    }

    LaunchedEffect(deviceId) {
        while (isActive) {
            try {
                val response = ApiClient.service.requestHostData(deviceId)
                if (response.isSuccessful && response.body()?.data != null) {
                    val hostData = response.body()!!.data!!
                    val newPath = hostData.screenshotPath
                    val newUpdateStr = hostData.updatedAt ?: ""

                    if (newUpdateStr != lastUpdatedAt || newPath != screenshotPath) {
                        screenshotPath = newPath
                        lastUpdatedAt = newUpdateStr
                        timestamp = System.currentTimeMillis()
                    }
                }
            } catch (e: Exception) {
                Log.e("RemoteControl", "Stream refresh failed: ${e.message}")
            }
            delay(state.pollIntervalMs)
        }
    }

    val fullImageUrl = remember(screenshotPath, timestamp, savedBaseUrl) {
        if (!screenshotPath.isNullOrBlank() && savedBaseUrl.isNotBlank()) {
            val domain = savedBaseUrl.removeSuffix("/api/").removeSuffix("/api")
            "$domain/storage/$screenshotPath?t=$timestamp"
        } else ""
    }

    val forceRefresh = {
        viewModel.forceRefreshScreenshot(deviceId)
        viewModel.resetPollingTimer()
        timestamp = System.currentTimeMillis()
    }

    if (isFullscreen) {
        Dialog(
            onDismissRequest = { isFullscreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            RemoteControlContent(deviceId, fullImageUrl, viewModel, state, true, forceRefresh) { isFullscreen = false }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            RemoteControlContent(deviceId, fullImageUrl, viewModel, state, false, forceRefresh) { isFullscreen = true }
        }
    }
}

@Composable
private fun RemoteControlContent(
    deviceId: String,
    fullImageUrl: String,
    viewModel: RemoteViewModel,
    state: RemoteState,
    isFullscreen: Boolean,
    onForceRefresh: () -> Unit,
    onToggleFullscreen: () -> Unit
) {
    var isComposerOpen by remember { mutableStateOf(false) }
    val shape = if (isFullscreen) RoundedCornerShape(0.dp) else RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black, shape)
            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, shape)
    ) {
        InteractiveStreamViewer(
            fullImageUrl = fullImageUrl,
            actions = state.actions,
            onPositionSelected = { pctX, pctY ->
                viewModel.addAction(Action.Move(pctX, pctY))
                if (!isComposerOpen) isComposerOpen = true
            }
        )

        TopNavBar(
            isFullscreen = isFullscreen,
            onForceRefresh = onForceRefresh,
            onToggleFullscreen = onToggleFullscreen,
            modifier = Modifier.align(Alignment.TopStart)
        )

        ComposerFab(
            isComposerOpen = isComposerOpen,
            actionCount = state.actions.size,
            onClick = { isComposerOpen = !isComposerOpen },
            modifier = Modifier.align(Alignment.BottomEnd)
        )

        if (isComposerOpen) {
            ActionComposerCard(
                deviceId = deviceId,
                state = state,
                viewModel = viewModel,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 90.dp, end = 24.dp)
            )
        }
    }
}