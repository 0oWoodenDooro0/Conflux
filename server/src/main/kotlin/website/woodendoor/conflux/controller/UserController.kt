package website.woodendoor.conflux.controller

import website.woodendoor.conflux.models.LoginRequest
import website.woodendoor.conflux.models.User
import website.woodendoor.conflux.service.UserService
import website.woodendoor.conflux.validation.UsernameValidator
import website.woodendoor.conflux.validation.ValidationResult

class UserController(
    private val userService: UserService
) {

    suspend fun login(request: LoginRequest): OperationResult<User> {
        when (val validationResult = UsernameValidator.validateUsername(request.username)) {
            is ValidationResult.Error -> {
                return OperationResult.Failure.BadRequest(validationResult.message)
            }
            ValidationResult.Success -> { /* OK */ }
        }

        val user = userService.getOrCreateUser(request.username)
        return if (user != null) {
            OperationResult.Success(user)
        } else {
            OperationResult.Failure.InternalError("Failed to create or retrieve user")
        }
    }
}
