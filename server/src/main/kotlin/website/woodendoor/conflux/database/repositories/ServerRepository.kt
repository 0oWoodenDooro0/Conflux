package website.woodendoor.conflux.database.repositories

import website.woodendoor.conflux.models.Role
import website.woodendoor.conflux.models.Server
import website.woodendoor.conflux.models.User

interface ServerRepository {
    suspend fun createServer(server: Server): Server?
    suspend fun getServer(id: String): Server?
    suspend fun getServersForUser(userId: String): List<Server>
    suspend fun updateServer(server: Server): Boolean
    suspend fun deleteServer(id: String): Boolean
    
    suspend fun addMember(serverId: String, userId: String): Boolean
    suspend fun removeMember(serverId: String, userId: String): Boolean
    suspend fun getMembers(serverId: String): List<User>
    
    suspend fun createRole(serverId: String, role: Role): Role?
    suspend fun updateRole(role: Role): Boolean
    suspend fun deleteRole(roleId: String): Boolean
    suspend fun getRoles(serverId: String): List<Role>
}
