package website.woodendoor.conflux.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import website.woodendoor.conflux.api.ServerApiClient
import website.woodendoor.conflux.models.Server

@Composable
fun CreateServerScreen(onServerCreated: (Server) -> Unit = {}) {
    var serverName by remember { mutableStateOf("") }
    var iconUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val apiClient = remember { ServerApiClient("http://localhost:8080") }

    DisposableEffect(Unit) {
        onDispose {
            apiClient.close()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create a New Server", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        
        TextField(
            value = serverName,
            onValueChange = { serverName = it },
            label = { Text("Server Name") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        TextField(
            value = iconUrl,
            onValueChange = { iconUrl = it },
            label = { Text("Icon URL (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    try {
                        val server = apiClient.createServer(serverName, iconUrl.ifBlank { null })
                        message = "Success! Server ID: ${server.id}"
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
        
        if (message.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(message, color = if (message.startsWith("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
        }
    }
}
