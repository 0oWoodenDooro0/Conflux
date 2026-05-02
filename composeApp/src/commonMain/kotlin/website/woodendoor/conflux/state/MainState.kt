package website.woodendoor.conflux.state

import androidx.compose.runtime.*
import website.woodendoor.conflux.api.ServerApiClient
import website.woodendoor.conflux.models.Channel
import website.woodendoor.conflux.models.Server

object MainState {
    var selectedServer by mutableStateOf<Server?>(null)
    var channelList by mutableStateOf<List<Channel>>(emptyList())
    var channelFetchError by mutableStateOf<String?>(null)

    suspend fun selectServer(server: Server, apiClient: ServerApiClient) {
        selectedServer = server
        channelList = emptyList()
        channelFetchError = null

        try {
            channelList = apiClient.getChannels(server.id)
        } catch (e: Exception) {
            channelFetchError = e.message ?: "Unknown error"
        }
    }

    fun reset() {
        selectedServer = null
        channelList = emptyList()
        channelFetchError = null
    }
}
