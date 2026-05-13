package io.github.sayaka04.androidremoteclient.ui.host

data class HostState(
    val screenX: Float = 0f,
    val screenY: Float = 0f,
    val imagePixelX: Float = 0f,
    val imagePixelY: Float = 0f,
    val percentX: Float = 0f,
    val percentY: Float = 0f,
    val scale: Float = 0f,
    val panX: Float = 0f,
    val panY: Float = 0f,

    val offsetX: Float = 0f,
    val offsetY: Float = 0f,

    val isSelectionMode: Boolean = false,
    val resetTrigger: Int = 0,

    val imageUrl: String = "https://cdn.hswstatic.com/gif/maps.jpg",

)
