package io.github.sayaka04.androidremoteclient.ui.devices

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.sayaka04.androidremoteclient.api.ApiClient
import io.github.sayaka04.androidremoteclient.api.Device
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DevicesViewModel : ViewModel() {
    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    val devices: StateFlow<List<Device>> = _devices.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun fetchDevices() {
        viewModelScope.launch {
            Log.d("DevicesLog", "--- Starting fetchDevices() ---")
            _isLoading.value = true
            try {
                val response = ApiClient.service.getDevices()
                Log.d("DevicesLog", "Response Code: ${response.code()}")

                if (response.isSuccessful) {
                    val list = response.body() ?: emptyList()
                    _devices.value = list
                    Log.d("DevicesLog", "Successfully fetched ${list.size} devices.")
                } else {
                    Log.e("DevicesLog", "Failed to fetch devices. Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("DevicesLog", "Exception in fetchDevices: ${e.localizedMessage}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
                Log.d("DevicesLog", "--- Finished fetchDevices() ---")
            }
        }
    }
}