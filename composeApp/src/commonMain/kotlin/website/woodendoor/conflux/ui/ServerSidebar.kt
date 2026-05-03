package website.woodendoor.conflux.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import website.woodendoor.conflux.models.Server

@Composable
fun ServerSidebar(
    servers: List<Server>,
    onServerClick: (Server) -> Unit,
    onHomeClick: () -> Unit,
    onCreateServerClick: () -> Unit,
    onJoinServerClick: () -> Unit
) {
    val copyToClipboard = rememberClipboardHelper()

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(72.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Home Button
        ServerIcon(
            name = "H",
            onClick = onHomeClick,
            backgroundColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
        
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        
        // Server List
        LazyColumn(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(servers) { server ->
                DebugContextMenu(
                    ids = mapOf(
                        "Server ID" to server.id,
                        "My User ID" to (website.woodendoor.conflux.state.MainState.currentUserId ?: "Unknown")
                    ),
                    onCopy = copyToClipboard
                ) {
                    ServerIcon(
                        name = server.name.take(1).uppercase(),
                        onClick = { onServerClick(server) }
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Add Server Button
        ServerIcon(
            name = "+",
            onClick = onCreateServerClick,
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Join Server Button
        ServerIcon(
            name = "J",
            onClick = onJoinServerClick,
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

@Composable
fun ServerIcon(
    name: String,
    onClick: () -> Unit,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape),
        color = backgroundColor,
        contentColor = contentColor,
        tonalElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
