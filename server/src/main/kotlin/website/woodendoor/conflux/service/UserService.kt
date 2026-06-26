package website.woodendoor.conflux.service

import website.woodendoor.conflux.models.User

interface UserService {
    suspend fun getOrCreateUser(username: String): User?
    suspend fun getUser(id: String): User?
    suspend fun createUser(user: User): User?
    suspend fun registerUser(username: String, passwordRaw: String): User?
    suspend fun authenticateUser(username: String, passwordRaw: String): User?
}
