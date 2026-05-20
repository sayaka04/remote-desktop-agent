package io.github.sayaka04.androidremoteclient

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch

// Screen Imports
import io.github.sayaka04.androidremoteclient.api.ApiClient
import io.github.sayaka04.androidremoteclient.ui.auth.LoginScreen
import io.github.sayaka04.androidremoteclient.ui.client.ClientScreen
import io.github.sayaka04.androidremoteclient.ui.command.CommandScreen
import io.github.sayaka04.androidremoteclient.ui.devices.CommandsScreen
import io.github.sayaka04.androidremoteclient.ui.devices.DevicesScreen
import io.github.sayaka04.androidremoteclient.ui.host.HostScreen
import io.github.sayaka04.androidremoteclient.util.PreferenceDatastoreUtil

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainLog", "MainActivity onCreate initialized")

        setContent {
            MaterialTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    NavHost(navController = navController, startDestination = "login_screen") {

        // 1. LOGIN
        composable("login_screen") {
            LoginScreen(
                onLoginSuccess = {
                    Log.d("NavLog", "Navigating to devices_screen")
                    navController.navigate("devices_screen") {
                        popUpTo("login_screen") { inclusive = true }
                    }
                }
            )
        }

        // 2. DEVICES
        composable("devices_screen") {
            DevicesScreen(
                onDeviceClick = { deviceId ->
                    Log.d("NavLog", "Navigating to commands_screen for device: $deviceId")
                    navController.navigate("commands_screen/$deviceId")
                },
                onLogoutClick = {
                    scope.launch {
                        Log.d("NavLog", "Executing Logout Sequence")
                        PreferenceDatastoreUtil.remove(context, "auth_token")
                        ApiClient.setToken(null)
                        navController.navigate("login_screen") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        // 3. COMMANDS
        composable(
            route = "commands_screen/{deviceId}",
            arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""

            CommandsScreen(
                deviceId = deviceId,
                onCommandClick = { commandId ->
                    Log.d("NavLog", "Navigating to main_dashboard. Device: $deviceId, Command: $commandId")
                    navController.navigate("main_dashboard/$deviceId/$commandId")
                },
                onBackClick = {
                    Log.d("NavLog", "Popping backstack from commands_screen")
                    navController.popBackStack()
                }
            )
        }

        // 4. MAIN DASHBOARD (Tabs)
        composable(
            route = "main_dashboard/{deviceId}/{commandId}",
            arguments = listOf(
                navArgument("deviceId") { type = NavType.StringType },
                navArgument("commandId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
            val commandId = backStackEntry.arguments?.getString("commandId") ?: ""

            MainScreen(
                deviceId = deviceId,
                commandId = commandId,
                onBackClick = {
                    Log.d("NavLog", "Popping backstack from main_dashboard")
                    navController.popBackStack()
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    deviceId: String,
    commandId: String,
    onBackClick: () -> Unit
) {
    // Note: We removed "Login" from these tabs since it's the starting screen now
    val tabs = listOf("Host", "Command", "Client")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                text = title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = tabs.size - 1,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.dp)
            ) { page ->
                when (page) {
                    0 -> HostScreen()
                    1 -> CommandScreen()
                    2 -> ClientScreen()
                }
            }
        }
    }
}