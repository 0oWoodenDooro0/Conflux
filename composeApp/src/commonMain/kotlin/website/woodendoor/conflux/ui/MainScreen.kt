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
import website.woodendoor.conflux.state.MainState

@Composable
fun MainScreen() {
    val user = LoginState.currentUser ?: return
    val apiClient = remember { ServerApiClient(DEFAULT_BASE_URL) }
    var servers by remember { mutableStateOf<List<Server>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var isCreatingServer by remember { mutableStateOf(false) }
    var isCreatingChannel by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val selectedServer = MainState.selectedServer
    val channels = MainState.channelList
    val channelFetchError = MainState.channelFetchError
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(user.id, isCreatingServer) {
        if (!isCreatingServer) {
            try {
                MainState.serverList = apiClient.getServers(user.id)
            } catch (e: Exception) {
                error = "Failed to load servers: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(channelFetchError) {
        if (channelFetchError != null) {
            snackbarHostState.showSnackbar(channelFetchError)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Row(modifier = Modifier.fillMaxSize().padding(padding)) {
            ServerSidebar(
                servers = servers,
                onServerClick = { server ->
                    isCreatingServer = false
                    scope.launch {
                        MainState.selectServer(server, apiClient)
                    }
                },
                onHomeClick = {
                    isCreatingServer = false
                    MainState.selectedServer = null
                },
                onCreateServerClick = { isCreatingServer = true }
            )

            if (selectedServer != null && !isCreatingServer) {
                ChannelSidebar(
                    serverName = selectedServer.name,
                    channels = channels,
                    selectedChannelId = MainState.selectedChannel?.id,
                    onCreateChannelClick = { isCreatingChannel = true },
                    onChannelClick = { channel ->
                        scope.launch {
                            MainState.selectChannel(channel, apiClient)
                        }
                    }
                )
            }
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
                } else if (isCreatingChannel && selectedServer != null) {
                    ChannelCreationDialog(
                        serverId = selectedServer.id,
                        apiClient = apiClient,
                        onDismissRequest = { isCreatingChannel = false },
                        onChannelCreated = { channel ->
                            isCreatingChannel = false
                            scope.launch {
                                // Refresh channels
                                MainState.selectServer(selectedServer, apiClient)
                            }
                        }
                    )
                } else if (MainState.selectedChannel != null) {
                    ChatRoom(apiClient = apiClient)
                } else {
                    val currentError = error
                    if (currentError != null) {
                        Text(currentError, color = MaterialTheme.colorScheme.error)
                    } else if (channelFetchError != null) {
                        Text(channelFetchError, color = MaterialTheme.colorScheme.error)
                    } else if (!isLoading) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (selectedServer != null) {
                                Text("Server: ${selectedServer.name}", style = MaterialTheme.typography.headlineSmall)
                                Text("Select a channel from the left.", style = MaterialTheme.typography.bodyMedium)
                            } else {
                                Text("Welcome, ${user.username}!", style = MaterialTheme.typography.headlineSmall)
                                Text("Select a server from the left to get started.", style = MaterialTheme.typography.bodyMedium)
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text("Found ${servers.size} servers", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
