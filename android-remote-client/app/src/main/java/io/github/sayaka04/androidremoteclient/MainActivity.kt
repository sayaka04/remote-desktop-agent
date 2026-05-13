package io.github.sayaka04.androidremoteclient

import android.os.Bundle

import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import io.github.sayaka04.androidremoteclient.ui.auth.LoginForm
import io.github.sayaka04.androidremoteclient.ui.host.HostScreen

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {

                MainScreen()

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabs = listOf("Home", "Commands", "[?]")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Name") }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            TabRow(selectedTabIndex = selectedTabIndex) {

                tabs.forEachIndexed { index, title ->

                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.dp)
            ) {

                when (selectedTabIndex) {
                    0 -> HostScreen()
                    1 -> Text("Commands")
                    2 -> LoginForm()
                }
            }
        }
    }
}
