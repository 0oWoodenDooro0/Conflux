package website.woodendoor.conflux.service

import website.woodendoor.conflux.models.Channel
import website.woodendoor.conflux.models.ChannelType

interface ChannelService {
    suspend fun createChannel(serverId: String, name: String, type: ChannelType, topic: String? = null): Channel?
    suspend fun getChannel(id: String): Channel?
    suspend fun getChannelsByServer(serverId: String): List<Channel>
    suspend fun updateChannel(channel: Channel): Boolean
    suspend fun deleteChannel(id: String): Boolean
}
