package website.woodendoor.conflux.service

import website.woodendoor.conflux.models.Server
import website.woodendoor.conflux.models.User

interface ServerService {
    suspend fun createServer(name: String, ownerId: String): Server?
    suspend fun getServer(id: String): Server?
    suspend fun getServersForUser(userId: String): List<Server>
    suspend fun getMembers(serverId: String): List<User>
    suspend fun getPermissionsForMember(serverId: String, userId: String): Long
    suspend fun joinServer(userId: String, serverId: String): Boolean
}
