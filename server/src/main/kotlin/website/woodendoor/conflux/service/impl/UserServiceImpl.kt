package website.woodendoor.conflux.service.impl

import website.woodendoor.conflux.database.repositories.UserRepository
import website.woodendoor.conflux.models.OnlineStatus
import website.woodendoor.conflux.models.User
import website.woodendoor.conflux.service.UserService

class UserServiceImpl(
    private val userRepository: UserRepository
) : UserService {

    override suspend fun getOrCreateUser(username: String): User? {
        val existingUser = userRepository.findByUsername(username)
        if (existingUser != null) {
            return existingUser
        }

        val newUser = User(
            id = java.util.UUID.randomUUID().toString(),
            username = username,
            discriminator = (1000..9999).random().toString(),
            onlineStatus = OnlineStatus.ONLINE,
            isOnline = true
        )
        return userRepository.createUser(newUser)
    }

    override suspend fun getUser(id: String): User? {
        return userRepository.getUser(id)
    }

    override suspend fun createUser(user: User): User? {
        return userRepository.createUser(user)
    }

    override suspend fun registerUser(username: String, passwordRaw: String): User? {
        val existingUser = userRepository.findByUsername(username)
        if (existingUser != null) {
            throw website.woodendoor.conflux.exceptions.ConflictException("Username is already taken")
        }
        val hashedPassword = website.woodendoor.conflux.util.PasswordHasher.hashPassword(passwordRaw)
        val newUser = User(
            id = java.util.UUID.randomUUID().toString(),
            username = username,
            discriminator = (1000..9999).random().toString(),
            onlineStatus = OnlineStatus.ONLINE,
            isOnline = true
        )
        return userRepository.createUser(newUser, hashedPassword)
    }

    override suspend fun authenticateUser(username: String, passwordRaw: String): User? {
        val existingUser = userRepository.findByUsername(username)
            ?: throw website.woodendoor.conflux.exceptions.UserNotFoundException("User not found")
        val storedHash = userRepository.getPasswordHash(username) ?: ""
        if (!website.woodendoor.conflux.util.PasswordHasher.verifyPassword(passwordRaw, storedHash)) {
            throw website.woodendoor.conflux.exceptions.UnauthorizedException("Invalid username or password")
        }
        val updatedUser = existingUser.copy(onlineStatus = OnlineStatus.ONLINE, isOnline = true)
        userRepository.updateUser(updatedUser)
        return updatedUser
    }

    override suspend fun updateUserStatus(userId: String, status: OnlineStatus, statusMessage: String?): User? {
        val user = userRepository.getUser(userId) ?: return null
        val updatedUser = user.copy(
            onlineStatus = status,
            isOnline = status != OnlineStatus.OFFLINE,
            statusMessage = statusMessage ?: user.statusMessage
        )
        userRepository.updateUser(updatedUser)
        return updatedUser
    }

    override suspend fun updateUserProfile(userId: String, avatar: String?, statusMessage: String?): User? {
        val user = userRepository.getUser(userId) ?: return null
        val updatedUser = user.copy(
            avatar = avatar ?: user.avatar,
            statusMessage = statusMessage ?: user.statusMessage
        )
        userRepository.updateUser(updatedUser)
        return updatedUser
    }
}
