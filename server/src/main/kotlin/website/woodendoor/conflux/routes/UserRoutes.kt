package website.woodendoor.conflux.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import website.woodendoor.conflux.controller.UserController
import website.woodendoor.conflux.controller.respond
import website.woodendoor.conflux.models.LoginRequest

import website.woodendoor.conflux.exceptions.BadRequestException

fun Route.userRoutes(userController: UserController) {
    post("/api/login") {
        val request = call.receive<LoginRequest>()
        val user = userController.login(request)
        call.respond(HttpStatusCode.OK, user)
    }

    get("/api/users/{userId}") {
        val userId = call.parameters["userId"] ?: throw BadRequestException("Missing userId")
        val user = userController.getUser(userId)
        call.respond(HttpStatusCode.OK, user)
    }
}
