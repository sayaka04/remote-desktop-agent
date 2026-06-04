package io.github.sayaka04.androidremoteclient.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch

// Screen Imports
import io.github.sayaka04.androidremoteclient.api.ApiClient
import io.github.sayaka04.androidremoteclient.ui.auth.LoginScreen
import io.github.sayaka04.androidremoteclient.ui.commands.CommandsScreen
import io.github.sayaka04.androidremoteclient.ui.devices.DevicesScreen
import io.github.sayaka04.androidremoteclient.ui.remote.RemoteControlScreen
import io.github.sayaka04.androidremoteclient.util.PreferenceDatastoreUtil

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Using Screen.Login.route instead of the raw string
    NavHost(navController = navController, startDestination = Screen.Login.route) {

        // 1. LOGIN
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    Log.d("NavLog", "Navigating to devices_screen")
                    navController.navigate(Screen.Devices.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. DEVICES
        composable(Screen.Devices.route) {
            DevicesScreen(
                onDeviceClick = { deviceUuid ->
                    Log.d("NavLog", "Navigating to commands_screen for device: $deviceUuid")
                    navController.navigate(Screen.Commands.createRoute(deviceUuid))
                },
                onLogoutClick = {
                    scope.launch {
                        Log.d("NavLog", "Executing Logout Sequence")
                        PreferenceDatastoreUtil.remove(context, "auth_token")
                        ApiClient.setToken(null)
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        // 3. COMMANDS
        composable(
            route = Screen.Commands.route,
            arguments = listOf(navArgument("deviceUuid") { type = NavType.StringType })
        ) { backStackEntry ->
            val deviceUuid = backStackEntry.arguments?.getString("deviceUuid") ?: ""

            CommandsScreen(
                deviceUuid = deviceUuid,
                onCommandClick = { commandUuid ->
                    Log.d("NavLog", "Navigating to single-page remote. Command: $commandUuid")
                    navController.navigate(Screen.Remote.createRoute(commandUuid))
                },
                onBackClick = {
                    Log.d("NavLog", "Popping backstack from commands_screen")
                    navController.popBackStack()
                }
            )
        }

        // 4. UNIFIED SINGLE PAGE DASHBOARD
        composable(
            route = Screen.Remote.route,
            arguments = listOf(navArgument("commandUuid") { type = NavType.StringType })
        ) { backStackEntry ->
            val commandUuid = backStackEntry.arguments?.getString("commandUuid") ?: return@composable

            RemoteControlScreen(deviceId = commandUuid)
        }
    }
}