package website.woodendoor.conflux.controller

import website.woodendoor.conflux.database.repositories.ServerRepository
import website.woodendoor.conflux.models.ConfluxPermission
import website.woodendoor.conflux.models.CreateRoleRequest
import website.woodendoor.conflux.models.Role
import java.util.*

class RoleController(private val serverRepository: ServerRepository) {

    suspend fun hasPermission(serverId: String, userId: String, permission: Long): Boolean {
        val userPermissions = serverRepository.getPermissionsForMember(serverId, userId)
        return ConfluxPermission.hasPermission(userPermissions, permission)
    }

    suspend fun createRole(serverId: String, request: CreateRoleRequest): OperationResult<Role> {
        val role = request.toDomain(id = UUID.randomUUID().toString())
        val created = serverRepository.createRole(serverId, role)
        
        return if (created != null) {
            OperationResult.Success(created)
        } else {
            OperationResult.Failure.InternalError("Failed to create role")
        }
    }

    suspend fun getRoles(serverId: String): OperationResult<List<Role>> {
        val roles = serverRepository.getRoles(serverId)
        return OperationResult.Success(roles)
    }

    suspend fun getRole(roleId: String): OperationResult<Role> {
        val role = serverRepository.getRole(roleId)
        return if (role != null) {
            OperationResult.Success(role)
        } else {
            OperationResult.Failure.NotFound("Role not found")
        }
    }

    suspend fun updateRole(role: Role): OperationResult<Role> {
        val success = serverRepository.updateRole(role)
        return if (success) {
            OperationResult.Success(role)
        } else {
            OperationResult.Failure.InternalError("Failed to update role")
        }
    }

    suspend fun assignRoleToMember(serverId: String, userId: String, roleId: String): OperationResult<Unit> {
        val success = serverRepository.assignRoleToMember(serverId, userId, roleId)
        return if (success) {
            OperationResult.Success(Unit)
        } else {
            OperationResult.Failure.InternalError("Failed to assign role")
        }
    }

    suspend fun removeRoleFromMember(serverId: String, userId: String, roleId: String): OperationResult<Unit> {
        val success = serverRepository.removeRoleFromMember(serverId, userId, roleId)
        return if (success) {
            OperationResult.Success(Unit)
        } else {
            OperationResult.Failure.InternalError("Failed to remove role")
        }
    }

    suspend fun getMembersWithRole(serverId: String, roleId: String): OperationResult<List<website.woodendoor.conflux.models.User>> {
        val members = serverRepository.getMembersWithRole(serverId, roleId)
        return OperationResult.Success(members)
    }
}
