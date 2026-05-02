package website.woodendoor.conflux.state

import website.woodendoor.conflux.api.ServerApiClient
import website.woodendoor.conflux.models.Channel
import website.woodendoor.conflux.models.Server

object MainState {
    var selectedServer: Server? = null
    var channelList: List<Channel> = emptyList()
    var isFetchingChannels: Boolean = false
    var channelFetchError: String? = null

    suspend fun selectServer(server: Server, apiClient: ServerApiClient) {
        selectedServer = server
        channelList = emptyList()
        isFetchingChannels = true
        channelFetchError = null

        try {
            channelList = apiClient.getChannels(server.id)
        } catch (e: Exception) {
            channelFetchError = e.message ?: "Unknown error"
        } finally {
            isFetchingChannels = false
        }
    }

    fun reset() {
        selectedServer = null
        channelList = emptyList()
        isFetchingChannels = false
        channelFetchError = null
    }
}
