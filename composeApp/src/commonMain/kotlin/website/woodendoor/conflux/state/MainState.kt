package website.woodendoor.conflux.state

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import website.woodendoor.conflux.api.ServerApiClient
import website.woodendoor.conflux.api.WebSocketClient
import website.woodendoor.conflux.models.Channel
import website.woodendoor.conflux.models.ConfluxEvent
import website.woodendoor.conflux.models.Message
import website.woodendoor.conflux.models.Server

object MainState {
    var selectedServer by mutableStateOf<Server?>(null)
    var channelList by mutableStateOf<List<Channel>>(emptyList())
    var channelFetchError by mutableStateOf<String?>(null)

    var selectedChannel by mutableStateOf<Channel?>(null)
    var messages by mutableStateOf<List<Message>>(emptyList())
    var messageFetchError by mutableStateOf<String?>(null)
    var messageSendError by mutableStateOf<String?>(null)

    private var webSocketClient: WebSocketClient? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun initializeWebSocket(apiClient: ServerApiClient, userId: String, baseUrl: String) {
        if (webSocketClient != null) return
        
        val wsClient = WebSocketClient(io.ktor.client.HttpClient(), baseUrl)
        webSocketClient = wsClient
        
        wsClient.events.onEach { event ->
            when (event) {
                is ConfluxEvent.Connected -> {
                    // Sync messages for current channel if selected
                    selectedChannel?.let { channel ->
                        scope.launch {
                            try {
                                messages = apiClient.getMessages(channel.id)
                            } catch (e: Exception) {
                                messageFetchError = "Sync failed: ${e.message}"
                            }
                        }
                    }
                }
                is ConfluxEvent.NewMessage -> {
                    if (event.message.channelId == selectedChannel?.id) {
                        // Avoid duplicates if we just sent it and refreshed
                        if (messages.none { it.id == event.message.id }) {
                            messages = messages + event.message
                        }
                    }
                }
                is ConfluxEvent.Error -> {
                    // Could show a notification or log
                }
                is ConfluxEvent.SubscriptionSuccess -> {
                    // OK
                }
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

    suspend fun selectServer(server: Server, apiClient: ServerApiClient) {
        selectedServer = server
        channelList = emptyList()
        channelFetchError = null
        selectedChannel = null
        messages = emptyList()

        try {
            channelList = apiClient.getChannels(server.id)
        } catch (e: Exception) {
            channelFetchError = e.message ?: "Unknown error"
        }
    }

    suspend fun selectChannel(channel: Channel, apiClient: ServerApiClient) {
        selectedChannel = channel
        messages = emptyList()
        messageFetchError = null
        
        try {
            messages = apiClient.getMessages(channel.id)
            webSocketClient?.subscribe(channel.id)
        } catch (e: Exception) {
            messageFetchError = e.message ?: "Unknown error"
        }
    }

    suspend fun sendMessage(senderId: String, content: String, apiClient: ServerApiClient) {
        val channelId = selectedChannel?.id ?: return
        messageSendError = null
        
        try {
            apiClient.sendMessage(channelId, senderId, content)
            // No need to refresh messages manually here anymore if WebSocket works,
            // but keeping it for immediate feedback or as a fallback is OK.
            // Actually, let's let the WebSocket handle the update to avoid double-refresh issues,
            // or just refresh and let NewMessage deduplicate.
            messages = apiClient.getMessages(channelId)
        } catch (e: Exception) {
            messageSendError = e.message ?: "Unknown error"
        }
    }

    fun reset() {
        selectedServer = null
        channelList = emptyList()
        channelFetchError = null
        selectedChannel = null
        messages = emptyList()
        messageFetchError = null
        messageSendError = null
        webSocketClient?.close()
        webSocketClient = null
    }
}
