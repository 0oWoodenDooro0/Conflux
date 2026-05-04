package website.woodendoor.conflux.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import website.woodendoor.conflux.controller.UserController
import website.woodendoor.conflux.controller.respond
import website.woodendoor.conflux.models.LoginRequest

fun Route.userRoutes(userController: UserController) {
    post("/api/login") {
        try {
            val request = call.receive<LoginRequest>()
            val result = userController.login(request)
            call.respond(result, HttpStatusCode.OK)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
        }
    }
}
