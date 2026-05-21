package website.woodendoor.conflux.controller

import website.woodendoor.conflux.database.repositories.ChannelRepository
import website.woodendoor.conflux.database.repositories.ServerRepository
import website.woodendoor.conflux.database.repositories.UserRepository
import website.woodendoor.conflux.DEFAULT_ROLE_PRIORITY_EVERYONE
import website.woodendoor.conflux.models.Channel
import website.woodendoor.conflux.models.ChannelPermissionOverride
import website.woodendoor.conflux.models.ChannelType
import website.woodendoor.conflux.models.CreateServerRequest
import website.woodendoor.conflux.models.OverrideType
import website.woodendoor.conflux.models.Server
import website.woodendoor.conflux.models.User
import java.util.*

class ServerController(
    private val serverRepository: ServerRepository,
    private val userRepository: UserRepository,
    private val channelRepository: ChannelRepository
) {

    suspend fun createServer(request: CreateServerRequest): OperationResult<Server> {
        val owner = userRepository.getUser(request.ownerId) ?: userRepository.findByUsername(request.ownerId)
        val resolvedOwnerId = if (owner == null) {
            val newUserId = UUID.randomUUID().toString()
            userRepository.createUser(
                User(
                    id = newUserId,
                    username = request.ownerId,
                    discriminator = "0000"
                )
            )
            newUserId
        } else {
            owner.id
        }

        val finalRequest = request.copy(ownerId = resolvedOwnerId)
        val server = finalRequest.toDomain(id = UUID.randomUUID().toString())
        val created = serverRepository.createServer(server)
        
        if (created != null) {
            // Create default #general channel and set default override for @everyone
            try {
                val generalChannel = channelRepository.createChannel(
                    Channel(
                        id = UUID.randomUUID().toString(),
                        serverId = created.id,
                        name = "general",
                        type = ChannelType.TEXT,
                        topic = "Welcome to ${created.name}!"
                    )
                )
                if (generalChannel != null) {
                    val roles = serverRepository.getRoles(created.id)
                    val everyoneRole = roles.find { it.priorityLevel == DEFAULT_ROLE_PRIORITY_EVERYONE }
                    if (everyoneRole != null) {
                        val override = ChannelPermissionOverride(
                            id = UUID.randomUUID().toString(),
                            channelId = generalChannel.id,
                            targetId = everyoneRole.id,
                            targetType = OverrideType.ROLE,
                            allow = 0L,
                            deny = 0L
                        )
                        channelRepository.upsertOverride(generalChannel.id, override)
                    }
                }
            } catch (e: Exception) {
                // We don't fail the server creation if the channel fails, but we should log it
                println("Failed to create default #general channel for server ${created.id}: ${e.message}")
            }
            return OperationResult.Success(created)
        } else {
            return OperationResult.Failure.InternalError("Failed to create server")
        }
    }

    suspend fun getServer(id: String): OperationResult<Server> {
        val server = serverRepository.getServer(id)
        return if (server != null) {
            OperationResult.Success(server)
        } else {
            OperationResult.Failure.NotFound("Server not found")
        }
    }

    suspend fun getServersForUser(userId: String): OperationResult<List<Server>> {
        val servers = serverRepository.getServersForUser(userId)
        return OperationResult.Success(servers)
    }

    suspend fun getMembers(serverId: String): OperationResult<List<User>> {
        val members = serverRepository.getMembers(serverId)
        return OperationResult.Success(members)
    }

    suspend fun getPermissionsForMember(serverId: String, userId: String): OperationResult<Long> {
        val permissions = serverRepository.getPermissionsForMember(serverId, userId)
        return OperationResult.Success(permissions)
    }

    suspend fun joinServer(userId: String, serverId: String): OperationResult<Unit> {
        val server = serverRepository.getServer(serverId) ?: return OperationResult.Failure.NotFound("Server not found")
        
        val joined = serverRepository.joinServer(userId, serverId)
        return if (joined) {
            OperationResult.Success(Unit)
        } else {
            OperationResult.Failure.Conflict("Already a member or owner")
        }
    }
}
