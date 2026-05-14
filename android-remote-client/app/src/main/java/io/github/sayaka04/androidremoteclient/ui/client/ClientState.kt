package io.github.sayaka04.androidremoteclient.ui.client

data class ClientState(
    val apiBaseURL: String = "",
    val apiToken: String = "",
    val apiDeviceId: String = "",
    val apiCommandId: String = "",

    val pollMaxIntervalSec: String = "",
    val pollTimeoutSec: String = "",
)