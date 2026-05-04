package website.woodendoor.conflux.controller

import website.woodendoor.conflux.database.repositories.UserRepository
import website.woodendoor.conflux.models.LoginRequest
import website.woodendoor.conflux.models.User
import website.woodendoor.conflux.validation.UsernameValidator
import website.woodendoor.conflux.validation.ValidationResult
import java.util.UUID

class UserController(private val userRepository: UserRepository) {

    suspend fun login(request: LoginRequest): OperationResult<User> {
        when (val validationResult = UsernameValidator.validateUsername(request.username)) {
            is ValidationResult.Error -> {
                return OperationResult.Failure.BadRequest(validationResult.message)
            }
            ValidationResult.Success -> { /* OK */ }
        }

        val existingUser = userRepository.findByUsername(request.username)
        if (existingUser != null) {
            return OperationResult.Success(existingUser)
        }

        val newUser = User(
            id = UUID.randomUUID().toString(),
            username = request.username,
            discriminator = (1000..9999).random().toString()
        )
        val created = userRepository.createUser(newUser)
        
        return if (created != null) {
            OperationResult.Success(created)
        } else {
            OperationResult.Failure.InternalError("Failed to create user")
        }
    }
}
