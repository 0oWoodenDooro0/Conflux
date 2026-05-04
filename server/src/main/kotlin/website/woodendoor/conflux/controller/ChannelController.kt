package website.woodendoor.conflux.controller

import website.woodendoor.conflux.WebSocketConnectionManager
import website.woodendoor.conflux.database.repositories.ChannelRepository
import website.woodendoor.conflux.database.repositories.ServerRepository
import website.woodendoor.conflux.models.Channel
import website.woodendoor.conflux.models.ConfluxEvent
import website.woodendoor.conflux.models.CreateChannelRequest
import website.woodendoor.conflux.models.UpdateChannelRequest
import io.ktor.websocket.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

class ChannelController(
    private val channelRepository: ChannelRepository,
    private val serverRepository: ServerRepository,
    private val connectionManager: WebSocketConnectionManager
) {

    suspend fun editChannel(channelId: String, request: UpdateChannelRequest): OperationResult<Channel> {
        val existingChannel = channelRepository.getChannel(channelId)
            ?: return OperationResult.Failure.NotFound("Channel not found")

        val updatedChannel = existingChannel.copy(
            name = request.name ?: existingChannel.name,
            type = request.type ?: existingChannel.type,
            topic = request.topic ?: existingChannel.topic
        )

        val success = channelRepository.updateChannel(updatedChannel)
        return if (success) {
            broadcastChannelUpdated(updatedChannel.serverId, updatedChannel)
            OperationResult.Success(updatedChannel)
        } else {
            OperationResult.Failure.InternalError("Failed to update channel")
        }
    }

    private suspend fun broadcastChannelUpdated(serverId: String, channel: Channel) {
        val connections = connectionManager.getConnectionsForServer(serverId)
        val event = ConfluxEvent.ChannelUpdated(channel)
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

    suspend fun deleteChannel(channelId: String): OperationResult<Unit> {
        val existingChannel = channelRepository.getChannel(channelId)
            ?: return OperationResult.Failure.NotFound("Channel not found")

        val success = channelRepository.deleteChannel(channelId)
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

    suspend fun createChannel(serverId: String, request: CreateChannelRequest): OperationResult<Channel> {
        if (serverRepository.getServer(serverId) == null) {
            return OperationResult.Failure.NotFound("Server not found")
        }

        val channel = request.toDomain(id = UUID.randomUUID().toString(), serverId = serverId)
        val created = channelRepository.createChannel(channel)
        
        return if (created != null) {
            OperationResult.Success(created)
        } else {
            OperationResult.Failure.InternalError("Failed to create channel")
        }
    }

    suspend fun getChannelsByServer(serverId: String): OperationResult<List<Channel>> {
        if (serverRepository.getServer(serverId) == null) {
            return OperationResult.Failure.NotFound("Server not found")
        }
        val channels = channelRepository.getChannelsByServer(serverId)
        return OperationResult.Success(channels)
    }

    suspend fun getChannel(channelId: String): OperationResult<Channel> {
        val channel = channelRepository.getChannel(channelId)
        return if (channel != null) {
            OperationResult.Success(channel)
        } else {
            OperationResult.Failure.NotFound("Channel not found")
        }
    }
}
