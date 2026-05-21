package website.woodendoor.conflux.service.impl

import website.woodendoor.conflux.database.repositories.ChannelRepository
import website.woodendoor.conflux.models.Channel
import website.woodendoor.conflux.models.ChannelType
import website.woodendoor.conflux.service.ChannelPermissionService
import website.woodendoor.conflux.service.ChannelService
import java.util.UUID

class ChannelServiceImpl(
    private val channelRepository: ChannelRepository,
    private val channelPermissionService: ChannelPermissionService
) : ChannelService {

    override suspend fun createChannel(serverId: String, name: String, type: ChannelType, topic: String?): Channel? {
        val channel = Channel(
            id = UUID.randomUUID().toString(),
            serverId = serverId,
            name = name,
            type = type,
            topic = topic
        )
        val created = channelRepository.createChannel(channel)
        if (created != null) {
            channelPermissionService.createEveryoneOverride(serverId, created.id)
        }
        return created
    }

    override suspend fun getChannel(id: String): Channel? {
        return channelRepository.getChannel(id)
    }

    override suspend fun getChannelsByServer(serverId: String): List<Channel> {
        return channelRepository.getChannelsByServer(serverId)
    }

    override suspend fun updateChannel(channel: Channel): Boolean {
        return channelRepository.updateChannel(channel)
    }

    override suspend fun deleteChannel(id: String): Boolean {
        return channelRepository.deleteChannel(id)
    }
}
