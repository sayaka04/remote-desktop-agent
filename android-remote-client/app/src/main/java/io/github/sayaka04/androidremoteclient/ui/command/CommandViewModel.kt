package io.github.sayaka04.androidremoteclient.ui.command

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class CommandViewModel: ViewModel(){
    private val _state = MutableStateFlow(CommandState())   // --- Private (Mutable)
    val state: StateFlow<CommandState> = _state.asStateFlow()     // --- Public (ReadOnly)

    fun updateTextField(newTextFieldInput: String){
        _state.update { currentState ->
            currentState.copy(textFieldInput = newTextFieldInput)
        }
    }

    fun insertIntoActionTypeText(newActionTypeText: String){
        _state.value.actions.add(Action.Text(newActionTypeText))
    }

    fun insertIntoActionClick(clickType: ClickType) {
        _state.value.actions.add(
            Action.Click(clickType)
        )
    }

    fun insertIntoActionMove(perX: Float, perY: Float){
        _state.value.actions.add(Action.Move(perX, perY))
    }

    fun getCommandActionLists(): String {
        var text: String = ""
        state.value.actions.forEach { action ->
            when (action) {
                is Action.Click -> {
                    text += "Click: ${action.button}\n"
                }

                is Action.Move -> {
                    text += "Move: x=${action.x}, y=${action.y}\n"
                }

                is Action.Text -> {
                    text += "Text: ${action.value}\n"
                }
            }
        }
        return text
    }

}