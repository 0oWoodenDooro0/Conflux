package website.woodendoor.conflux.controller

import website.woodendoor.conflux.models.CreateServerRequest
import website.woodendoor.conflux.models.Server
import website.woodendoor.conflux.models.User
import website.woodendoor.conflux.service.ServerService

class ServerController(
    private val serverService: ServerService
) {

    suspend fun createServer(request: CreateServerRequest): OperationResult<Server> {
        val created = serverService.createServer(request.name, request.ownerId)
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

    suspend fun getMembers(serverId: String): OperationResult<List<User>> {
        val members = serverService.getMembers(serverId)
        return OperationResult.Success(members)
    }

    suspend fun getPermissionsForMember(serverId: String, userId: String): OperationResult<Long> {
        val permissions = serverService.getPermissionsForMember(serverId, userId)
        return OperationResult.Success(permissions)
    }

    suspend fun joinServer(userId: String, serverId: String): OperationResult<Unit> {
        val server = serverService.getServer(serverId) ?: return OperationResult.Failure.NotFound("Server not found")
        
        val joined = serverService.joinServer(userId, serverId)
        return if (joined) {
            OperationResult.Success(Unit)
        } else {
            OperationResult.Failure.Conflict("Already a member or owner")
        }
    }
}
