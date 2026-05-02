package website.woodendoor.conflux.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import website.woodendoor.conflux.DEFAULT_BASE_URL
import website.woodendoor.conflux.api.ServerApiClient
import website.woodendoor.conflux.models.Server
import website.woodendoor.conflux.state.LoginState

@Composable
fun MainScreen() {
    val user = LoginState.currentUser ?: return
    val apiClient = remember { ServerApiClient(DEFAULT_BASE_URL) }
    var servers by remember { mutableStateOf<List<Server>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var isCreatingServer by remember { mutableStateOf(false) }

    LaunchedEffect(user.id, isCreatingServer) {
        if (!isCreatingServer) {
            try {
                servers = apiClient.getServers(user.id)
            } catch (e: Exception) {
                error = "Failed to load servers: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        ServerSidebar(
            servers = servers,
            onServerClick = { isCreatingServer = false /* Later: select server */ },
            onHomeClick = { isCreatingServer = false },
            onCreateServerClick = { isCreatingServer = true }
        )

        // Main content placeholder
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (isCreatingServer) {
                CreateServerScreen(onServerCreated = {
                    isCreatingServer = false
                })
            } else if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            } else if (!isLoading) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Welcome, ${user.username}!", style = MaterialTheme.typography.headlineSmall)
                    Text("Select a server from the left to get started.", style = MaterialTheme.typography.bodyMedium)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Found ${servers.size} servers", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
