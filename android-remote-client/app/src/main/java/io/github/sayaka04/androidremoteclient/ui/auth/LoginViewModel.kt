package io.github.sayaka04.androidremoteclient.ui.auth

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.sayaka04.androidremoteclient.api.ApiClient
import io.github.sayaka04.androidremoteclient.api.LoginRequest
import io.github.sayaka04.androidremoteclient.util.PreferenceDatastoreUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun updateEmail(v: String) = _state.update { it.copy(email = v) }
    fun updatePassword(v: String) = _state.update { it.copy(password = v) }
    fun updateBaseUrl(v: String) = _state.update { it.copy(baseUrl = v) }

    // NEW: Method to clear errors after they are consumed by the UI
    fun clearError() = _state.update { it.copy(error = null) }

    fun checkAutoLogin(context: Context, onAutoLogin: () -> Unit) {
        viewModelScope.launch {
            Log.d("AuthLog", "--- Starting Auto-Login Check ---")
            val url = PreferenceDatastoreUtil.getString(context, "base_url") ?: ""
            val token = PreferenceDatastoreUtil.getString(context, "auth_token") ?: ""

            _state.update { it.copy(baseUrl = url) }

            if (url.isNotBlank() && token.isNotBlank()) {
                Log.d("AuthLog", "Auto-login triggered! Priming ApiClient and navigating...")
                ApiClient.setApiBaseUrl(url)
                ApiClient.setToken(token)
                onAutoLogin()
            } else {
                Log.d("AuthLog", "Auto-login skipped. User needs to log in manually.")
            }
        }
    }

    fun saveUrl(context: Context) {
        viewModelScope.launch {
            val currentUrl = _state.value.baseUrl
            Log.d("AuthLog", "Manually saving URL to DataStore: $currentUrl")
            PreferenceDatastoreUtil.saveString(context, "base_url", currentUrl)
            ApiClient.setApiBaseUrl(currentUrl)
        }
    }

    fun performLogin(context: Context, onSuccess: () -> Unit) {
        if (_state.value.baseUrl.isBlank()) {
            _state.update { it.copy(error = "Set Server URL in the Settings tab first") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                ApiClient.setApiBaseUrl(_state.value.baseUrl)

                val request = LoginRequest(_state.value.email, _state.value.password)
                val response = ApiClient.service.login(request)

                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.token

                    PreferenceDatastoreUtil.saveString(context, "base_url", _state.value.baseUrl)
                    PreferenceDatastoreUtil.saveString(context, "auth_token", token)
                    ApiClient.setToken(token)

                    onSuccess()
                } else {
                    _state.update { it.copy(error = "Login Failed: ${response.code()}") }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.update { it.copy(error = "Connection Error: ${e.localizedMessage}") }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}