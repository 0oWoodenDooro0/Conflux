package website.woodendoor.conflux.service

import website.woodendoor.conflux.models.Role
import website.woodendoor.conflux.models.User

interface RoleService {
    suspend fun createRole(serverId: String, role: Role): Role?
    suspend fun getRoles(serverId: String): List<Role>
    suspend fun getRole(roleId: String): Role?
    suspend fun deleteRole(roleId: String): Boolean
    suspend fun updateRole(role: Role): Boolean
    suspend fun assignRoleToMember(serverId: String, userId: String, roleId: String): Boolean
    suspend fun removeRoleFromMember(serverId: String, userId: String, roleId: String): Boolean
    suspend fun getMembersWithRole(serverId: String, roleId: String): List<User>
    suspend fun hasPermission(serverId: String, userId: String, permission: Long): Boolean
}
