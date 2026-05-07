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
import website.woodendoor.conflux.models.ConfluxPermission
import website.woodendoor.conflux.state.LoginState
import website.woodendoor.conflux.state.MainState

@Composable
fun MainScreen() {
    val user = LoginState.currentUser ?: return
    val apiClient = remember { ServerApiClient(DEFAULT_BASE_URL) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var isCreatingServer by remember { mutableStateOf(false) }
    var isJoiningServer by remember { mutableStateOf(false) }
    var isCreatingChannel by remember { mutableStateOf(false) }
    var isShowingSettings by remember { mutableStateOf(false) }
    var channelToEdit by remember { mutableStateOf<website.woodendoor.conflux.models.Channel?>(null) }
    val scope = rememberCoroutineScope()

    val servers = MainState.serverList
    val selectedServer = MainState.selectedServer
    val channels = MainState.channelList
    val channelFetchError = MainState.channelFetchError
    val snackbarHostState = remember { SnackbarHostState() }

    val canManageServer = (MainState.currentUserPermissions and (ConfluxPermission.ROLE_MANAGEMENT or ConfluxPermission.SERVER_MANAGEMENT)) != 0L
    val canManageChannels = (MainState.currentUserPermissions and ConfluxPermission.CHANNEL_MANAGEMENT) != 0L

    LaunchedEffect(canManageServer) {
        if (!canManageServer) {
            isShowingSettings = false
        }
    }

    LaunchedEffect(canManageChannels) {
        if (!canManageChannels) {
            isCreatingChannel = false
            channelToEdit = null
        }
    }

    LaunchedEffect(user.id, isCreatingServer, isJoiningServer) {
        if (!isCreatingServer && !isJoiningServer) {
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
                    isJoiningServer = false
                    scope.launch {
                        MainState.selectServer(server, apiClient)
                    }
                },
                onHomeClick = {
                    isCreatingServer = false
                    isJoiningServer = false
                    MainState.selectedServer = null
                },
                onCreateServerClick = { 
                    isCreatingServer = true 
                    isJoiningServer = false
                },
                onJoinServerClick = {
                    isJoiningServer = true
                    isCreatingServer = false
                }
            )

            if (selectedServer != null && !isCreatingServer && !isJoiningServer) {
                ChannelSidebar(
                    serverName = selectedServer.name,
                    channels = channels,
                    selectedChannelId = MainState.selectedChannel?.id,
                    canCreateChannel = canManageChannels,
                    canManageServer = canManageServer,
                    onCreateChannelClick = { isCreatingChannel = true },
                    onSettingsClick = { isShowingSettings = true },
                    onChannelClick = { channel ->
                        scope.launch {
                            MainState.selectChannel(channel, apiClient)
                        }
                    },
                    onChannelSettingsClick = { channel ->
                        channelToEdit = channel
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
                    CreateServerDialog(
                        apiClient = apiClient,
                        onDismissRequest = { isCreatingServer = false },
                        onServerCreated = {
                            isCreatingServer = false
                        }
                    )
                } else if (isJoiningServer) {
                    JoinServerDialog(
                        apiClient = apiClient,
                        onDismissRequest = { isJoiningServer = false },
                        onJoined = {
                            isJoiningServer = false
                        }
                    )
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
                } else if (isShowingSettings && selectedServer != null) {
                    ServerSettingsDialog(
                        server = selectedServer,
                        apiClient = apiClient,
                        onDismissRequest = { isShowingSettings = false }
                    )
                } else if (channelToEdit != null) {
                    ChannelSettingsDialog(
                        channel = channelToEdit!!,
                        apiClient = apiClient,
                        onDismissRequest = { channelToEdit = null }
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
