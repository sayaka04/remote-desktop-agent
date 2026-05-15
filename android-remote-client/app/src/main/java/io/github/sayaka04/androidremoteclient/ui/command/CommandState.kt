package io.github.sayaka04.androidremoteclient.ui.command

data class CommandState(

    val textFieldInput: String = "",
    val actions: MutableList<Action> = mutableListOf(),

    // New fields to hold the API response
    val hostMessage: String = "",
    val isHostTaskSuccessful: Boolean = false,
    val isNetworkLoading: Boolean = false
)

enum class ClickType {
    LEFT,
    MIDDLE,
    RIGHT
}

sealed class Action {

    data class Click(
        val button: ClickType
    ) : Action()

    data class Move(
        val x: Float,
        val y: Float
    ) : Action()

    data class Text(
        val value: String
    ) : Action()
}