package io.github.sayaka04.androidremoteclient.ui.remote.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sayaka04.androidremoteclient.ui.commands.Action
import io.github.sayaka04.androidremoteclient.ui.commands.ClickType
import io.github.sayaka04.androidremoteclient.ui.remote.RemoteState
import io.github.sayaka04.androidremoteclient.ui.remote.RemoteViewModel
import io.github.sayaka04.androidremoteclient.ui.remote.SendStatus

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ActionComposerCard(
    deviceId: String,
    state: RemoteState,
    viewModel: RemoteViewModel,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    var hkKey by remember { mutableStateOf("") }
    var hkModifiers by remember { mutableStateOf(setOf<String>()) }
    var selectedTab by remember { mutableStateOf(0) }

    Card(
        modifier = modifier
            .width(350.dp)
            .fillMaxHeight(0.85f),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Action Queue (${state.actions.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                TextButton(
                    onClick = { viewModel.clearActions() },
                    modifier = Modifier.height(28.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Clear All", fontSize = 11.sp)
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            // List of Actions
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (state.actions.isEmpty()) {
                    item {
                        // SHADCN FIX: Use onSurfaceVariant instead of hardcoded Color.Gray
                        Text(
                            "Queue is empty.\nTap the screen or use tabs below.",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
                itemsIndexed(state.actions) { index, action ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // SHADCN FIX: Dynamic text color instead of Color.White
                        Text(
                            "${index + 1}",
                            modifier = Modifier.size(18.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.width(8.dp))

                        val label = when (action) {
                            is Action.Click -> "Click ${action.button.name}"
                            is Action.Move -> "Move to (%.2f, %.2f)".format(action.x, action.y)
                            is Action.Text -> "Type: \"${action.value}\""
                            is Action.DoubleClick -> "Double Click ${action.button.name}"
                            is Action.Scroll -> "Scroll ${action.axis.uppercase()} (${action.amount})"
                            is Action.KeyPress -> "Press Key: [${action.key}]"
                            is Action.Hotkey -> "Hotkey: ${action.modifiers.joinToString("+")} + ${action.key}"
                            else -> "Action"
                        }

                        Text(label, fontSize = 11.sp, modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.moveAction(index, index - 1) }, enabled = index > 0, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.KeyboardArrowUp, null, modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = { viewModel.moveAction(index, index + 1) }, enabled = index < state.actions.lastIndex, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = { viewModel.removeAction(index) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            // Input Tabs
            val tabs = listOf("Text", "Mouse", "Keys", "Combo", "Scroll")
            Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(8.dp)) {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.height(36.dp),
                    edgePadding = 0.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    divider = {} // Removed default tab divider for cleaner look
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title, fontSize = 11.sp) })
                    }
                }

                Box(modifier = Modifier.height(110.dp).padding(top = 8.dp), contentAlignment = Alignment.TopStart) {
                    when (selectedTab) {
                        0 -> {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
                                OutlinedTextField(
                                    value = textInput, onValueChange = { textInput = it }, modifier = Modifier.weight(1f).height(50.dp),
                                    singleLine = true, label = { Text("Keyboard Output", fontSize = 10.sp) }, textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Button(onClick = {
                                    if (textInput.isNotBlank()) viewModel.addAction(Action.Text(textInput))
                                    textInput = ""
                                }, modifier = Modifier.height(50.dp)) { Text("Add") }
                            }
                        }
                        1 -> {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { viewModel.addAction(Action.Click(ClickType.LEFT)) }, modifier = Modifier.height(36.dp)) { Text("Left Click", fontSize = 11.sp) }
                                Button(onClick = { viewModel.addAction(Action.Click(ClickType.RIGHT)) }, modifier = Modifier.height(36.dp)) { Text("Right Click", fontSize = 11.sp) }
                                Button(onClick = { viewModel.addAction(Action.DoubleClick(ClickType.LEFT)) }, modifier = Modifier.height(36.dp)) { Text("Double Click", fontSize = 11.sp) }
                            }
                        }
                        2 -> {
                            val specialKeys = listOf("enter", "escape", "tab", "backspace", "space", "up", "down", "left", "right")
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                specialKeys.forEach { key ->
                                    OutlinedButton(onClick = { viewModel.addAction(Action.KeyPress(key)) }, modifier = Modifier.height(32.dp), contentPadding = PaddingValues(horizontal = 8.dp)) {
                                        Text(key.uppercase(), fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                        3 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("ctrl", "alt", "shift", "win").forEach { mod ->
                                        FilterChip(
                                            selected = hkModifiers.contains(mod),
                                            onClick = { hkModifiers = if (hkModifiers.contains(mod)) hkModifiers - mod else hkModifiers + mod },
                                            label = { Text(mod.uppercase(), fontSize = 10.sp) }, modifier = Modifier.height(28.dp)
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = hkKey, onValueChange = { hkKey = it }, label = { Text("Key (e.g. c, v)", fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f).height(50.dp), singleLine = true, textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            if (hkKey.isNotBlank()) {
                                                viewModel.addAction(Action.Hotkey(hkKey.lowercase(), hkModifiers.toList()))
                                                hkKey = ""
                                            }
                                        }, modifier = Modifier.height(50.dp)
                                    ) { Text("Add") }
                                }
                            }
                        }
                        4 -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                                OutlinedButton(onClick = { viewModel.addAction(Action.Scroll("vertical", -3)) }) { Text("Scroll Up") }
                                OutlinedButton(onClick = { viewModel.addAction(Action.Scroll("vertical", 3)) }) { Text("Scroll Down") }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Execute Button
                Button(
                    onClick = { viewModel.sendPayload(deviceId) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    // SHADCN FIX: Maintain dynamic text contrast based on background color
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.sendStatus == SendStatus.SUCCESS) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                        contentColor = if (state.sendStatus == SendStatus.SUCCESS) Color.White else MaterialTheme.colorScheme.onPrimary
                    ),
                    enabled = state.sendStatus != SendStatus.SENDING && state.actions.isNotEmpty()
                ) {
                    if (state.sendStatus == SendStatus.SENDING) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(if (state.sendStatus == SendStatus.SUCCESS) Icons.Default.Check else Icons.Default.Send, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (state.sendStatus == SendStatus.SUCCESS) "SENT SUCCESSFULLY" else "EXECUTE SEQUENCE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}