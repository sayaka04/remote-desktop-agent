package io.github.sayaka04.androidremoteclient.ui.commands

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.sayaka04.androidremoteclient.api.ApiClient
import io.github.sayaka04.androidremoteclient.api.DeviceCommand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CommandsViewModel : ViewModel() {
    private val _commands = MutableStateFlow<List<DeviceCommand>>(emptyList())
    val commands: StateFlow<List<DeviceCommand>> = _commands.asStateFlow()

    // Used for the initial page load
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Used for pull-to-refresh or manual retries
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun fetchCommands(deviceId: String, isRefresh: Boolean = false) {
        viewModelScope.launch {
            Log.d("CommandsLog", "--- Starting fetchCommands for device: $deviceId (isRefresh=$isRefresh) ---")

            if (isRefresh) {
                _isRefreshing.value = true
            } else {
                _isLoading.value = true
            }

            try {
                val response = ApiClient.service.getCommands(deviceId)
                Log.d("CommandsLog", "Response Code: ${response.code()}")

                if (response.isSuccessful) {
                    val list = response.body() ?: emptyList()
                    _commands.value = list
                    Log.d("CommandsLog", "Successfully fetched ${list.size} commands.")
                } else {
                    Log.e("CommandsLog", "Failed to fetch commands. Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("CommandsLog", "Exception in fetchCommands: ${e.localizedMessage}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
                _isRefreshing.value = false
                Log.d("CommandsLog", "--- Finished fetchCommands ---")
            }
        }
    }
}