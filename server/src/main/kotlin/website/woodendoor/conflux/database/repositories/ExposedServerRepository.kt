package website.woodendoor.conflux.database.repositories

import website.woodendoor.conflux.database.DatabaseFactory.dbQuery
import website.woodendoor.conflux.database.models.*
import website.woodendoor.conflux.models.Role
import website.woodendoor.conflux.models.Server
import website.woodendoor.conflux.models.User
import website.woodendoor.conflux.models.ConfluxPermission
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import java.util.UUID

import website.woodendoor.conflux.DEFAULT_ROLE_NAME_EVERYONE
import website.woodendoor.conflux.DEFAULT_ROLE_PRIORITY_EVERYONE

class ExposedServerRepository(private val userRepository: ExposedUserRepository) : ServerRepository {
    private fun resultRowToServer(row: ResultRow) = Server(
        id = row[Servers.id],
        name = row[Servers.name],
        ownerId = row[Servers.ownerId]
    )

    private fun resultRowToRole(row: ResultRow) = Role(
        id = row[Roles.id],
        serverId = row[Roles.serverId],
        name = row[Roles.name],
        permissions = row[Roles.permissions],
        color = row[Roles.color],
        priorityLevel = row[Roles.priorityLevel]
    )

    private fun resultRowToUser(row: ResultRow) = User(
        id = row[Users.id],
        username = row[Users.username],
        discriminator = row[Users.discriminator],
        avatar = row[Users.avatar]
    )

    override suspend fun createServer(server: Server): Server? = dbQuery {
        val insertStatement = Servers.insert {
            it[id] = server.id
            it[name] = server.name
            it[ownerId] = server.ownerId
        }
        val createdServer = insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToServer)
        
        if (createdServer != null) {
            // Add creator to ServerMembers
            ServerMembers.insert {
                it[this.serverId] = createdServer.id
                it[this.userId] = createdServer.ownerId
            }

            // Create @everyone role
            Roles.insert {
                it[id] = UUID.randomUUID().toString()
                it[this.serverId] = createdServer.id
                it[name] = DEFAULT_ROLE_NAME_EVERYONE
                it[permissions] = ConfluxPermission.MESSAGING
                it[color] = null
                it[priorityLevel] = DEFAULT_ROLE_PRIORITY_EVERYONE
            }
        }
        
        createdServer
    }

    override suspend fun getServer(id: String): Server? = dbQuery {
        Servers.selectAll().where { Servers.id eq id }
            .map(::resultRowToServer)
            .singleOrNull()
    }

    override suspend fun getServersForUser(userId: String): List<Server> = dbQuery {
        val resolvedUserId = if (userRepository.getUser(userId) == null) {
            userRepository.findByUsername(userId)?.id ?: userId
        } else {
            userId
        }

        val ownedServers = Servers.selectAll().where { Servers.ownerId eq resolvedUserId }
            .map(::resultRowToServer)
        
        val memberServers = (Servers innerJoin ServerMembers)
            .selectAll().where { ServerMembers.userId eq resolvedUserId }
            .map(::resultRowToServer)
        
        (ownedServers + memberServers).distinctBy { it.id }
    }

    override suspend fun updateServer(server: Server): Boolean = dbQuery {
        Servers.update({ Servers.id eq server.id }) {
            it[name] = server.name
            it[ownerId] = server.ownerId
        } > 0
    }

    override suspend fun deleteServer(id: String): Boolean = dbQuery {
        Servers.deleteWhere { Servers.id eq id } > 0
    }

    override suspend fun addMember(serverId: String, userId: String): Boolean = dbQuery {
        ServerMembers.insert {
            it[this.serverId] = serverId
            it[this.userId] = userId
        }.insertedCount > 0
    }

    override suspend fun removeMember(serverId: String, userId: String): Boolean = dbQuery {
        ServerMembers.deleteWhere { 
            (ServerMembers.serverId eq serverId) and (ServerMembers.userId eq userId) 
        } > 0
    }

    override suspend fun getMembers(serverId: String): List<User> = dbQuery {
        (Users innerJoin ServerMembers)
            .selectAll().where { ServerMembers.serverId eq serverId }
            .map(::resultRowToUser)
    }

    override suspend fun joinServer(userId: String, serverId: String): Boolean = dbQuery {
        val server = Servers.selectAll().where { Servers.id eq serverId }.singleOrNull()
        if (server == null) return@dbQuery false

        if (server[Servers.ownerId] == userId) return@dbQuery false

        val isAlreadyMember = ServerMembers.selectAll()
            .where { (ServerMembers.serverId eq serverId) and (ServerMembers.userId eq userId) }
            .any()
        
        if (isAlreadyMember) return@dbQuery false

        val inserted = ServerMembers.insert {
            it[this.serverId] = serverId
            it[this.userId] = userId
        }.insertedCount > 0

        inserted
    }

    override suspend fun createRole(serverId: String, role: Role): Role? = dbQuery {
        val insertStatement = Roles.insert {
            it[id] = role.id
            it[this.serverId] = serverId
            it[name] = role.name
            it[permissions] = role.permissions
            it[color] = role.color
            it[priorityLevel] = role.priorityLevel
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToRole)
    }

    override suspend fun getRole(roleId: String): Role? = dbQuery {
        Roles.selectAll().where { Roles.id eq roleId }
            .map(::resultRowToRole)
            .singleOrNull()
    }

    override suspend fun updateRole(role: Role): Boolean = dbQuery {
        Roles.update({ Roles.id eq role.id }) {
            it[name] = role.name
            it[permissions] = role.permissions
            it[color] = role.color
            it[priorityLevel] = role.priorityLevel
        } > 0
    }

    override suspend fun deleteRole(roleId: String): Boolean = dbQuery {
        val role = Roles.selectAll().where { Roles.id eq roleId }.singleOrNull()
        if (role != null && role[Roles.priorityLevel] == DEFAULT_ROLE_PRIORITY_EVERYONE) {
            return@dbQuery false
        }
        Roles.deleteWhere { Roles.id eq roleId } > 0
    }

    override suspend fun getRoles(serverId: String): List<Role> = dbQuery {
        Roles.selectAll().where { Roles.serverId eq serverId }
            .orderBy(Roles.priorityLevel, SortOrder.DESC)
            .map(::resultRowToRole)
    }

    override suspend fun assignRoleToMember(serverId: String, userId: String, roleId: String): Boolean = dbQuery {
        MemberRoles.insert {
            it[this.serverId] = serverId
            it[this.userId] = userId
            it[this.roleId] = roleId
        }.insertedCount > 0
    }

    override suspend fun removeRoleFromMember(serverId: String, userId: String, roleId: String): Boolean = dbQuery {
        MemberRoles.deleteWhere { 
            (MemberRoles.serverId eq serverId) and (MemberRoles.userId eq userId) and (MemberRoles.roleId eq roleId)
        } > 0
    }

    override suspend fun getRolesForMember(serverId: String, userId: String): List<Role> = dbQuery {
        val everyoneRole = Roles.selectAll()
            .where { (Roles.serverId eq serverId) and (Roles.priorityLevel eq DEFAULT_ROLE_PRIORITY_EVERYONE) }
            .map(::resultRowToRole)
            .singleOrNull()

        val assignedRoles = (Roles innerJoin MemberRoles)
            .selectAll().where { 
                (MemberRoles.serverId eq serverId) and 
                (MemberRoles.userId eq userId) and 
                (Roles.id eq MemberRoles.roleId) 
            }
            .orderBy(Roles.priorityLevel, SortOrder.DESC)
            .map(::resultRowToRole)

        if (everyoneRole != null) {
            (assignedRoles + everyoneRole).distinctBy { it.id }.sortedByDescending { it.priorityLevel }
        } else {
            assignedRoles
        }
    }

    override suspend fun getPermissionsForMember(serverId: String, userId: String): Long = dbQuery {
        val resolvedUserId = if (userRepository.getUser(userId) == null) {
            userRepository.findByUsername(userId)?.id ?: userId
        } else {
            userId
        }

        val server = Servers.selectAll().where { Servers.id eq serverId }.singleOrNull()
        if (server != null && server[Servers.ownerId] == resolvedUserId) {
            return@dbQuery ConfluxPermission.ALL
        }

        val everyonePermissions = Roles.selectAll()
            .where { (Roles.serverId eq serverId) and (Roles.priorityLevel eq DEFAULT_ROLE_PRIORITY_EVERYONE) }
            .map { it[Roles.permissions] }
            .singleOrNull() ?: 0L

        val rolePermissions = (Roles innerJoin MemberRoles)
            .select(Roles.permissions)
            .where { 
                (MemberRoles.serverId eq serverId) and 
                (MemberRoles.userId eq resolvedUserId) and 
                (Roles.id eq MemberRoles.roleId) 
            }
            .map { it[Roles.permissions] }
            .fold(0L) { acc, p -> acc or p }

        rolePermissions or everyonePermissions
    }

    override suspend fun getMembersWithRole(serverId: String, roleId: String): List<User> = dbQuery {
        (Users innerJoin MemberRoles)
            .selectAll().where { 
                (MemberRoles.serverId eq serverId) and 
                (MemberRoles.roleId eq roleId) and
                (Users.id eq MemberRoles.userId)
            }
            .map(::resultRowToUser)
    }
}
