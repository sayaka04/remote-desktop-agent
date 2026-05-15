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


class CommandViewModel: ViewModel(){
    private val _state = MutableStateFlow(CommandState())   // --- Private (Mutable)
    val state: StateFlow<CommandState> = _state.asStateFlow()     // --- Public (ReadOnly)

    fun updateTextField(newTextFieldInput: String){
        _state.update { currentState ->
            currentState.copy(textFieldInput = newTextFieldInput)
        }
    }

    fun insertIntoActionTypeText(newActionTypeText: String){
        _state.value.actions.add(Action.Text(newActionTypeText))
    }

    fun insertIntoActionClick(clickType: ClickType) {
        _state.value.actions.add(
            Action.Click(clickType)
        )
    }

    fun insertIntoActionMove(perX: Float, perY: Float){
        _state.value.actions.add(Action.Move(perX, perY))
    }

    fun getCommandActionLists(): String {
        var text: String = ""
        state.value.actions.forEach { action ->
            when (action) {
                is Action.Click -> {
                    text += "Click: ${action.button}\n"
                }

                is Action.Move -> {
                    text += "Move: x=${action.x}, y=${action.y}\n"
                }

                is Action.Text -> {
                    text += "Text: ${action.value}\n"
                }
            }
        }
        return text
    }


    // --- NETWORK FUNCTIONS ---

    fun sendCommandsToServer(deviceId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isNetworkLoading = true) }
            try {
                // Map UI Actions to Network Actions
                val networkActions = state.value.actions.map { action ->
                    when (action) {
                        is Action.Click -> NetworkAction(
                            type = "click",
                            button = action.button.name.lowercase()
                        )
                        is Action.Move -> NetworkAction(
                            type = "move_mouse",
                            x = action.x,
                            y = action.y
                        )
                        is Action.Text -> NetworkAction(
                            type = "type_text",
                            text = action.value
                        )
                    }
                }

                val payload = CommandRequest(ClientPayload(networkActions))
                val response = ApiClient.service.sendCommandRequest(deviceId, payload)

                if (response.isSuccessful) {
                    Log.d("API", "Commands sent successfully!")
                    // Clear actions after sending if desired:
                    // _state.value.actions.clear()
                } else {
                    Log.e("API", "Send error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("API", "Network exception: ${e.message}")
            } finally {
                _state.update { it.copy(isNetworkLoading = false) }
            }
        }
    }



    fun requestDataFromHost(deviceId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isNetworkLoading = true) }
            try {
                Log.d("API_CHECK", "Fetching data for device: $deviceId")
                val response = ApiClient.service.requestHostData(deviceId)

                if (response.isSuccessful && response.body()?.data != null) {
                    val hostData = response.body()!!.data!!

                    // --- LOGS TO CONFIRM ---
                    Log.d("API_CHECK", "Success! Response received.")
                    Log.d("API_CHECK", "Has Host Response: ${hostData.hasHostResponse}")
                    Log.d("API_CHECK", "Last Updated: ${hostData.lastUpdatedAt}")

                    _state.update {
                        it.copy(
                            hostMessage = "Updated: ${hostData.lastUpdatedAt}",
                            isHostTaskSuccessful = hostData.hasHostResponse == true
                        )
                    }
                } else {
                    val errorMsg = response.errorBody()?.string()
                    Log.e("API_CHECK", "Server Error: $errorMsg")
                }
            } catch (e: Exception) {
                Log.e("API_CHECK", "Network Exception: ${e.message}")
            } finally {
                _state.update { it.copy(isNetworkLoading = false) }
            }
        }
    }

}