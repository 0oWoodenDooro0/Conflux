package website.woodendoor.conflux.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import website.woodendoor.conflux.api.ServerApiClient
import website.woodendoor.conflux.models.Channel

@Composable
fun ChannelCreationDialog(
    serverId: String,
    apiClient: ServerApiClient,
    onChannelCreated: (Channel) -> Unit,
    onDismissRequest: () -> Unit
) {
    var channelName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Create Channel") },
        text = {
            Column {
                TextField(
                    value = channelName,
                    onValueChange = {
                        channelName = it
                        validationError = when (val result = ChannelValidator.validateName(it)) {
                            is ValidationResult.Error -> result.message
                            ValidationResult.Success -> null
                        }
                    },
                    label = { Text("Channel Name") },
                    isError = validationError != null,
                    supportingText = {
                        if (validationError != null) {
                            Text(validationError!!, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val result = ChannelValidator.validateName(channelName)
                    if (result is ValidationResult.Error) {
                        validationError = result.message
                        return@Button
                    }

                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        try {
                            val channel = apiClient.createChannel(serverId, channelName)
                            onChannelCreated(channel)
                            onDismissRequest()
                        } catch (e: Exception) {
                            errorMessage = "Failed to create channel: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading && validationError == null && channelName.isNotBlank()
            ) {
                Text(if (isLoading) "Creating..." else "Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest, enabled = !isLoading) {
                Text("Cancel")
            }
        }
    )
}
