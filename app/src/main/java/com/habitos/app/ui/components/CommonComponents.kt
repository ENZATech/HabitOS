package com.habitos.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.habitos.app.data.models.Task
import com.habitos.app.ui.theme.*

@Composable
fun HabitOSCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = Surface),
        border   = BorderStroke(1.dp, Border)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun TaskRow(
    task: Task,
    onToggleComplete: () -> Unit,
    onToggleImportant: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Completion checkbox
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = if (task.isCompleted) AccentEmerald else Border,
                    shape = CircleShape
                )
                .background(if (task.isCompleted) AccentEmerald else Color.Transparent)
                .clickable { onToggleComplete() },
            contentAlignment = Alignment.Center
        ) {
            if (task.isCompleted) {
                Icon(
                    Icons.Default.Check, contentDescription = "Done",
                    tint   = Background,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // Title
        Text(
            text     = task.title,
            style    = MaterialTheme.typography.bodyLarge,
            color    = if (task.isCompleted) OnSurfaceDim else OnSurface,
            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // Important star
        IconButton(onClick = onToggleImportant) {
            Icon(
                imageVector = if (task.isImportant) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = "Important",
                tint = if (task.isImportant) Amber else OnSurfaceMuted
            )
        }

        // Delete
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.DeleteOutline, contentDescription = "Delete",
                tint = OnSurfaceMuted
            )
        }
    }
}

@Composable
fun AddItemTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    placeholder: String = "Add a task…",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = OnSurfaceDim) },
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = AccentEmerald,
            unfocusedBorderColor = Border,
            focusedTextColor     = OnSurface,
            unfocusedTextColor   = OnSurface,
            cursorColor          = AccentEmerald,
            containerColor       = Surface,
        ),
        trailingIcon = {
            IconButton(onClick = onDone) {
                Icon(Icons.Default.Add, "Add", tint = AccentEmerald)
            }
        },
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            imeAction = androidx.compose.ui.text.input.ImeAction.Done
        ),
        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
            onDone = { onDone() }
        )
    )
}

@Composable
fun SectionHeader(title: String, count: Int = -1, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text  = title,
            style = MaterialTheme.typography.titleMedium,
            color = OnSurfaceMuted,
            modifier = Modifier.weight(1f)
        )
        if (count >= 0) {
            Text(
                text  = count.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceDim
            )
        }
    }
}

@Composable
fun ColorDot(colorHex: String, size: Int = 10, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(parseColor(colorHex))
    )
}

fun parseColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (e: Exception) {
    Color(0xFF10B981)
}
