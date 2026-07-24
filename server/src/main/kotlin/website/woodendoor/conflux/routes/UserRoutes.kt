package website.woodendoor.conflux.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import website.woodendoor.conflux.controller.UserController
import website.woodendoor.conflux.models.LoginRequest
import website.woodendoor.conflux.models.RegisterRequest
import website.woodendoor.conflux.models.UpdateProfileRequest
import website.woodendoor.conflux.models.UpdateStatusRequest

fun Route.userRoutes(userController: UserController) {
    post("/api/login") {
        val request = call.receive<LoginRequest>()
        val authResponse = userController.login(request)
        call.respond(HttpStatusCode.OK, authResponse)
    }

    post("/api/register") {
        val request = call.receive<RegisterRequest>()
        val authResponse = userController.register(request)
        call.respond(HttpStatusCode.OK, authResponse)
    }

    post("/api/auth/login") {
        val request = call.receive<LoginRequest>()
        val authResponse = userController.login(request)
        call.respond(HttpStatusCode.OK, authResponse)
    }

    post("/api/auth/register") {
        val request = call.receive<RegisterRequest>()
        val authResponse = userController.register(request)
        call.respond(HttpStatusCode.OK, authResponse)
    }

    get("/api/users/{userId}") {
        val userId = call.parameters["userId"] ?: throw BadRequestException("Missing userId")
        val user = userController.getUser(userId)
        call.respond(HttpStatusCode.OK, user)
    }

    put("/api/users/{userId}/status") {
        val userId = call.parameters["userId"] ?: throw BadRequestException("Missing userId")
        val request = call.receive<UpdateStatusRequest>()
        val updatedUser = userController.updateStatus(userId, request)
        call.respond(HttpStatusCode.OK, updatedUser)
    }

    put("/api/users/{userId}/profile") {
        val userId = call.parameters["userId"] ?: throw BadRequestException("Missing userId")
        val request = call.receive<UpdateProfileRequest>()
        val updatedUser = userController.updateProfile(userId, request)
        call.respond(HttpStatusCode.OK, updatedUser)
    }
}
