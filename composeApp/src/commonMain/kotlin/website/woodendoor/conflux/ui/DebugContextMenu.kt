package website.woodendoor.conflux.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Composable
fun DebugContextMenu(
    ids: Map<String, String>,
    onCopy: (String) -> Unit,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var offset by remember { mutableStateOf(DpOffset.Zero) }

    Box(
        modifier = Modifier.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    // Check for secondary (right) click
                    if (event.buttons.isSecondaryPressed) {
                        val change = event.changes.first()
                        offset = DpOffset(change.position.x.toDp(), change.position.y.toDp())
                        expanded = true
                    }
                }
            }
        }
    ) {
        content()
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = offset
        ) {
            Text(
                "Developer Debug",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )
            ids.forEach { (label, id) ->
                DropdownMenuItem(
                    text = { 
                        Column {
                            Text(label, style = MaterialTheme.typography.labelSmall)
                            Text(id, style = MaterialTheme.typography.bodySmall)
                        }
                    },
                    onClick = {
                        onCopy(id)
                        expanded = false
                    },
                    trailingIcon = {
                        Text("Copy", style = MaterialTheme.typography.labelSmall)
                    }
                )
            }
        }
    }
}
