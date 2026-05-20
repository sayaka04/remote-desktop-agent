package io.github.sayaka04.androidremoteclient.ui.auth

data class LoginState(
    val email: String = "",
    val password: String = "",
    val baseUrl: String = "",
    val token: String? = "",
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)