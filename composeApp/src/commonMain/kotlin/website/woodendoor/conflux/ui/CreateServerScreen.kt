package website.woodendoor.conflux.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import website.woodendoor.conflux.DEFAULT_BASE_URL
import website.woodendoor.conflux.api.ServerApiClient
import website.woodendoor.conflux.models.Server
import website.woodendoor.conflux.state.LoginState

@Composable
fun CreateServerScreen(onServerCreated: (Server) -> Unit = {}) {
    var serverName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var createdServer by remember { mutableStateOf<Server?>(null) }
    var showChannelDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val apiClient = remember { ServerApiClient(DEFAULT_BASE_URL) }

    DisposableEffect(Unit) {
        onDispose {
            apiClient.close()
        }
    }

    if (showChannelDialog && createdServer != null) {
        ChannelCreationDialog(
            serverId = createdServer!!.id,
            apiClient = apiClient,
            onChannelCreated = { channel ->
                message = "Success! Channel '${channel.name}' created in server '${createdServer!!.name}'"
            },
            onDismissRequest = { showChannelDialog = false }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (createdServer == null) {
            Text("Create a New Server", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))
            
            TextField(
                value = serverName,
                onValueChange = { serverName = it },
                label = { Text("Server Name") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            val currentUser = LoginState.currentUser
                            if (currentUser == null) {
                                message = "Error: You must be logged in to create a server."
                                isLoading = false
                                return@launch
                            }
                            val ownerId = currentUser.username
                            val server = apiClient.createServer(
                                name = serverName,
                                ownerId = ownerId
                            )
                            message = "Success! Server ID: ${server.id}"
                            createdServer = server
                            onServerCreated(server)
                        } catch (e: Exception) {
                            message = "Error: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = serverName.isNotBlank() && !isLoading
            ) {
                Text(if (isLoading) "Creating..." else "Create Server")
            }
        } else {
            Text("Server '${createdServer!!.name}' Created!", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { showChannelDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Channel (+)")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = {
                    createdServer = null
                    serverName = ""
                    message = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Another Server")
            }
        }
        
        if (message.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(message, color = if (message.startsWith("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
        }
    }
}
