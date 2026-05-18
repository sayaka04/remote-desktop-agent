package io.github.sayaka04.androidremoteclient.ui.command

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.sayaka04.androidremoteclient.api.ApiClient
import io.github.sayaka04.androidremoteclient.api.ClientPayload
import io.github.sayaka04.androidremoteclient.api.CommandRequest
import io.github.sayaka04.androidremoteclient.api.NetworkAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CommandViewModel : ViewModel() {
    private val _state = MutableStateFlow(CommandState())
    val state: StateFlow<CommandState> = _state.asStateFlow()

    fun updateTextField(newTextFieldInput: String) {
        _state.update { it.copy(textFieldInput = newTextFieldInput) }
    }

    fun insertIntoActionTypeText(text: String) {
        if (text.isBlank()) return
        _state.update { it.copy(
            actions = it.actions + Action.Text(text),
            textFieldInput = ""
        )}
    }

    fun insertIntoActionClick(type: ClickType) {
        _state.update { it.copy(actions = it.actions + Action.Click(type)) }
    }

    fun insertIntoActionMove(x: Float, y: Float) {
        _state.update { it.copy(actions = it.actions + Action.Move(x, y)) }
    }

    fun removeActionAt(index: Int) {
        _state.update { currentState ->
            val newList = currentState.actions.toMutableList()
            if (index in newList.indices) newList.removeAt(index)
            currentState.copy(actions = newList)
        }
    }

    fun moveAction(fromIndex: Int, toIndex: Int) {
        _state.update { currentState ->
            val newList = currentState.actions.toMutableList()
            if (fromIndex in newList.indices && toIndex in newList.indices) {
                val item = newList.removeAt(fromIndex)
                newList.add(toIndex, item)
            }
            currentState.copy(actions = newList)
        }
    }

    fun clearActions() {
        _state.update { it.copy(actions = emptyList()) }
    }
// --- NETWORK OPERATIONS ---

    fun sendCommandsToServer(deviceId: String) {
        val currentActions = state.value.actions
        if (currentActions.isEmpty()) {
            Log.w("API_DEBUG", "Abort: No actions in queue to send.")
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isNetworkLoading = true) }
            try {
                Log.d("API_DEBUG", "Starting Send Process for Device: $deviceId")

                val networkActions = currentActions.map { action ->
                    when (action) {
                        is Action.Click -> NetworkAction(type = "click", button = action.button.name.lowercase())
                        is Action.Move -> NetworkAction(type = "move_mouse", x = action.x, y = action.y)
                        is Action.Text -> NetworkAction(type = "type_text", text = action.value)
                    }
                }

                val payload = CommandRequest(ClientPayload(networkActions))
                Log.d("API_DEBUG", "Payload Prepared: ${networkActions.size} actions mapped.")

                Log.d("API_DEBUG", "Attempting connection to server...")
                val response = ApiClient.service.sendCommandRequest(deviceId, payload)

                if (response.isSuccessful) {
                    Log.d("API_DEBUG", "HTTP 200: Commands sent and received successfully!")
                    clearActions() // Auto-clear on success
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("API_DEBUG", "HTTP Error Code: ${response.code()}")
                    Log.e("API_DEBUG", "Server Error Response: $errorBody")
                }
            } catch (e: Exception) {
                Log.e("API_DEBUG", "CRITICAL NETWORK FAILURE")
                Log.e("API_DEBUG", "Exception Message: ${e.message}")
                e.printStackTrace()
            } finally {
                _state.update { it.copy(isNetworkLoading = false) }
                Log.d("API_DEBUG", "Send Process Finished.")
            }
        }
    }

    fun requestDataFromHost(deviceId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isNetworkLoading = true) }
            try {
                Log.d("API_DEBUG", "Fetching Host Status for: $deviceId")
                val response = ApiClient.service.requestHostData(deviceId)

                if (response.isSuccessful && response.body()?.data != null) {
                    val hostData = response.body()!!.data!!
                    Log.d("API_DEBUG", "Host Data Received: Success=${hostData.hasHostResponse}")

                    _state.update {
                        it.copy(
                            hostMessage = "Updated: ${hostData.lastUpdatedAt}",
                            isHostTaskSuccessful = hostData.hasHostResponse == true
                        )
                    }
                } else {
                    Log.e("API_DEBUG", "Host Fetch Error: HTTP ${response.code()}")
                    Log.e("API_DEBUG", "Error Body: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("API_DEBUG", "Host Fetch Exception: ${e.message}")
            } finally {
                _state.update { it.copy(isNetworkLoading = false) }
            }
        }
    }
}