package website.woodendoor.conflux.controller

import website.woodendoor.conflux.WebSocketConnectionManager
import website.woodendoor.conflux.models.ConfluxEvent
import website.woodendoor.conflux.models.ConfluxPermission
import website.woodendoor.conflux.models.CreateRoleRequest
import website.woodendoor.conflux.models.Role
import website.woodendoor.conflux.service.RoleService
import java.util.*

class RoleController(
    private val roleService: RoleService,
    private val connectionManager: WebSocketConnectionManager
) {

    suspend fun hasPermission(serverId: String, userId: String, permission: Long): Boolean {
        return roleService.hasPermission(serverId, userId, permission)
    }

    suspend fun createRole(serverId: String, operatorId: String, request: CreateRoleRequest): OperationResult<Role> {
        if (!hasPermission(serverId, operatorId, ConfluxPermission.ROLE_MANAGEMENT)) {
            return OperationResult.Failure.Forbidden("Insufficient permissions")
        }
        val role = request.toDomain(id = UUID.randomUUID().toString(), serverId = serverId)
        val created = roleService.createRole(serverId, role)
        
        return if (created != null) {
            OperationResult.Success(created)
        } else {
            OperationResult.Failure.InternalError("Failed to create role")
        }
    }

    suspend fun getRoles(serverId: String): OperationResult<List<Role>> {
        val roles = roleService.getRoles(serverId)
        return OperationResult.Success(roles)
    }

    suspend fun getRole(roleId: String): OperationResult<Role> {
        val role = roleService.getRole(roleId)
        return if (role != null) {
            OperationResult.Success(role)
        } else {
            OperationResult.Failure.NotFound("Role not found")
        }
    }

    suspend fun deleteRole(serverId: String, operatorId: String, roleId: String): OperationResult<Unit> {
        if (!hasPermission(serverId, operatorId, ConfluxPermission.ROLE_MANAGEMENT)) {
            return OperationResult.Failure.Forbidden("Insufficient permissions")
        }
        val success = roleService.deleteRole(roleId)
        return if (success) {
            OperationResult.Success(Unit)
        } else {
            OperationResult.Failure.InternalError("Failed to delete role")
        }
    }

    suspend fun updateRole(serverId: String, operatorId: String, role: Role): OperationResult<Role> {
        if (!hasPermission(serverId, operatorId, ConfluxPermission.ROLE_MANAGEMENT)) {
            return OperationResult.Failure.Forbidden("Insufficient permissions")
        }
        val success = roleService.updateRole(role)
        return if (success) {
            connectionManager.broadcastToServer(role.serverId, ConfluxEvent.PermissionUpdate(role.serverId, roleId = role.id))
            OperationResult.Success(role)
        } else {
            OperationResult.Failure.InternalError("Failed to update role")
        }
    }

    suspend fun assignRoleToMember(serverId: String, operatorId: String, targetUserId: String, roleId: String): OperationResult<Unit> {
        if (!hasPermission(serverId, operatorId, ConfluxPermission.ROLE_MANAGEMENT)) {
            return OperationResult.Failure.Forbidden("Insufficient permissions")
        }
        val success = roleService.assignRoleToMember(serverId, targetUserId, roleId)
        return if (success) {
            connectionManager.broadcastToServer(serverId, ConfluxEvent.PermissionUpdate(serverId, userId = targetUserId))
            OperationResult.Success(Unit)
        } else {
            OperationResult.Failure.InternalError("Failed to assign role")
        }
    }

    suspend fun removeRoleFromMember(serverId: String, operatorId: String, targetUserId: String, roleId: String): OperationResult<Unit> {
        if (!hasPermission(serverId, operatorId, ConfluxPermission.ROLE_MANAGEMENT)) {
            return OperationResult.Failure.Forbidden("Insufficient permissions")
        }
        val success = roleService.removeRoleFromMember(serverId, targetUserId, roleId)
        return if (success) {
            connectionManager.broadcastToServer(serverId, ConfluxEvent.PermissionUpdate(serverId, userId = targetUserId))
            OperationResult.Success(Unit)
        } else {
            OperationResult.Failure.InternalError("Failed to remove role")
        }
    }

    suspend fun getMembersWithRole(serverId: String, roleId: String): OperationResult<List<website.woodendoor.conflux.models.User>> {
        val members = roleService.getMembersWithRole(serverId, roleId)
        return OperationResult.Success(members)
    }
}
