package io.github.sayaka04.androidremoteclient.ui.remote

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.sayaka04.androidremoteclient.api.ApiClient
import io.github.sayaka04.androidremoteclient.api.ClientPayload
import io.github.sayaka04.androidremoteclient.api.CommandRequest
import io.github.sayaka04.androidremoteclient.api.NetworkAction
import io.github.sayaka04.androidremoteclient.ui.commands.Action
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Collections

class RemoteViewModel : ViewModel() {
    private val _state = MutableStateFlow(RemoteState())
    val state: StateFlow<RemoteState> = _state.asStateFlow()

    private var pollingJob: Job? = null
    private var lastActiveTime = System.currentTimeMillis()

    fun startPolling(deviceId: String) {
        if (pollingJob?.isActive == true) return

        pollingJob = viewModelScope.launch {
            while (true) {
                val timeSinceLastActive = System.currentTimeMillis() - lastActiveTime
                val interval = if (timeSinceLastActive > 60000) 5000L else 1000L

                _state.update { it.copy(pollIntervalMs = interval) }

                try {
                    val response = ApiClient.service.requestHostData(deviceId)
                    if (response.isSuccessful) {
                        val hostData = response.body()?.data
                        if (hostData != null) {
                            val baseUrl = "http://10.0.2.2:8000" // Adjust as needed
                            val fullImageUrl = hostData.screenshotPath?.let {
                                if (it.startsWith("http")) it else "$baseUrl/$it"
                            } ?: ""

                            val statusMsg = when {
                                hostData.hasClientRequest == true -> "Waiting for Host..."
                                hostData.hasHostResponse == true -> "Execution Complete"
                                else -> "Idle"
                            }

                            _state.update { it.copy(
                                imageUrl = fullImageUrl,
                                hostMessage = statusMsg
                            )}
                        }
                    }
                } catch (e: Exception) {
                    Log.e("API_DEBUG", "Polling error: ${e.message}")
                }
                delay(interval)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun resetPollingTimer() {
        lastActiveTime = System.currentTimeMillis()
        _state.update { it.copy(pollIntervalMs = 1000L) }
    }

    // Force host to take a new screenshot by sending a harmless "Scroll 0" payload
    fun forceRefreshScreenshot(deviceId: String) {
        resetPollingTimer()
        viewModelScope.launch {
            try {
                val dummyAction = NetworkAction(type = "scroll", axis = "vertical", amount = 0)
                val payload = CommandRequest(clientPayload = ClientPayload(actions = listOf(dummyAction)))

                Log.d("API_DEBUG", "Sending dummy payload (Scroll 0) to force host screenshot update.")
                ApiClient.service.sendCommandRequest(deviceId, payload)
            } catch (e: Exception) {
                Log.e("API_DEBUG", "Failed to force refresh screenshot", e)
            }
        }
    }

    fun addAction(action: Action) {
        _state.update { it.copy(actions = it.actions + action) }
        resetPollingTimer()
    }

    fun removeAction(index: Int) {
        _state.update {
            val current = it.actions.toMutableList()
            if (index in current.indices) current.removeAt(index)
            it.copy(actions = current)
        }
        resetPollingTimer()
    }

    fun clearActions() {
        _state.update { it.copy(actions = emptyList()) }
        resetPollingTimer()
    }

    fun moveAction(fromIndex: Int, toIndex: Int) {
        _state.update {
            val newActions = it.actions.toMutableList()
            if (fromIndex in newActions.indices && toIndex in newActions.indices) {
                Collections.swap(newActions, fromIndex, toIndex)
            }
            it.copy(actions = newActions)
        }
        resetPollingTimer()
    }

    fun sendPayload(deviceId: String) {
        val currentActions = _state.value.actions
        if (currentActions.isEmpty()) return

        _state.update { it.copy(sendStatus = SendStatus.SENDING) }

        viewModelScope.launch {
            val networkActions = currentActions.map { action ->
                when (action) {
                    is Action.Move -> NetworkAction(type = "move_mouse", x = action.x, y = action.y)
                    is Action.Click -> NetworkAction(type = "click", button = action.button.name.lowercase())
                    is Action.DoubleClick -> NetworkAction(type = "double_click", button = action.button.name.lowercase())
                    is Action.Text -> NetworkAction(type = "type_text", text = action.value)
                    is Action.Scroll -> NetworkAction(type = "scroll", axis = action.axis, amount = action.amount)
                    is Action.KeyPress -> NetworkAction(type = "key_press", key = action.key)
                    is Action.Hotkey -> NetworkAction(type = "hotkey", key = action.key, modifiers = action.modifiers)
                }
            }

            // LOGS
            Log.d("API_DEBUG", "======= 🛠️ RAW ACTIONS GENERATED (COUNT: ${networkActions.size}) =======")
            networkActions.forEachIndexed { index, netAction ->
                val debugLine = "Index: $index | Type: [${netAction.type}] | Key: [${netAction.key}] | Text: [${netAction.text}]"

                Log.d("API_DEBUG", debugLine)      // Prints to Logcat
                println("🔥 UI_CHECK -> $debugLine") // Prints to Run Tab window
            }
            Log.d("API_DEBUG", "================================================================")

            try {
                val payload = CommandRequest(clientPayload = ClientPayload(actions = networkActions))

                println("🔥 ACTION QUEUE: Sending ${networkActions.size} actions to device: $deviceId")
                Log.d("API_DEBUG", "Sending actions: $payload")

                val response = ApiClient.service.sendCommandRequest(deviceId, payload)

                if (response.isSuccessful) {
                    println("🔥 SUCCESS: Server returned code ${response.code()} and saved payload!")
                    Log.d("API_DEBUG", "Success! Server saved payload to DB.")

                    _state.update { it.copy(
                        sendStatus = SendStatus.SUCCESS,
                        actions = emptyList() // Clear local queue on success
                    )}
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("🔥 FAILED: HTTP ${response.code()} | Server Message: $errorBody")
                    Log.e("API_DEBUG", "Database save rejected! HTTP Status: ${response.code()} | Server says: $errorBody")

                    _state.update { it.copy(sendStatus = SendStatus.ERROR) }
                }
            } catch (e: Exception) {
                println("🔥 CRASH: ${e.message}")
                Log.e("API_DEBUG", "Network execution failed fatal: ${e.message}", e)
                _state.update { it.copy(sendStatus = SendStatus.ERROR) }
            } finally {
                delay(3000)
                _state.update { if (it.sendStatus != SendStatus.SENDING) it.copy(sendStatus = SendStatus.IDLE) else it }
            }
        }
    }
}