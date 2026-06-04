package io.github.sayaka04.androidremoteclient.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login_screen")

    object Devices : Screen("devices_screen")

    object Commands : Screen("commands_screen/{deviceUuid}") {
        // Helper to safely build the route when navigating
        fun createRoute(deviceUuid: String) = "commands_screen/$deviceUuid"
    }

    object Remote : Screen("remote_screen/{commandUuid}") {
        // Helper to safely build the route when navigating
        fun createRoute(commandUuid: String) = "remote_screen/$commandUuid"
    }
}