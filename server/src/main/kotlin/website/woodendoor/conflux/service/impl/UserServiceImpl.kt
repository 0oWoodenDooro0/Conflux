package website.woodendoor.conflux.service.impl

import website.woodendoor.conflux.database.repositories.UserRepository
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
            discriminator = (1000..9999).random().toString()
        )
        return userRepository.createUser(newUser)
    }

    override suspend fun getUser(id: String): User? {
        return userRepository.getUser(id)
    }

    override suspend fun createUser(user: User): User? {
        return userRepository.createUser(user)
    }
}
