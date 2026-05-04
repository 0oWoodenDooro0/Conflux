package website.woodendoor.conflux.controller

import website.woodendoor.conflux.database.repositories.ServerRepository
import website.woodendoor.conflux.models.CreateRoleRequest
import website.woodendoor.conflux.models.Role
import java.util.*

class RoleController(private val serverRepository: ServerRepository) {

    suspend fun createRole(serverId: String, request: CreateRoleRequest): OperationResult<Role> {
        val role = request.toDomain(id = UUID.randomUUID().toString())
        val created = serverRepository.createRole(serverId, role)
        
        return if (created != null) {
            OperationResult.Success(created)
        } else {
            OperationResult.Failure.InternalError("Failed to create role")
        }
    }
}
