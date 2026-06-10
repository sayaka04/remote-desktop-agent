package io.github.sayaka04.androidremoteclient.ui.commands

//data class CommandState(
//    val textFieldInput: String = "",
//    val actions: List<Action> = emptyList(),
//    val hostMessage: String = "",
//    val isHostTaskSuccessful: Boolean = false,
//    val isNetworkLoading: Boolean = false
//)

enum class ClickType { LEFT, MIDDLE, RIGHT }

sealed class Action {
    // Mouse
    data class Move(val x: Float, val y: Float) : Action()
    data class Click(val button: ClickType) : Action()
    data class DoubleClick(val button: ClickType) : Action()
    data class Scroll(val axis: String, val amount: Int) : Action() // axis: "vertical" or "horizontal"

    // Keyboard
    data class Text(val value: String) : Action()
    data class KeyPress(val key: String) : Action()
    data class Hotkey(val key: String, val modifiers: List<String>) : Action()
}