package website.woodendoor.conflux.database.repositories

import website.woodendoor.conflux.database.DatabaseFactory.dbQuery
import website.woodendoor.conflux.database.models.Users
import website.woodendoor.conflux.models.User
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.core.insert
import org.jetbrains.exposed.v1.core.selectAll
import org.jetbrains.exposed.v1.core.update
import org.jetbrains.exposed.v1.core.deleteWhere
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq

class ExposedUserRepository : UserRepository {
    override suspend fun createUser(user: User): User? = TODO()
    override suspend fun getUser(id: String): User? = TODO()
    override suspend fun updateUser(user: User): Boolean = TODO()
    override suspend fun deleteUser(id: String): Boolean = TODO()
}
