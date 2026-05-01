package website.woodendoor.conflux.database.repositories

import website.woodendoor.conflux.database.DatabaseFactory.dbQuery
import website.woodendoor.conflux.database.models.*
import website.woodendoor.conflux.models.Role
import website.woodendoor.conflux.models.Server
import website.woodendoor.conflux.models.User
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*

class ExposedServerRepository : ServerRepository {
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
        color = row[Roles.color]
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
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToServer)
    }

    override suspend fun getServer(id: String): Server? = dbQuery {
        Servers.selectAll().where { Servers.id eq id }
            .map(::resultRowToServer)
            .singleOrNull()
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

    override suspend fun createRole(serverId: String, role: Role): Role? = dbQuery {
        val insertStatement = Roles.insert {
            it[id] = role.id
            it[this.serverId] = serverId
            it[name] = role.name
            it[permissions] = role.permissions
            it[color] = role.color
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToRole)
    }

    override suspend fun updateRole(role: Role): Boolean = dbQuery {
        Roles.update({ Roles.id eq role.id }) {
            it[name] = role.name
            it[permissions] = role.permissions
            it[color] = role.color
        } > 0
    }

    override suspend fun deleteRole(roleId: String): Boolean = dbQuery {
        Roles.deleteWhere { Roles.id eq roleId } > 0
    }

    override suspend fun getRoles(serverId: String): List<Role> = dbQuery {
        Roles.selectAll().where { Roles.serverId eq serverId }
            .map(::resultRowToRole)
    }
}
