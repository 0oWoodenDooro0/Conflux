package website.woodendoor.conflux.database.repositories

import website.woodendoor.conflux.models.User

interface UserRepository {
    suspend fun createUser(user: User): User?
    suspend fun getUser(id: String): User?
    suspend fun findByUsername(username: String): User?
    suspend fun updateUser(user: User): Boolean
    suspend fun deleteUser(id: String): Boolean
}
