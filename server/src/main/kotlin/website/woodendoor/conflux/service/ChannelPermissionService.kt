package website.woodendoor.conflux.service

import website.woodendoor.conflux.models.ChannelPermissionOverride
import website.woodendoor.conflux.models.UpsertOverrideRequest

interface ChannelPermissionService {
    suspend fun getOverrides(channelId: String): List<ChannelPermissionOverride>
    suspend fun upsertOverride(channelId: String, request: UpsertOverrideRequest): Boolean
    suspend fun deleteOverride(serverId: String, overrideId: String): Boolean
    suspend fun getEffectivePermissions(serverId: String, channelId: String, userId: String): Long
    suspend fun hasPermission(serverId: String, channelId: String, userId: String, permission: Long): Boolean
    suspend fun createEveryoneOverride(serverId: String, channelId: String): Boolean
}
