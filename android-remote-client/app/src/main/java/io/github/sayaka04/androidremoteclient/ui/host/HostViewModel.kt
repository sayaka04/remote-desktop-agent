package io.github.sayaka04.androidremoteclient.ui.host

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HostViewModel: ViewModel() {

    private val _state = MutableStateFlow(HostState())  // --- Private (Mutable)
    val state: StateFlow<HostState> = _state.asStateFlow()      // --- Public (ReadOnly)

    fun updateScreenX(newScreenX: Float){
        _state.update { currentState ->
            currentState.copy(screenX = newScreenX)
        }
    }

    fun updateScreenY(newScreenY: Float){
        _state.update { currentState ->
            currentState.copy(screenY = newScreenY)
        }
    }

    fun updateImagePixelX(newImagePixelX: Float){
        _state.update { currentState ->
            currentState.copy(imagePixelX = newImagePixelX)
        }
    }

    fun updateImagePixelY(newImagePixelY: Float){
        _state.update { currentState ->
            currentState.copy(imagePixelY = newImagePixelY)
        }
    }

    fun updatePercentX(newPercentX: Float){
        _state.update { currentState ->
            currentState.copy(percentX = newPercentX)
        }
    }

    fun updatePercentY(newPercentY: Float){
        _state.update { currentState ->
            currentState.copy(percentY = newPercentY)
        }
    }


    fun updateIsSelectionMode(newIsSelectionMode: Boolean){
        _state.update { currentState ->
            currentState.copy(isSelectionMode = newIsSelectionMode)
        }
    }

    fun updateScale(newScale: Float){
        _state.update { currentState ->
            currentState.copy(scale = newScale)
        }
    }

    fun updateOffsetX(newOffsetX: Float){
        _state.update { currentState ->
            currentState.copy(offsetX = newOffsetX)
        }
    }

    fun updateOffsetY(newOffsetY: Float){
        _state.update { currentState ->
            currentState.copy(offsetY = newOffsetY)
        }
    }



    fun incrementResetTrigger(){
        _state.update { currentState ->
            currentState.copy( resetTrigger = currentState.resetTrigger + 1)
        }
    }





}