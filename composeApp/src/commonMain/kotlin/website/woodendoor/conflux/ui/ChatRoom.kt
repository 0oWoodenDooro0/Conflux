package website.woodendoor.conflux.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import website.woodendoor.conflux.api.ServerApiClient
import website.woodendoor.conflux.formatTimestamp
import website.woodendoor.conflux.models.Message
import website.woodendoor.conflux.models.ConfluxPermission
import website.woodendoor.conflux.models.User
import website.woodendoor.conflux.state.LoginState
import website.woodendoor.conflux.state.MainState

@Composable
fun ChatRoom(apiClient: ServerApiClient) {
    val channel = MainState.selectedChannel ?: return
    val messages = MainState.messages
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val copyToClipboard = rememberClipboardHelper()

    var members by remember { mutableStateOf<List<User>>(emptyList()) }
    LaunchedEffect(channel.serverId) {
        try {
            members = apiClient.getMembers(channel.serverId)
        } catch (e: Exception) {
            println("Failed to fetch members: ${e.message}")
        }
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    val user = members.find { it.id == message.authorId }
                    val displayName = user?.username ?: "User ${message.authorId}"
                    val formattedTime = formatTimestamp(message.timestamp)

                    DebugContextMenu(
                        ids = mapOf(
                            "Message ID" to message.id,
                            "Author ID" to message.authorId
                        ),
                        onCopy = copyToClipboard
                    ) {
                        MessageItem(
                            message = message,
                            authorName = displayName,
                            formattedTime = formattedTime
                        )
                    }
                }
            }
            
            // Scroll to Bottom Button
            if (listState.firstVisibleItemIndex < messages.size - 10 && messages.isNotEmpty()) {
                SmallFloatingActionButton(
                    onClick = {
                        scope.launch {
                            listState.animateScrollToItem(messages.size - 1)
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
                ) {
                    Text("↓")
                }
            }
        }

        MessageInput(
            onSendMessage = { content ->
                val user = LoginState.currentUser ?: return@MessageInput
                scope.launch {
                    MainState.sendMessage(user.id, content, apiClient)
                }
            },
            sendError = MainState.messageSendError,
            enabled = (MainState.currentChannelPermissions and ConfluxPermission.MESSAGING) != 0L
        )
    }
}

@Composable
fun MessageItem(message: Message, authorName: String, formattedTime: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = authorName,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun MessageInput(
    onSendMessage: (String) -> Unit,
    sendError: String?,
    enabled: Boolean = true
) {
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(enabled) {
        if (enabled) {
            focusRequester.requestFocus()
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        if (sendError != null) {
            Text(
                text = sendError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = text,
                onValueChange = { if (it.length <= 2000) text = it },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onKeyEvent {
                        if (enabled && it.type == KeyEventType.KeyDown && it.key == Key.Enter) {
                            if (text.isNotBlank()) {
                                onSendMessage(text)
                                text = ""
                            }
                            true
                        } else {
                            false
                        }
                    },
                placeholder = { Text(if (enabled) "Message..." else "You do not have permission to send messages here.") },
                maxLines = 4,
                enabled = enabled
            )
            IconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onSendMessage(text)
                        text = ""
                    }
                },
                enabled = enabled && text.isNotBlank()
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
        Text(
            text = "${text.length}/2000",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
        )
    }
}
