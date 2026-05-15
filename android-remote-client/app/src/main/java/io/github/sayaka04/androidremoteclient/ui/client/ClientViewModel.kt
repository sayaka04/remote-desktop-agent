package io.github.sayaka04.androidremoteclient.ui.client

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ClientViewModel: ViewModel(){

    private val _state = MutableStateFlow(ClientState())   // --- Private (Mutable)
    val state: StateFlow<ClientState> = _state.asStateFlow()     // --- Public (ReadOnly)

    fun helloworld(){
        //
    }

    fun updateApiBaseUrl(newApiBaseUrl: String){
        _state.update { currentState ->
            currentState.copy(apiBaseURL = newApiBaseUrl)
        }
    }

    fun updateApiDeviceId(newApiDeviceId: String){
        _state.update { currentState ->
            currentState.copy(apiDeviceId = newApiDeviceId)
        }
    }

    fun updateApiCommandId(newApiCommandId: String){
        _state.update { currentState ->
            currentState.copy(apiCommandId = newApiCommandId)
        }
    }

    fun getClientDetails(): String{
        return "Base URL: ${state.value.apiBaseURL}\n" +
                "Device ID: ${state.value.apiDeviceId}\n" +
                "Command ID: ${state.value.apiCommandId}\n"
    }
}