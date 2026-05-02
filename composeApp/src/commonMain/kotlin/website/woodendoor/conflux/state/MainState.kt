package website.woodendoor.conflux.state

import androidx.compose.runtime.*
import website.woodendoor.conflux.api.ServerApiClient
import website.woodendoor.conflux.models.Channel
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
        } catch (e: Exception) {
            messageFetchError = e.message ?: "Unknown error"
        }
    }

    suspend fun sendMessage(senderId: String, content: String, apiClient: ServerApiClient) {
        val channelId = selectedChannel?.id ?: return
        messageSendError = null
        
        try {
            apiClient.sendMessage(channelId, senderId, content)
            // Refresh messages
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
    }
}
