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

import website.woodendoor.conflux.DEFAULT_ROLE_NAME_OWNER
import website.woodendoor.conflux.DEFAULT_ROLE_NAME_MEMBER

class ExposedServerRepository(private val userRepository: UserRepository) : ServerRepository {
    private fun resultRowToServer(row: ResultRow) = Server(
        id = row[Servers.id],
        name = row[Servers.name],
        ownerId = row[Servers.ownerId],
        icon = row[Servers.icon]
    )

    private fun resultRowToRole(row: ResultRow) = Role(
        id = row[Roles.id],
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
            it[icon] = server.icon
        }
        val createdServer = insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToServer)
        
        if (createdServer != null) {
            // Add creator to ServerMembers
            ServerMembers.insert {
                it[this.serverId] = createdServer.id
                it[this.userId] = createdServer.ownerId
            }

            // Create Owner role
            val ownerRoleId = UUID.randomUUID().toString()
            Roles.insert {
                it[id] = ownerRoleId
                it[this.serverId] = createdServer.id
                it[name] = DEFAULT_ROLE_NAME_OWNER
                it[permissions] = ConfluxPermission.ALL
                it[color] = null
                it[priorityLevel] = 100
            }
            
            // Create Member role
            Roles.insert {
                it[id] = UUID.randomUUID().toString()
                it[this.serverId] = createdServer.id
                it[name] = DEFAULT_ROLE_NAME_MEMBER
                it[permissions] = ConfluxPermission.MESSAGING
                it[color] = null
                it[priorityLevel] = 0
            }
            
            // Assign Owner role to creator
            MemberRoles.insert {
                it[this.serverId] = createdServer.id
                it[this.userId] = createdServer.ownerId
                it[this.roleId] = ownerRoleId
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
            it[icon] = server.icon
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

        if (inserted) {
            val memberRole = Roles.selectAll()
                .where { (Roles.serverId eq serverId) and (Roles.name eq DEFAULT_ROLE_NAME_MEMBER) }
                .singleOrNull()
            
            if (memberRole != null) {
                MemberRoles.insert {
                    it[this.serverId] = serverId
                    it[this.userId] = userId
                    it[this.roleId] = memberRole[Roles.id]
                }
            }
        }

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
        Roles.deleteWhere { Roles.id eq roleId } > 0
    }

    override suspend fun getRoles(serverId: String): List<Role> = dbQuery {
        Roles.selectAll().where { Roles.serverId eq serverId }
            .map(::resultRowToRole)
    }

    override suspend fun assignRoleToMember(serverId: String, userId: String, roleId: String): Boolean = dbQuery {
        MemberRoles.insert {
            it[this.serverId] = serverId
            it[this.userId] = userId
            it[this.roleId] = roleId
        }.insertedCount > 0
    }

    override suspend fun getRolesForMember(serverId: String, userId: String): List<Role> = dbQuery {
        (Roles innerJoin MemberRoles)
            .selectAll().where { 
                (MemberRoles.serverId eq serverId) and 
                (MemberRoles.userId eq userId) and 
                (Roles.id eq MemberRoles.roleId) 
            }
            .map(::resultRowToRole)
    }

    override suspend fun getPermissionsForMember(serverId: String, userId: String): Long = dbQuery {
        (Roles innerJoin MemberRoles)
            .select(Roles.permissions)
            .where { 
                (MemberRoles.serverId eq serverId) and 
                (MemberRoles.userId eq userId) and 
                (Roles.id eq MemberRoles.roleId) 
            }
            .map { it[Roles.permissions] }
            .fold(0L) { acc, p -> acc or p }
    }
}
