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

    fun checkAutoLogin(context: Context, onAutoLogin: () -> Unit) {
        viewModelScope.launch {
            Log.d("AuthLog", "--- Starting Auto-Login Check ---")
            val url = PreferenceDatastoreUtil.getString(context, "base_url") ?: ""
            val token = PreferenceDatastoreUtil.getString(context, "auth_token") ?: ""

            Log.d("AuthLog", "Found Saved URL: '$url'")
            Log.d("AuthLog", "Found Saved Token: '${if (token.isNotBlank()) "YES (Hidden for security)" else "NONE"}'")

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
            Log.e("AuthLog", "Login attempt blocked: Base URL is blank.")
            _state.update { it.copy(error = "Set Server URL in the Settings tab first") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            Log.d("AuthLog", "--- Starting Login Request ---")
            Log.d("AuthLog", "Target URL: ${_state.value.baseUrl}")
            Log.d("AuthLog", "Email used: ${_state.value.email}")

            try {
                // Ensure API Client is using the correct URL before sending request
                ApiClient.setApiBaseUrl(_state.value.baseUrl)

                val request = LoginRequest(_state.value.email, _state.value.password)
                val response = ApiClient.service.login(request)

                Log.d("AuthLog", "Server Response Code: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.token
                    Log.d("AuthLog", "Login SUCCESS! Received Token: $token")

                    Log.d("AuthLog", "Saving URL and Token to DataStore...")
                    PreferenceDatastoreUtil.saveString(context, "base_url", _state.value.baseUrl)
                    PreferenceDatastoreUtil.saveString(context, "auth_token", token)

                    ApiClient.setToken(token)

                    Log.d("AuthLog", "Triggering onSuccess navigation callback.")
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AuthLog", "Login FAILED! Response Code: ${response.code()}")
                    Log.e("AuthLog", "Error Body: $errorBody")
                    _state.update { it.copy(error = "Login Failed: ${response.code()}") }
                }
            } catch (e: Exception) {
                Log.e("AuthLog", "EXCEPTION during login: ${e.message}")
                e.printStackTrace()
                _state.update { it.copy(error = e.localizedMessage) }
            } finally {
                Log.d("AuthLog", "--- Login Request Finished ---")
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}