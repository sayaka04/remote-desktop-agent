package io.github.sayaka04.androidremoteclient.ui.remote.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ComposerFab(
    isComposerOpen: Boolean,
    actionCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.padding(24.dp)) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = if (isComposerOpen) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            contentColor = if (isComposerOpen) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(if (isComposerOpen) Icons.Default.Close else Icons.Default.Edit, contentDescription = "Toggle")

            if (!isComposerOpen && actionCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-6).dp)
                        .size(20.dp)
                        .background(Color.Red, CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$actionCount", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}