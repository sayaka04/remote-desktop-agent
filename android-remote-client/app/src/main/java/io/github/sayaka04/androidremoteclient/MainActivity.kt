package io.github.sayaka04.androidremoteclient

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

import io.github.sayaka04.androidremoteclient.navigation.AppNavigation
import io.github.sayaka04.androidremoteclient.ui.theme.ShadcnTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainLog", "MainActivity onCreate initialized")

        setContent {
            // Replaced default MaterialTheme with ShadcnTheme
            ShadcnTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // All routing logic safely delegated in AppNavigation
                    AppNavigation()
                }
            }
        }
    }
}