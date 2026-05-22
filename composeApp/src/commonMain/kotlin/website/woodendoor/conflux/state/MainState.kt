package website.woodendoor.conflux.state

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import website.woodendoor.conflux.api.ServerApiClient
import website.woodendoor.conflux.api.WebSocketClient
import website.woodendoor.conflux.models.Channel
import website.woodendoor.conflux.models.ConfluxEvent
import website.woodendoor.conflux.models.Message
import website.woodendoor.conflux.models.Server
import website.woodendoor.conflux.models.User

object MainState {
    var currentUserId by mutableStateOf<String?>(null)
    var currentUserPermissions by mutableStateOf<Long>(0L)
    var currentChannelPermissions by mutableStateOf<Long>(0L)

    var serverList by mutableStateOf<List<Server>>(emptyList())
    var selectedServer by mutableStateOf<Server?>(null)
    var channelList by mutableStateOf<List<Channel>>(emptyList())
    var channelFetchError by mutableStateOf<String?>(null)
    var unreadChannels by mutableStateOf<Set<String>>(emptySet())
    var unreadServerIds by mutableStateOf<Set<String>>(emptySet())

    var selectedChannel by mutableStateOf<Channel?>(null)
    var messages by mutableStateOf<List<Message>>(emptyList())
    var currentServerMembers by mutableStateOf<List<User>>(emptyList())
    var messageFetchError by mutableStateOf<String?>(null)
    var messageSendError by mutableStateOf<String?>(null)

    private var webSocketClient: WebSocketClient? = null
    // Use a fixed scope or ensure it's matched with the platform's Main dispatcher
    private val scope = CoroutineScope(Dispatchers.Main)

    fun initializeWebSocket(apiClient: ServerApiClient, userId: String, baseUrl: String) {
        currentUserId = userId
        if (webSocketClient != null) return
        
        val wsClient = WebSocketClient(io.ktor.client.HttpClient {
            install(io.ktor.client.plugins.websocket.WebSockets)
        }, baseUrl)
        webSocketClient = wsClient
        
        wsClient.events.onEach { event ->
            // Ensure state updates happen on the Main thread for Compose to see them
            withContext(Dispatchers.Main) {
                handleWebSocketEvent(event, apiClient)
            }
        }.launchIn(scope)

        scope.launch {
            try {
                val token = apiClient.getWsToken(userId)
                wsClient.connect(token)
            } catch (e: Exception) {
                // Handle token error
            }
        }
    }

    suspend fun handleWebSocketEvent(event: ConfluxEvent, apiClient: ServerApiClient) {
        when (event) {
            is ConfluxEvent.Connected -> {
                // Sync messages for current channel if selected
                selectedChannel?.let { channel ->
                    try {
                        messages = apiClient.getMessages(channel.id)
                    } catch (e: Exception) {
                        messageFetchError = "Sync failed: ${e.message}"
                    }
                }
            }
            is ConfluxEvent.NewMessage -> {
                val isCurrentChannel = event.message.channelId == selectedChannel?.id
                if (isCurrentChannel) {
                    // Avoid duplicates
                    if (messages.none { it.id == event.message.id }) {
                        messages = messages + event.message
                    }
                } else {
                    if (event.message.authorId != currentUserId) {
                        unreadChannels = unreadChannels + event.message.channelId
                        unreadServerIds = unreadServerIds + event.serverId
                    }
                }
            }
            is ConfluxEvent.Error -> {
                // Log or handle error
            }
            is ConfluxEvent.SubscriptionSuccess -> {
                // OK
            }
            is ConfluxEvent.ChannelCreated -> {
                if (event.channel.serverId == selectedServer?.id) {
                    if (channelList.none { it.id == event.channel.id }) {
                        channelList = channelList + event.channel
                    }
                }
            }
            is ConfluxEvent.ChannelUpdated -> {
                // Update channel list
                channelList = channelList.map {
                    if (it.id == event.channel.id) event.channel else it
                }
                // Update selected channel if it's the one being edited
                if (selectedChannel?.id == event.channel.id) {
                    selectedChannel = event.channel
                }
            }
            is ConfluxEvent.ChannelDeleted -> {
                val wasSelected = selectedChannel?.id == event.channelId
                
                // Clear from unreads
                unreadChannels = unreadChannels - event.channelId
                selectedServer?.let { server ->
                    val hasAnyUnreadOnServer = channelList.any { it.id in unreadChannels }
                    if (!hasAnyUnreadOnServer) {
                        unreadServerIds = unreadServerIds - server.id
                    }
                }
                
                // Remove channel from list
                channelList = channelList.filter { it.id != event.channelId }
                
                if (wasSelected) {
                    val nextChannel = channelList.firstOrNull()
                    if (nextChannel != null) {
                        selectChannel(nextChannel, apiClient)
                    } else {
                        selectedChannel = null
                        messages = emptyList()
                    }
                }
            }
            is ConfluxEvent.PermissionUpdate -> {
                if (event.serverId == selectedServer?.id) {
                    currentUserId?.let { userId ->
                        try {
                            currentUserPermissions = apiClient.getPermissions(event.serverId, userId)
                            currentServerMembers = apiClient.getMembers(event.serverId, selectedChannel?.id)
                            val oldChannelIds = channelList.map { it.id }.toSet()
                            val newChannels = apiClient.getChannels(event.serverId, userId)
                            val newChannelIds = newChannels.map { it.id }.toSet()
                            
                            val removedChannelIds = oldChannelIds - newChannelIds
                            unreadChannels = unreadChannels - removedChannelIds
                            
                            val hasAnyUnreadOnServer = newChannels.any { it.id in unreadChannels }
                            if (!hasAnyUnreadOnServer) {
                                unreadServerIds = unreadServerIds - event.serverId
                            }
                            
                            channelList = newChannels
                            
                            val wasSelected = selectedChannel != null
                            if (wasSelected && newChannels.none { it.id == selectedChannel?.id }) {
                                val nextChannel = newChannels.firstOrNull()
                                if (nextChannel != null) {
                                    selectChannel(nextChannel, apiClient)
                                } else {
                                    selectedChannel = null
                                    messages = emptyList()
                                }
                            } else {
                                selectedChannel?.let { channel ->
                                    currentChannelPermissions = apiClient.getChannelPermissions(event.serverId, channel.id, userId)
                                }
                            }
                        } catch (e: Exception) {
                            println("Failed to re-fetch permissions: ${e.message}")
                        }
                    }
                }
            }
            is ConfluxEvent.UserPresenceChanged -> {
                currentServerMembers = currentServerMembers.map {
                    if (it.id == event.userId) it.copy(isOnline = event.isOnline) else it
                }
            }
        }
    }

    suspend fun selectServer(server: Server, apiClient: ServerApiClient) {
        selectedServer = server
        channelList = emptyList()
        channelFetchError = null
        selectedChannel = null
        messages = emptyList()
        currentServerMembers = emptyList()
        currentUserPermissions = 0L
        currentChannelPermissions = 0L

        webSocketClient?.subscribeServer(server.id)

        try {
            channelList = apiClient.getChannels(server.id, currentUserId)
            currentServerMembers = apiClient.getMembers(server.id)
            currentUserId?.let { userId ->
                currentUserPermissions = apiClient.getPermissions(server.id, userId)
            }
        } catch (e: Exception) {
            channelFetchError = e.message ?: "Unknown error"
        }
    }

    suspend fun selectChannel(channel: Channel, apiClient: ServerApiClient) {
        selectedChannel = channel
        messages = emptyList()
        messageFetchError = null
        currentChannelPermissions = 0L

        // Clear notification
        unreadChannels = unreadChannels - channel.id
        selectedServer?.let { server ->
            val hasAnyUnreadOnServer = channelList.any { it.id in unreadChannels }
            if (!hasAnyUnreadOnServer) {
                unreadServerIds = unreadServerIds - server.id
            }
        }
        
        try {
            messages = apiClient.getMessages(channel.id)
            webSocketClient?.subscribe(channel.id)

            // Fetch hierarchical permissions for the channel
            selectedServer?.let { server ->
                currentUserId?.let { userId ->
                    currentChannelPermissions = apiClient.getChannelPermissions(server.id, channel.id, userId)
                }
                currentServerMembers = apiClient.getMembers(server.id, channel.id)
            }
        } catch (e: Exception) {
            messageFetchError = e.message ?: "Unknown error"
        }
    }

    suspend fun joinServer(serverId: String, userId: String, apiClient: ServerApiClient): Boolean {
        return try {
            val success = apiClient.joinServer(serverId, userId)
            if (success) {
                serverList = apiClient.getServers(userId)
                
                val joinedServer = serverList.find { it.id == serverId }
                if (joinedServer != null) {
                    selectServer(joinedServer, apiClient)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun sendMessage(senderId: String, content: String, apiClient: ServerApiClient) {
        val channelId = selectedChannel?.id ?: return
        messageSendError = null
        
        try {
            apiClient.sendMessage(channelId, senderId, content)
            // Manual refresh is now a fallback, but we'll keep it for immediate local feedback
            // while the WebSocket event will handle the deduplication and others' messages.
            messages = apiClient.getMessages(channelId)
        } catch (e: Exception) {
            messageSendError = e.message ?: "Unknown error"
        }
    }

    fun subscribeToAllServers() {
        val ws = webSocketClient ?: return
        scope.launch {
            serverList.forEach { server ->
                ws.subscribeServer(server.id)
            }
        }
    }

    fun reset() {
        serverList = emptyList()
        selectedServer = null
        channelList = emptyList()
        channelFetchError = null
        selectedChannel = null
        messages = emptyList()
        messageFetchError = null
        messageSendError = null
        unreadChannels = emptySet()
        unreadServerIds = emptySet()
        currentServerMembers = emptyList()
        webSocketClient?.close()
        webSocketClient = null
    }
}
