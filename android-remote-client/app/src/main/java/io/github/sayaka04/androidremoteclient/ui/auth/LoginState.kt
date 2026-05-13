package io.github.sayaka04.androidremoteclient.ui.auth

data class LoginState(
    val email: String = "",
    val password: String = "",
    val token: String? = "",
    val isLoggedIn: Boolean = false,
)