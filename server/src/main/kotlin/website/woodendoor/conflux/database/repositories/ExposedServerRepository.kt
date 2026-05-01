package website.woodendoor.conflux.database.repositories

import website.woodendoor.conflux.database.DatabaseFactory.dbQuery
import website.woodendoor.conflux.database.models.*
import website.woodendoor.conflux.models.Role
import website.woodendoor.conflux.models.Server
import website.woodendoor.conflux.models.User
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*

class ExposedServerRepository : ServerRepository {
    override suspend fun createServer(server: Server): Server? = TODO()
    override suspend fun getServer(id: String): Server? = TODO()
    override suspend fun updateServer(server: Server): Boolean = TODO()
    override suspend fun deleteServer(id: String): Boolean = TODO()
    override suspend fun addMember(serverId: String, userId: String): Boolean = TODO()
    override suspend fun removeMember(serverId: String, userId: String): Boolean = TODO()
    override suspend fun getMembers(serverId: String): List<User> = TODO()
    override suspend fun createRole(serverId: String, role: Role): Role? = TODO()
    override suspend fun updateRole(role: Role): Boolean = TODO()
    override suspend fun deleteRole(roleId: String): Boolean = TODO()
    override suspend fun getRoles(serverId: String): List<Role> = TODO()
}
