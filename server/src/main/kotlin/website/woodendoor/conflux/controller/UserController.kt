package website.woodendoor.conflux.controller

import website.woodendoor.conflux.auth.WebSocketAuthTokenManager
import website.woodendoor.conflux.exceptions.*
import website.woodendoor.conflux.models.*
import website.woodendoor.conflux.service.UserService
import website.woodendoor.conflux.validation.PasswordValidator
import website.woodendoor.conflux.validation.UsernameValidator
import website.woodendoor.conflux.validation.ValidationResult

class UserController(
    private val userService: UserService,
    private val tokenManager: WebSocketAuthTokenManager
) {

    suspend fun login(request: LoginRequest): AuthResponse {
        when (val validationResult = UsernameValidator.validateUsername(request.username)) {
            is ValidationResult.Error -> {
                throw BadRequestException(validationResult.message)
            }
            ValidationResult.Success -> { /* OK */ }
        }

        val user = userService.authenticateUser(request.username, request.password)
            ?: throw InternalServerErrorException("Failed to authenticate user")

        val token = tokenManager.generateToken(user.id)
        return AuthResponse(token = token, user = user)
    }

    suspend fun register(request: RegisterRequest): AuthResponse {
        when (val validationResult = UsernameValidator.validateUsername(request.username)) {
            is ValidationResult.Error -> {
                throw BadRequestException(validationResult.message)
            }
            ValidationResult.Success -> { /* OK */ }
        }

        when (val validationResult = PasswordValidator.validatePassword(request.password)) {
            is ValidationResult.Error -> {
                throw BadRequestException(validationResult.message)
            }
            ValidationResult.Success -> { /* OK */ }
        }

        val user = userService.registerUser(request.username, request.password)
            ?: throw InternalServerErrorException("Failed to register user")

        val token = tokenManager.generateToken(user.id)
        return AuthResponse(token = token, user = user)
    }

    suspend fun getUser(id: String): User {
        return userService.getUser(id)
            ?: throw UserNotFoundException("User with ID $id not found")
    }

    suspend fun updateStatus(userId: String, request: UpdateStatusRequest): User {
        return userService.updateUserStatus(userId, request.onlineStatus, request.statusMessage)
            ?: throw UserNotFoundException("User with ID $userId not found")
    }

    suspend fun updateProfile(userId: String, request: UpdateProfileRequest): User {
        return userService.updateUserProfile(userId, request.avatar, request.statusMessage)
            ?: throw UserNotFoundException("User with ID $userId not found")
    }
}
