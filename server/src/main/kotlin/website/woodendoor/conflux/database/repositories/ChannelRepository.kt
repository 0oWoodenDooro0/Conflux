package website.woodendoor.conflux.database.repositories

import website.woodendoor.conflux.models.Channel

interface ChannelRepository {
    suspend fun createChannel(channel: Channel): Channel?
    suspend fun getChannel(id: String): Channel?
    suspend fun getChannelsByServer(serverId: String): List<Channel>
    suspend fun updateChannel(channel: Channel): Boolean
    suspend fun deleteChannel(id: String): Boolean
}
