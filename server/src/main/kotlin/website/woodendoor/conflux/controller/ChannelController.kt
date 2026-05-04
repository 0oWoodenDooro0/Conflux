package website.woodendoor.conflux.controller

import website.woodendoor.conflux.database.repositories.ChannelRepository
import website.woodendoor.conflux.models.Channel
import website.woodendoor.conflux.models.CreateChannelRequest
import java.util.*

class ChannelController(private val channelRepository: ChannelRepository) {

    suspend fun createChannel(serverId: String, request: CreateChannelRequest): OperationResult<Channel> {
        val channel = request.toDomain(id = UUID.randomUUID().toString(), serverId = serverId)
        val created = channelRepository.createChannel(channel)
        
        return if (created != null) {
            OperationResult.Success(created)
        } else {
            OperationResult.Failure.InternalError("Failed to create channel")
        }
    }

    suspend fun getChannelsByServer(serverId: String): OperationResult<List<Channel>> {
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
