package website.woodendoor.conflux.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import website.woodendoor.conflux.api.ServerApiClient
import website.woodendoor.conflux.models.Channel
import website.woodendoor.conflux.state.MainState

@Composable
fun ChannelSettingsDialog(
    channel: Channel,
    apiClient: ServerApiClient,
    onDismissRequest: () -> Unit
) {
    var name by remember { mutableStateOf(channel.name) }
    var isDeleting by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxSize().padding(16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Channel Settings: #${channel.name}")
                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text("Channel Name", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("new-channel-name") }
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                HorizontalDivider()
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text("Danger Zone", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { isDeleting = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Channel")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        try {
                            val userId = MainState.currentUserId ?: return@launch
                            apiClient.updateChannel(
                                serverId = channel.serverId,
                                channelId = channel.id,
                                userId = userId,
                                name = name
                            )
                            onDismissRequest()
                        } catch (e: Exception) {
                            errorMessage = "Failed to update channel: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = name.isNotBlank() && name != channel.name && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Save Changes")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )

    if (isDeleting) {
        AlertDialog(
            onDismissRequest = { isDeleting = false },
            title = { Text("Delete Channel") },
            text = { Text("Are you sure you want to delete #${channel.name}? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                val userId = MainState.currentUserId ?: return@launch
                                val success = apiClient.deleteChannel(channel.serverId, channel.id, userId)
                                if (success) {
                                    onDismissRequest()
                                } else {
                                    errorMessage = "Failed to delete channel"
                                }
                            } catch (e: Exception) {
                                errorMessage = "Error: ${e.message}"
                            } finally {
                                isLoading = false
                                isDeleting = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { isDeleting = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
