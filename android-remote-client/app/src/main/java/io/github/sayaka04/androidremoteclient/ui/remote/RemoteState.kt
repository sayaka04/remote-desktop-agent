package io.github.sayaka04.androidremoteclient.ui.remote

import io.github.sayaka04.androidremoteclient.ui.commands.Action

enum class SendStatus { IDLE, SENDING, SUCCESS, ERROR }

data class RemoteState(
    // 1. Host Image & Targeting Properties
    val imageUrl: String = "",
    val percentX: Float = 0f,
    val percentY: Float = 0f,
    val imagePixelX: Float = 0f,
    val imagePixelY: Float = 0f,
    val isSelectionMode: Boolean = true,

    // Zoom/Pan preservation
    val scale: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,

    // 2. Composer Queue Properties
    val actions: List<Action> = emptyList(),
    val sendStatus: SendStatus = SendStatus.IDLE,
    val hostMessage: String = "",

    // 3. Polling Properties
    val pollIntervalMs: Long = 1000L
)