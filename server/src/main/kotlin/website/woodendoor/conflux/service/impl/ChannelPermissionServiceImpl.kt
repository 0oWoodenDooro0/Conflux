package website.woodendoor.conflux.service.impl

import website.woodendoor.conflux.DEFAULT_ROLE_PRIORITY_EVERYONE
import website.woodendoor.conflux.database.repositories.ChannelRepository
import website.woodendoor.conflux.database.repositories.ServerRepository
import website.woodendoor.conflux.models.*
import website.woodendoor.conflux.service.ChannelPermissionService
import java.util.UUID

class ChannelPermissionServiceImpl(
    private val channelRepository: ChannelRepository,
    private val serverRepository: ServerRepository
) : ChannelPermissionService {

    override suspend fun getOverrides(channelId: String): List<ChannelPermissionOverride> {
        return channelRepository.getOverrides(channelId)
    }

    override suspend fun upsertOverride(channelId: String, request: UpsertOverrideRequest): Boolean {
        val override = ChannelPermissionOverride(
            id = UUID.randomUUID().toString(),
            channelId = channelId,
            targetId = request.targetId,
            targetType = request.targetType,
            allow = request.allow,
            deny = request.deny
        )
        return channelRepository.upsertOverride(channelId, override)
    }

    override suspend fun deleteOverride(serverId: String, overrideId: String): Boolean {
        val roles = serverRepository.getRoles(serverId)
        val everyoneRole = roles.find { it.priorityLevel == DEFAULT_ROLE_PRIORITY_EVERYONE }
        if (everyoneRole != null) {
            val channels = channelRepository.getChannelsByServer(serverId)
            for (channel in channels) {
                val overrides = channelRepository.getOverrides(channel.id)
                val matchingOverride = overrides.find { it.id == overrideId }
                if (matchingOverride != null) {
                    if (matchingOverride.targetId == everyoneRole.id) {
                        return false // Blocks "@everyone cannot delete override"
                    }
                    break
                }
            }
        }
        return channelRepository.deleteOverride(overrideId)
    }

    override suspend fun getEffectivePermissions(serverId: String, channelId: String, userId: String): Long {
        return channelRepository.getEffectivePermissions(serverId, channelId, userId)
    }

    override suspend fun hasPermission(serverId: String, channelId: String, userId: String, permission: Long): Boolean {
        val effectivePermissions = getEffectivePermissions(serverId, channelId, userId)
        return ConfluxPermission.hasPermission(effectivePermissions, permission)
    }

    override suspend fun createEveryoneOverride(serverId: String, channelId: String): Boolean {
        val roles = serverRepository.getRoles(serverId)
        val everyoneRole = roles.find { it.priorityLevel == DEFAULT_ROLE_PRIORITY_EVERYONE }
        return if (everyoneRole != null) {
            val override = ChannelPermissionOverride(
                id = UUID.randomUUID().toString(),
                channelId = channelId,
                targetId = everyoneRole.id,
                targetType = OverrideType.ROLE,
                allow = 0L,
                deny = 0L
            )
            channelRepository.upsertOverride(channelId, override)
        } else {
            false
        }
    }
}
