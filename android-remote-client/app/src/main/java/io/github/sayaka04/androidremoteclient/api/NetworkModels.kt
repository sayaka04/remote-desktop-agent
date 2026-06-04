package io.github.sayaka04.androidremoteclient.api

import com.google.gson.annotations.SerializedName

// ==========================================
// Auth & Navigation Models
// ==========================================
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String
)

data class Device(
    val uuid: String?,
    val id: String?,
    val name: String?,
    @SerializedName("is_online") val isOnline: Boolean = false
)

data class DeviceCommand(
    val uuid: String?,
    val id: String?,
    val name: String?,
    val description: String? = null
)

// ==========================================
// Command Payload Models
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
    @SerializedName("text") val text: String? = null,
    @SerializedName("key") val key: String? = null,
    @SerializedName("axis") val axis: String? = null,
    @SerializedName("amount") val amount: Int? = null,
    @SerializedName("modifiers") val modifiers: List<String>? = null
)

// ==========================================
// Host Response Models
// ==========================================
data class HostResponseWrapper(
    @SerializedName("data") val data: HostData?
)

data class HostData(
    @SerializedName("command_id") val commandId: Int?,
    @SerializedName("has_client_request") val hasClientRequest: Boolean?,
    @SerializedName("has_host_response") val hasHostResponse: Boolean?,
    @SerializedName("screenshot_path") val screenshotPath: String?,
    @SerializedName("updated_at") val updatedAt: String?
)