package website.woodendoor.conflux.database.repositories

import website.woodendoor.conflux.database.DatabaseFactory.dbQuery
import website.woodendoor.conflux.database.models.Users
import website.woodendoor.conflux.models.User
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class ExposedUserRepository : UserRepository {
    private fun resultRowToUser(row: ResultRow) = User(
        id = row[Users.id],
        username = row[Users.username],
        discriminator = row[Users.discriminator],
        avatar = row[Users.avatar]
    )

    override suspend fun createUser(user: User, passwordHash: String): User? = dbQuery {
        val insertStatement = Users.insert {
            it[id] = user.id
            it[username] = user.username
            it[discriminator] = user.discriminator
            it[avatar] = user.avatar
            it[Users.passwordHash] = passwordHash
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
        } > 0
    }

    override suspend fun deleteUser(id: String): Boolean = dbQuery {
        Users.deleteWhere { Users.id eq id } > 0
    }
}
