package io.github.sayaka04.androidremoteclient.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    // Auto-login check on launch
    LaunchedEffect(Unit) {
        vm.checkAutoLogin(context, onLoginSuccess)
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Login") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Server") })
            }

            Column(modifier = Modifier.padding(24.dp)) {
                if (selectedTab == 0) {
                    // Login Form
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = vm::updateEmail,
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = vm::updatePassword,
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (state.error != null) {
                        Text(
                            text = state.error!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { vm.performLogin(context, onLoginSuccess) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Text("Login")
                        }
                    }
                } else {
                    // Server Settings Form
                    OutlinedTextField(
                        value = state.baseUrl,
                        onValueChange = vm::updateBaseUrl,
                        label = { Text("Base URL") },
                        placeholder = { Text("http://10.26.140.122/api/") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            vm.saveUrl(context)
                            selectedTab = 0
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save & Return")
                    }
                }
            }
        }
    }
}