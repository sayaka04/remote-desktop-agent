package io.github.sayaka04.androidremoteclient

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import kotlinx.coroutines.launch

// Your custom screen imports
import io.github.sayaka04.androidremoteclient.ui.client.ClientScreen
import io.github.sayaka04.androidremoteclient.ui.auth.LoginScreen
import io.github.sayaka04.androidremoteclient.ui.command.CommandScreen
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
    val tabs = listOf("Home", "Commands", "Client", "[?]")

    // 1. Setup the pager state for our 4 tabs
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { tabs.size }
    )

    // 2. Coroutine scope needed to trigger the scroll animation from a regular onClick callback
    val coroutineScope = rememberCoroutineScope()

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
            // 3. TabRow syncing its selected state with the Pager's current page
            TabRow(selectedTabIndex = pagerState.currentPage) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            // Launching a coroutine fixes the "@Composable invocations" error
                            // because we aren't calling a Composable, we are just telling the
                            // state to update, and Compose reacts to it.
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

            // 4. HorizontalPager handles the actual screen switching and state retention
            HorizontalPager(
                state = pagerState,
                // Keeps all tabs alive in memory off-screen = instant switching, no lost state!
                beyondViewportPageCount = tabs.size - 1,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.dp)
            ) { page ->
                when (page) {
                    0 -> HostScreen()
                    1 -> CommandScreen()
                    2 -> ClientScreen()
                    3 -> LoginScreen()
                }
            }
        }
    }
}