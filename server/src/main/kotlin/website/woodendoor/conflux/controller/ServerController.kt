package website.woodendoor.conflux.controller

import website.woodendoor.conflux.database.repositories.ServerRepository
import website.woodendoor.conflux.models.CreateServerRequest
import website.woodendoor.conflux.models.Server
import java.util.*

class ServerController(private val serverRepository: ServerRepository) {

    suspend fun createServer(request: CreateServerRequest): OperationResult<Server> {
        val server = request.toDomain(id = UUID.randomUUID().toString())
        val created = serverRepository.createServer(server)
        
        return if (created != null) {
            OperationResult.Success(created)
        } else {
            OperationResult.Failure.InternalError("Failed to create server")
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

    suspend fun joinServer(userId: String, serverId: String): OperationResult<Unit> {
        val joined = serverRepository.joinServer(userId, serverId)
        return if (joined) {
            OperationResult.Success(Unit)
        } else {
            OperationResult.Failure.BadRequest("Failed to join server")
        }
    }
}
