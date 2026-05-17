package website.woodendoor.conflux.database.repositories

import website.woodendoor.conflux.models.Channel
import website.woodendoor.conflux.models.ChannelPermissionOverride

interface ChannelRepository {
    suspend fun createChannel(channel: Channel): Channel?
    suspend fun getChannel(id: String): Channel?
    suspend fun getChannelsByServer(serverId: String): List<Channel>
    suspend fun updateChannel(channel: Channel): Boolean
    suspend fun deleteChannel(id: String): Boolean

    suspend fun getOverrides(channelId: String): List<ChannelPermissionOverride>
    suspend fun upsertOverride(channelId: String, override: ChannelPermissionOverride): Boolean
    suspend fun deleteOverride(overrideId: String): Boolean
    suspend fun getEffectivePermissions(serverId: String, channelId: String, userId: String): Long
}
