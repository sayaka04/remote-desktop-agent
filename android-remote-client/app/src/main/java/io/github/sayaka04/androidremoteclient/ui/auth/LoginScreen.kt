package io.github.sayaka04.androidremoteclient.ui.auth

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    vm: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.checkAutoLogin(context, onLoginSuccess)
    }

    LaunchedEffect(state.error) {
        state.error?.let { errorMessage ->
            snackbarHostState.showSnackbar(errorMessage)
            vm.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // SHADCN FIX: Wrapped in a flat, bordered card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Login") })
                        Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Server") })
                    }

                    Box(modifier = Modifier.padding(24.dp)) {
                        if (selectedTab == 0) {
                            LoginForm(state = state, vm = vm, context = context, onLoginSuccess = onLoginSuccess)
                        } else {
                            ServerSettingsForm(state = state, vm = vm, context = context) {
                                selectedTab = 0
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginForm(
    state: LoginState,
    vm: LoginViewModel,
    context: Context,
    onLoginSuccess: () -> Unit
) {
    Column {
        OutlinedTextField(
            value = state.email,
            onValueChange = vm::updateEmail,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = state.password,
            onValueChange = vm::updatePassword,
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { vm.performLogin(context, onLoginSuccess) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Login")
            }
        }
    }
}

@Composable
private fun ServerSettingsForm(
    state: LoginState,
    vm: LoginViewModel,
    context: Context,
    onSaveSuccess: () -> Unit
) {
    Column {
        OutlinedTextField(
            value = state.baseUrl,
            onValueChange = vm::updateBaseUrl,
            label = { Text("Base URL") },
            placeholder = { Text("http://10.26.140.122/api/") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                vm.saveUrl(context)
                onSaveSuccess()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save & Return")
        }
    }
}