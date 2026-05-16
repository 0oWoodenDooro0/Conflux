package website.woodendoor.conflux.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import website.woodendoor.conflux.api.ServerApiClient
import website.woodendoor.conflux.models.Server
import website.woodendoor.conflux.state.LoginState

@Composable
fun CreateServerDialog(
    apiClient: ServerApiClient,
    onServerCreated: (Server) -> Unit,
    onDismissRequest: () -> Unit
) {
    var serverName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Create New Server") },
        text = {
            Column {
                Text("Servers are where you and your friends hang out. Make yours and start talking.")
                Spacer(modifier = Modifier.height(16.dp))
                
                TextField(
                    value = serverName,
                    onValueChange = { serverName = it },
                    label = { Text("Server Name") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    placeholder = { Text("My Awesome Server") }
                )
                
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
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
                            val currentUser = LoginState.currentUser
                            if (currentUser == null) {
                                errorMessage = "You must be logged in."
                                return@launch
                            }
                            val server = apiClient.createServer(
                                name = serverName,
                                ownerId = currentUser.username
                            )
                            onServerCreated(server)
                            onDismissRequest()
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "An unknown error occurred"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = serverName.isNotBlank() && !isLoading
            ) {
                Text(if (isLoading) "Creating..." else "Create")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}
