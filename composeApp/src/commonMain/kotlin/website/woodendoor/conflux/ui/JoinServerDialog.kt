package website.woodendoor.conflux.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import website.woodendoor.conflux.api.ServerApiClient
import website.woodendoor.conflux.state.LoginState
import website.woodendoor.conflux.state.MainState
import website.woodendoor.conflux.validation.ServerIdValidator

@Composable
fun JoinServerDialog(
    apiClient: ServerApiClient,
    onDismissRequest: () -> Unit,
    onJoined: () -> Unit
) {
    var serverId by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val user = LoginState.currentUser ?: return

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Join Server") },
        text = {
            Column {
                Text("Enter the Server ID (UUID) to join an existing server.")
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = serverId,
                    onValueChange = {
                        serverId = it
                        isError = false
                    },
                    label = { Text("Server ID") },
                    isError = isError,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (ServerIdValidator.isValid(serverId)) {
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            try {
                                val success = MainState.joinServer(serverId, user.id, apiClient)
                                if (success) {
                                    onJoined()
                                } else {
                                    isError = true
                                    errorMessage = "Could not join server. Check the ID or if you are already a member."
                                }
                            } catch (e: Exception) {
                                isError = true
                                errorMessage = e.message ?: "An unexpected error occurred"
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        isError = true
                        errorMessage = "Invalid Server ID format (UUID expected)"
                    }
                },
                enabled = serverId.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Join")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest, enabled = !isLoading) {
                Text("Cancel")
            }
        }
    )
}
