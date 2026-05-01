package website.woodendoor.conflux.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import website.woodendoor.conflux.database.repositories.UserRepository
import website.woodendoor.conflux.models.LoginRequest
import website.woodendoor.conflux.models.User
import website.woodendoor.conflux.validation.UsernameValidator
import website.woodendoor.conflux.validation.ValidationResult
import java.util.UUID

fun Route.userRoutes(userRepository: UserRepository) {
    post("/api/login") {
        try {
            val request = call.receive<LoginRequest>()
            
            // Validate username
            when (val validationResult = UsernameValidator.validateUsername(request.username)) {
                is ValidationResult.Error -> {
                    call.respond(HttpStatusCode.BadRequest, validationResult.message)
                    return@post
                }
                ValidationResult.Success -> { /* OK */ }
            }
            
            // Check if user exists
            val existingUser = userRepository.findByUsername(request.username)
            if (existingUser != null) {
                call.respond(HttpStatusCode.OK, existingUser)
                return@post
            }
            
            // Create new user
            val newUser = User(
                id = UUID.randomUUID().toString(),
                username = request.username,
                discriminator = (1000..9999).random().toString()
            )
            val created = userRepository.createUser(newUser)
            if (created != null) {
                call.respond(HttpStatusCode.OK, created)
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Failed to create user")
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
        }
    }
}
