package io.github.sayaka04.androidremoteclient.api

import com.google.gson.annotations.SerializedName

// ==========================================
// NEW MODELS (Auth & Navigation)
// ==========================================
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String
)

data class Device(
    val id: String?,       // Made nullable to prevent parsing crashes
    val name: String?,     // Made nullable to prevent parsing crashes
    @SerializedName("is_online") val isOnline: Boolean = false
)

data class DeviceCommand(
    val id: String?,       // Made nullable
    val name: String?,     // Made nullable
    val description: String? = null
)


// ==========================================
// YOUR ORIGINAL MODELS (Untouched)
// ==========================================
data class CommandRequest(
    @SerializedName("client_payload") val clientPayload: ClientPayload
)

data class ClientPayload(
    @SerializedName("actions") val actions: List<NetworkAction>
)

data class NetworkAction(
    @SerializedName("type") val type: String,
    @SerializedName("x") val x: Float? = null,
    @SerializedName("y") val y: Float? = null,
    @SerializedName("button") val button: String? = null,
    @SerializedName("text") val text: String? = null
)

data class HostResponseWrapper(
    @SerializedName("data") val data: HostData?
)

data class HostData(
    @SerializedName("command_id") val commandId: Int?,
    @SerializedName("has_client_request") val hasClientRequest: Boolean?,
    @SerializedName("has_host_response") val hasHostResponse: Boolean?,
    @SerializedName("requested_at") val requestedAt: String?,
    @SerializedName("last_updated_at") val lastUpdatedAt: String?
)