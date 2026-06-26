package website.woodendoor.conflux.controller

import website.woodendoor.conflux.models.LoginRequest
import website.woodendoor.conflux.models.User
import website.woodendoor.conflux.service.UserService
import website.woodendoor.conflux.validation.UsernameValidator
import website.woodendoor.conflux.validation.ValidationResult

import website.woodendoor.conflux.exceptions.*

class UserController(
    private val userService: UserService
) {

    suspend fun login(request: LoginRequest): User {
        when (val validationResult = UsernameValidator.validateUsername(request.username)) {
            is ValidationResult.Error -> {
                throw BadRequestException(validationResult.message)
            }
            ValidationResult.Success -> { /* OK */ }
        }

        return userService.authenticateUser(request.username, request.password)
            ?: throw InternalServerErrorException("Failed to authenticate user")
    }

    suspend fun register(request: website.woodendoor.conflux.models.RegisterRequest): User {
        when (val validationResult = UsernameValidator.validateUsername(request.username)) {
            is ValidationResult.Error -> {
                throw BadRequestException(validationResult.message)
            }
            ValidationResult.Success -> { /* OK */ }
        }

        when (val validationResult = website.woodendoor.conflux.validation.PasswordValidator.validatePassword(request.password)) {
            is ValidationResult.Error -> {
                throw BadRequestException(validationResult.message)
            }
            ValidationResult.Success -> { /* OK */ }
        }

        return userService.registerUser(request.username, request.password)
            ?: throw InternalServerErrorException("Failed to register user")
    }

    suspend fun getUser(id: String): User {
        return userService.getUser(id)
            ?: throw UserNotFoundException("User with ID $id not found")
    }
}

