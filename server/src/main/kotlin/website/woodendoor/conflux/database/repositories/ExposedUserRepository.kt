package website.woodendoor.conflux.database.repositories

import website.woodendoor.conflux.database.DatabaseFactory.dbQuery
import website.woodendoor.conflux.database.models.Users
import website.woodendoor.conflux.models.OnlineStatus
import website.woodendoor.conflux.models.User
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*

class ExposedUserRepository : UserRepository {
    private fun resultRowToUser(row: ResultRow) = User(
        id = row[Users.id],
        username = row[Users.username],
        discriminator = row[Users.discriminator],
        avatar = row[Users.avatar],
        statusMessage = row[Users.statusMessage],
        onlineStatus = try {
            OnlineStatus.valueOf(row[Users.onlineStatus])
        } catch (e: Exception) {
            OnlineStatus.OFFLINE
        },
        isOnline = row[Users.onlineStatus] != "OFFLINE"
    )

    override suspend fun createUser(user: User, passwordHash: String): User? = dbQuery {
        val insertStatement = Users.insert {
            it[id] = user.id
            it[username] = user.username
            it[discriminator] = user.discriminator
            it[avatar] = user.avatar
            it[Users.passwordHash] = passwordHash
            it[statusMessage] = user.statusMessage
            it[onlineStatus] = user.onlineStatus.name
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToUser)
    }

    override suspend fun getUser(id: String): User? = dbQuery {
        Users.selectAll().where { Users.id eq id }
            .map(::resultRowToUser)
            .singleOrNull()
    }

    override suspend fun findByUsername(username: String): User? = dbQuery {
        Users.selectAll().where { Users.username eq username }
            .map(::resultRowToUser)
            .singleOrNull()
    }

    override suspend fun getPasswordHash(username: String): String? = dbQuery {
        Users.selectAll().where { Users.username eq username }
            .map { it[Users.passwordHash] }
            .singleOrNull()
    }

    override suspend fun updateUser(user: User): Boolean = dbQuery {
        Users.update({ Users.id eq user.id }) {
            it[username] = user.username
            it[discriminator] = user.discriminator
            it[avatar] = user.avatar
            it[statusMessage] = user.statusMessage
            it[onlineStatus] = user.onlineStatus.name
        } > 0
    }

    override suspend fun deleteUser(id: String): Boolean = dbQuery {
        Users.deleteWhere { Users.id eq id } > 0
    }
}
