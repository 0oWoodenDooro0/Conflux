package website.woodendoor.conflux.controller

import website.woodendoor.conflux.models.CreateServerRequest
import website.woodendoor.conflux.models.Server
import website.woodendoor.conflux.models.User
import website.woodendoor.conflux.models.ConfluxPermission
import website.woodendoor.conflux.service.ServerService
import website.woodendoor.conflux.service.ChannelPermissionService
import website.woodendoor.conflux.WebSocketConnectionManager

class ServerController(
    private val serverService: ServerService,
    private val connectionManager: WebSocketConnectionManager,
    private val channelPermissionService: ChannelPermissionService
) {

    suspend fun createServer(request: CreateServerRequest): OperationResult<Server> {
        val created = serverService.createServer(request.name, request.ownerId, request.icon, request.description)
        return if (created != null) {
            OperationResult.Success(created)
        } else {
            OperationResult.Failure.InternalError("Failed to create server")
        }
    }

    suspend fun getServer(id: String): OperationResult<Server> {
        val server = serverService.getServer(id)
        return if (server != null) {
            OperationResult.Success(server)
        } else {
            OperationResult.Failure.NotFound("Server not found")
        }
    }

    suspend fun getServersForUser(userId: String): OperationResult<List<Server>> {
        val servers = serverService.getServersForUser(userId)
        return OperationResult.Success(servers)
    }

    suspend fun getMembers(serverId: String, channelId: String? = null): OperationResult<List<User>> {
        val members = serverService.getMembers(serverId)
        val filteredMembers = if (channelId != null) {
            members.filter { member ->
                channelPermissionService.hasPermission(serverId, channelId, member.id, ConfluxPermission.VIEW_CHANNEL)
            }
        } else {
            members
        }
        val membersWithPresence = filteredMembers.map { member ->
            val isOnline = connectionManager.getUserSessions(member.id).isNotEmpty()
            member.copy(isOnline = isOnline)
        }
        return OperationResult.Success(membersWithPresence)
    }

    suspend fun getPermissionsForMember(serverId: String, userId: String): OperationResult<Long> {
        val permissions = serverService.getPermissionsForMember(serverId, userId)
        return OperationResult.Success(permissions)
    }

    suspend fun joinServer(userId: String, serverId: String): OperationResult<Unit> {
        val server = serverService.getServer(serverId) ?: return OperationResult.Failure.NotFound("Server not found")
        
        val joined = serverService.joinServer(userId, serverId)
        return if (joined) {
            connectionManager.broadcastToServer(serverId, website.woodendoor.conflux.models.ConfluxEvent.PermissionUpdate(serverId, userId = userId))
            OperationResult.Success(Unit)
        } else {
            OperationResult.Failure.Conflict("Already a member or owner")
        }
    }

    suspend fun joinByInviteCode(userId: String, inviteCode: String): OperationResult<Server> {
        val server = serverService.joinByInviteCode(userId, inviteCode)
            ?: return OperationResult.Failure.NotFound("Invalid invite code")
        connectionManager.broadcastToServer(server.id, website.woodendoor.conflux.models.ConfluxEvent.PermissionUpdate(server.id, userId = userId))
        return OperationResult.Success(server)
    }
}
