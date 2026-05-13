package io.github.sayaka04.androidremoteclient.ui.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel: ViewModel() {

    private val _state = MutableStateFlow(LoginState())   // --- Private (Mutable)
    val state: StateFlow<LoginState> = _state.asStateFlow()     // --- Public (ReadOnly)

    fun updateEmail(newEmail: String) {
        _state.update { currentState ->
            currentState.copy(email = newEmail)
        }
    }

    fun updatePassword(newPassword: String) {
        _state.update { currentState ->
            currentState.copy(password = newPassword)
        }
    }
}