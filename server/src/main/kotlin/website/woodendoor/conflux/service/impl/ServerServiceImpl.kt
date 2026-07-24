package website.woodendoor.conflux.service.impl

import website.woodendoor.conflux.database.repositories.ServerRepository
import website.woodendoor.conflux.database.repositories.UserRepository
import website.woodendoor.conflux.models.*
import website.woodendoor.conflux.service.ChannelService
import website.woodendoor.conflux.service.ServerService
import java.util.UUID

class ServerServiceImpl(
    private val serverRepository: ServerRepository,
    private val userRepository: UserRepository,
    private val channelService: ChannelService
) : ServerService {

    override suspend fun createServer(name: String, ownerId: String, icon: String?, description: String?): Server? {
        val owner = userRepository.getUser(ownerId) ?: userRepository.findByUsername(ownerId)
        val resolvedOwnerId = if (owner == null) {
            val newUserId = UUID.randomUUID().toString()
            userRepository.createUser(
                User(
                    id = newUserId,
                    username = ownerId,
                    discriminator = "0000"
                )
            )
            newUserId
        } else {
            owner.id
        }

        val server = Server(
            id = UUID.randomUUID().toString(),
            name = name,
            ownerId = resolvedOwnerId,
            icon = icon,
            description = description
        )
        val created = serverRepository.createServer(server)
        if (created != null) {
            try {
                // Create default Text Channels category & channel
                val textCat = channelService.createChannel(
                    serverId = created.id,
                    name = "TEXT CHANNELS",
                    type = ChannelType.CATEGORY
                )
                
                channelService.createChannel(
                    serverId = created.id,
                    name = "general",
                    type = ChannelType.TEXT,
                    topic = "Welcome to ${created.name}!",
                    categoryId = textCat?.id
                )

                // Create default Voice Channels category & channel
                val voiceCat = channelService.createChannel(
                    serverId = created.id,
                    name = "VOICE CHANNELS",
                    type = ChannelType.CATEGORY
                )

                channelService.createChannel(
                    serverId = created.id,
                    name = "General Voice",
                    type = ChannelType.VOICE,
                    categoryId = voiceCat?.id
                )
            } catch (e: Exception) {
                println("Failed to create default channels for server ${created.id}: ${e.message}")
            }
        }
        return created
    }

    override suspend fun getServer(id: String): Server? {
        return serverRepository.getServer(id)
    }

    override suspend fun getServersForUser(userId: String): List<Server> {
        return serverRepository.getServersForUser(userId)
    }

    override suspend fun getMembers(serverId: String): List<User> {
        return serverRepository.getMembers(serverId)
    }

    override suspend fun getPermissionsForMember(serverId: String, userId: String): Long {
        return serverRepository.getPermissionsForMember(serverId, userId)
    }

    override suspend fun joinServer(userId: String, serverId: String): Boolean {
        return serverRepository.joinServer(userId, serverId)
    }

    override suspend fun joinByInviteCode(userId: String, inviteCode: String): Server? {
        val server = serverRepository.findByInviteCode(inviteCode) ?: return null
        val joined = serverRepository.joinServer(userId, server.id)
        return if (joined || server.ownerId == userId) server else server
    }
}
