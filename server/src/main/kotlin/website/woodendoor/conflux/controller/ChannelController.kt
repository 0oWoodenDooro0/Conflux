package website.woodendoor.conflux.controller

import website.woodendoor.conflux.WebSocketConnectionManager
import website.woodendoor.conflux.models.*
import website.woodendoor.conflux.service.ChannelPermissionService
import website.woodendoor.conflux.service.ChannelService
import website.woodendoor.conflux.service.ServerService
import io.ktor.websocket.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

class ChannelController(
    private val channelService: ChannelService,
    private val channelPermissionService: ChannelPermissionService,
    private val serverService: ServerService,
    private val connectionManager: WebSocketConnectionManager
) {

    suspend fun getOverrides(channelId: String, userId: String): OperationResult<List<ChannelPermissionOverride>> {
        val channel = channelService.getChannel(channelId) ?: return OperationResult.Failure.NotFound("Channel not found")
        if (!hasPermission(channel.serverId, channelId, userId, ConfluxPermission.CHANNEL_MANAGEMENT)) {
            return OperationResult.Failure.Forbidden("Insufficient permissions")
        }
        val overrides = channelPermissionService.getOverrides(channelId)
        return OperationResult.Success(overrides)
    }

    suspend fun upsertOverride(channelId: String, userId: String, request: UpsertOverrideRequest): OperationResult<Unit> {
        val channel = channelService.getChannel(channelId) ?: return OperationResult.Failure.NotFound("Channel not found")
        if (!hasPermission(channel.serverId, channelId, userId, ConfluxPermission.CHANNEL_MANAGEMENT)) {
            return OperationResult.Failure.Forbidden("Insufficient permissions")
        }

        val success = channelPermissionService.upsertOverride(channelId, request)
        return if (success) {
            broadcastPermissionUpdate(channel.serverId)
            OperationResult.Success(Unit)
        } else {
            OperationResult.Failure.InternalError("Failed to upsert override")
        }
    }

    suspend fun deleteOverride(serverId: String, userId: String, overrideId: String): OperationResult<Unit> {
        val channels = channelService.getChannelsByServer(serverId)
        var foundOverride: ChannelPermissionOverride? = null
        for (channel in channels) {
            val overrides = channelPermissionService.getOverrides(channel.id)
            val matching = overrides.find { it.id == overrideId }
            if (matching != null) {
                foundOverride = matching
                break
            }
        }
        if (foundOverride == null) {
            return OperationResult.Failure.NotFound("Override not found or failed to delete")
        }
        if (!hasPermission(serverId, foundOverride.channelId, userId, ConfluxPermission.CHANNEL_MANAGEMENT)) {
            return OperationResult.Failure.Forbidden("Insufficient permissions")
        }

        val success = channelPermissionService.deleteOverride(serverId, overrideId)
        return if (success) {
            broadcastPermissionUpdate(serverId)
            OperationResult.Success(Unit)
        } else {
            OperationResult.Failure.BadRequest("Cannot delete the @everyone override")
        }
    }

    private suspend fun broadcastPermissionUpdate(serverId: String) {
        val connections = connectionManager.getConnectionsForServer(serverId)
        val event = ConfluxEvent.PermissionUpdate(serverId)
        val eventJson = Json.encodeToString<ConfluxEvent>(event)
        
        coroutineScope {
            connections.forEach { session ->
                launch {
                    try {
                        session.send(Frame.Text(eventJson))
                    } catch (e: Exception) {
                        // Session might be closed
                    }
                }
            }
        }
    }

    suspend fun getEffectivePermissions(serverId: String, channelId: String, userId: String): OperationResult<Long> {
        val permissions = channelPermissionService.getEffectivePermissions(serverId, channelId, userId)
        return OperationResult.Success(permissions)
    }

    suspend fun hasPermission(serverId: String, channelId: String, userId: String, permission: Long): Boolean {
        return channelPermissionService.hasPermission(serverId, channelId, userId, permission)
    }

    suspend fun editChannel(channelId: String, userId: String, request: UpdateChannelRequest): OperationResult<Channel> {
        val existingChannel = channelService.getChannel(channelId)
            ?: return OperationResult.Failure.NotFound("Channel not found")
        if (!hasPermission(existingChannel.serverId, channelId, userId, ConfluxPermission.CHANNEL_MANAGEMENT)) {
            return OperationResult.Failure.Forbidden("Insufficient permissions")
        }

        val updatedChannel = existingChannel.copy(
            name = request.name ?: existingChannel.name,
            type = request.type ?: existingChannel.type,
            topic = request.topic ?: existingChannel.topic,
            categoryId = request.categoryId ?: existingChannel.categoryId,
            position = request.position ?: existingChannel.position
        )

        val success = channelService.updateChannel(updatedChannel)
        return if (success) {
            broadcastChannelUpdated(updatedChannel.serverId, updatedChannel)
            OperationResult.Success(updatedChannel)
        } else {
            OperationResult.Failure.InternalError("Failed to update channel")
        }
    }

    private suspend fun broadcastChannelUpdated(serverId: String, channel: Channel) {
        val userIds = connectionManager.getServerSubscribers(serverId)
        
        coroutineScope {
            userIds.forEach { userId ->
                launch {
                    if (hasPermission(serverId, channel.id, userId, ConfluxPermission.VIEW_CHANNEL)) {
                        val userSpecificChannel = channel.copy(
                            canManage = hasPermission(serverId, channel.id, userId, ConfluxPermission.CHANNEL_MANAGEMENT)
                        )
                        val event = ConfluxEvent.ChannelUpdated(userSpecificChannel)
                        val eventJson = Json.encodeToString<ConfluxEvent>(event)
                        connectionManager.getUserSessions(userId).forEach { session ->
                            try {
                                session.send(Frame.Text(eventJson))
                            } catch (e: Exception) {
                                // Session might be closed
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun deleteChannel(channelId: String, userId: String): OperationResult<Unit> {
        val existingChannel = channelService.getChannel(channelId)
            ?: return OperationResult.Failure.NotFound("Channel not found")
        if (!hasPermission(existingChannel.serverId, channelId, userId, ConfluxPermission.CHANNEL_MANAGEMENT)) {
            return OperationResult.Failure.Forbidden("Insufficient permissions")
        }

        val success = channelService.deleteChannel(channelId)
        return if (success) {
            broadcastChannelDeleted(existingChannel.serverId, channelId)
            OperationResult.Success(Unit)
        } else {
            OperationResult.Failure.InternalError("Failed to delete channel")
        }
    }

    private suspend fun broadcastChannelDeleted(serverId: String, channelId: String) {
        val connections = connectionManager.getConnectionsForServer(serverId)
        val event = ConfluxEvent.ChannelDeleted(channelId, serverId)
        val eventJson = Json.encodeToString<ConfluxEvent>(event)
        
        coroutineScope {
            connections.forEach { session ->
                launch {
                    try {
                        session.send(Frame.Text(eventJson))
                    } catch (e: Exception) {
                        // Session might be closed
                    }
                }
            }
        }
    }

    suspend fun createChannel(serverId: String, userId: String, request: CreateChannelRequest): OperationResult<Channel> {
        if (!hasPermission(serverId, channelId = "", userId, ConfluxPermission.CHANNEL_MANAGEMENT)) {
            return OperationResult.Failure.Forbidden("Insufficient permissions")
        }
        if (serverService.getServer(serverId) == null) {
            return OperationResult.Failure.NotFound("Server not found")
        }

        val created = channelService.createChannel(serverId, request.name, request.type, request.topic, request.categoryId, request.position)
        return if (created != null) {
            broadcastChannelCreated(serverId, created)
            OperationResult.Success(created)
        } else {
            OperationResult.Failure.InternalError("Failed to create channel")
        }
    }

    private suspend fun broadcastChannelCreated(serverId: String, channel: Channel) {
        val userIds = connectionManager.getServerSubscribers(serverId)
        
        coroutineScope {
            userIds.forEach { userId ->
                launch {
                    if (hasPermission(serverId, channel.id, userId, ConfluxPermission.VIEW_CHANNEL)) {
                        val userSpecificChannel = channel.copy(
                            canManage = hasPermission(serverId, channel.id, userId, ConfluxPermission.CHANNEL_MANAGEMENT)
                        )
                        val event = ConfluxEvent.ChannelCreated(userSpecificChannel)
                        val eventJson = Json.encodeToString<ConfluxEvent>(event)
                        connectionManager.getUserSessions(userId).forEach { session ->
                            try {
                                session.send(Frame.Text(eventJson))
                            } catch (e: Exception) {
                                // Session might be closed
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun getChannelsByServer(serverId: String, userId: String? = null): OperationResult<List<Channel>> {
        if (serverService.getServer(serverId) == null) {
            return OperationResult.Failure.NotFound("Server not found")
        }
        val channels = channelService.getChannelsByServer(serverId)
        val filtered = if (userId != null) {
            channels.filter { channel ->
                hasPermission(serverId, channel.id, userId, ConfluxPermission.VIEW_CHANNEL)
            }.map { channel ->
                channel.copy(
                    canManage = hasPermission(serverId, channel.id, userId, ConfluxPermission.CHANNEL_MANAGEMENT)
                )
            }
        } else {
            channels
        }
        return OperationResult.Success(filtered)
    }

    suspend fun getChannel(channelId: String): OperationResult<Channel> {
        val channel = channelService.getChannel(channelId)
        return if (channel != null) {
            OperationResult.Success(channel)
        } else {
            OperationResult.Failure.NotFound("Channel not found")
        }
    }
}
