package io.github.sayaka04.androidremoteclient.ui.host

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import io.github.sayaka04.androidremoteclient.api.ApiClient
import io.github.sayaka04.androidremoteclient.ui.client.ClientViewModel
import io.github.sayaka04.androidremoteclient.ui.command.CommandViewModel
import io.github.sayaka04.androidremoteclient.ui.host.components.ZoomableMapView
import io.github.sayaka04.androidremoteclient.ui.host.components.HostControlPanel
import io.github.sayaka04.androidremoteclient.util.PreferenceDatastoreUtil
import io.github.sayaka04.androidremoteclient.util.PreferenceDatastoreUtil.getString

@Composable
fun HostScreen(hostViewModel: HostViewModel = viewModel(),
               commandViewModel: CommandViewModel = viewModel(),
               clientViewModel: ClientViewModel = viewModel()) {

    val hostState by hostViewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        PreferenceDatastoreUtil.setClientDetails(context, clientViewModel)
        val apiBaseUrl = getString(context, "apiBaseURL")
        ApiClient.setApiBaseUrl(apiBaseUrl.toString())
    }



    val painter = rememberAsyncImagePainter(
        hostState.imageUrl
    )

    Box(modifier = Modifier.fillMaxSize()) {

        ZoomableMapView(
            modifier = Modifier.fillMaxSize(),
            painter = painter,
            hostState = hostState,
            hostViewModel = hostViewModel,
            onCoordinateSelected = { newSelectedState ->
                // FIX: Actually update the ViewModel with the new coordinates!
                hostViewModel.updatePercentX(newSelectedState.percentX)
                hostViewModel.updatePercentY(newSelectedState.percentY)
                hostViewModel.updateImagePixelX(newSelectedState.imagePixelX)
                hostViewModel.updateImagePixelY(newSelectedState.imagePixelY)
                hostViewModel.updateScreenX(newSelectedState.screenX)
                hostViewModel.updateScreenY(newSelectedState.screenY)
            }
        )

        HostControlPanel(
            hostState = hostState,
            onToggleMode = {
                hostViewModel.updateIsSelectionMode(
                    !hostState.isSelectionMode
                )
            },
            onReset = {
                hostViewModel.incrementResetTrigger()
            },
            onSet = {
                commandViewModel.insertIntoActionMove(hostState.percentX, hostState.percentY)
                Toast.makeText(
                    context,
                    "(${hostState.imagePixelX.toInt()}, ${hostState.imagePixelY.toInt()}) -> (${hostState.percentX}, ${hostState.percentY})",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}