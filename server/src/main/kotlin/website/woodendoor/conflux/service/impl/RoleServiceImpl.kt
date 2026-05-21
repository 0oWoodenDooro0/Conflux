package website.woodendoor.conflux.service.impl

import website.woodendoor.conflux.database.repositories.ServerRepository
import website.woodendoor.conflux.models.ConfluxPermission
import website.woodendoor.conflux.models.Role
import website.woodendoor.conflux.models.User
import website.woodendoor.conflux.service.RoleService

class RoleServiceImpl(
    private val serverRepository: ServerRepository
) : RoleService {

    override suspend fun createRole(serverId: String, role: Role): Role? {
        return serverRepository.createRole(serverId, role)
    }

    override suspend fun getRoles(serverId: String): List<Role> {
        return serverRepository.getRoles(serverId)
    }

    override suspend fun getRole(roleId: String): Role? {
        return serverRepository.getRole(roleId)
    }

    override suspend fun deleteRole(roleId: String): Boolean {
        return serverRepository.deleteRole(roleId)
    }

    override suspend fun updateRole(role: Role): Boolean {
        return serverRepository.updateRole(role)
    }

    override suspend fun assignRoleToMember(serverId: String, userId: String, roleId: String): Boolean {
        return serverRepository.assignRoleToMember(serverId, userId, roleId)
    }

    override suspend fun removeRoleFromMember(serverId: String, userId: String, roleId: String): Boolean {
        return serverRepository.removeRoleFromMember(serverId, userId, roleId)
    }

    override suspend fun getMembersWithRole(serverId: String, roleId: String): List<User> {
        return serverRepository.getMembersWithRole(serverId, roleId)
    }

    override suspend fun hasPermission(serverId: String, userId: String, permission: Long): Boolean {
        val userPermissions = serverRepository.getPermissionsForMember(serverId, userId)
        return ConfluxPermission.hasPermission(userPermissions, permission)
    }
}
