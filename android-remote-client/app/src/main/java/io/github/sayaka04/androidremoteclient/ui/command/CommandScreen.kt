package io.github.sayaka04.androidremoteclient.ui.command

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommandScreen(commandViewModel: CommandViewModel = viewModel()) {
    val commandState by commandViewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Command Composer", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Add New Action", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ClickType.values().forEach { type ->
                        FilledTonalButton(onClick = { commandViewModel.insertIntoActionClick(type) }) {
                            Text(type.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = commandState.textFieldInput, onValueChange = { commandViewModel.updateTextField(it) }, label = { Text("Keyboard Input") }, modifier = Modifier.weight(1f), singleLine = true)
                    Button(onClick = { commandViewModel.insertIntoActionTypeText(commandState.textFieldInput) }, enabled = commandState.textFieldInput.isNotBlank()) {
                        Text("Add")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Action Sequence", style = MaterialTheme.typography.titleMedium)
            if (commandState.actions.isNotEmpty()) {
                TextButton(onClick = { commandViewModel.clearActions() }) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
            itemsIndexed(items = commandState.actions, key = { index, action -> action.hashCode() + index }) { index, action ->
                ActionNodeItem(
                    index = index + 1,
                    isFirst = index == 0,
                    isLast = index == commandState.actions.lastIndex,
                    action = action,
                    onMoveUp = { commandViewModel.moveAction(index, index - 1) },
                    onMoveDown = { commandViewModel.moveAction(index, index + 1) },
                    onRemove = { commandViewModel.removeActionAt(index) },
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
            if (commandState.hostMessage.isNotEmpty()) {
                Text(text = "Status: ${commandState.hostMessage}", color = if (commandState.isHostTaskSuccessful) Color(0xFF2E7D32) else Color.Red, style = MaterialTheme.typography.bodyMedium)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { commandViewModel.sendCommandsToServer("1") }, modifier = Modifier.weight(1f), enabled = !commandState.isNetworkLoading && commandState.actions.isNotEmpty()) {
                    if (commandState.isNetworkLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White) else Text("Execute Queue")
                }
                OutlinedButton(onClick = { commandViewModel.requestDataFromHost("1") }, modifier = Modifier.weight(1f), enabled = !commandState.isNetworkLoading) {
                    Text("Check Host")
                }
            }
        }
    }
}

@Composable
fun ActionNodeItem(index: Int, isFirst: Boolean, isLast: Boolean, action: Action, onMoveUp: () -> Unit, onMoveDown: () -> Unit, onRemove: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), tonalElevation = 2.dp, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(28.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                Text(text = index.toString(), color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                val label = when (action) {
                    is Action.Click -> "Click ${action.button.name}"
                    is Action.Move -> "Move to (${action.x}, ${action.y})"
                    is Action.Text -> "Type: \"${action.value}\""
                }
                Text(text = label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            }
            IconButton(onClick = onMoveUp, enabled = !isFirst) { Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = null) }
            IconButton(onClick = onMoveDown, enabled = !isLast) { Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null) }
            IconButton(onClick = onRemove) { Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
        }
    }
}