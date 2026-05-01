package website.woodendoor.conflux.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import website.woodendoor.conflux.database.repositories.ServerRepository
import website.woodendoor.conflux.models.CreateServerRequest
import website.woodendoor.conflux.models.Server
import java.util.UUID

fun Route.serverRoutes(serverRepository: ServerRepository) {
    route("/api/servers") {
        post {
            try {
                val request = call.receive<CreateServerRequest>()
                val server = Server(
                    id = UUID.randomUUID().toString(),
                    name = request.name,
                    ownerId = "default-user", // Placeholder until auth is implemented
                    icon = request.iconUrl
                )
                val created = serverRepository.createServer(server)
                if (created != null) {
                    call.respond(HttpStatusCode.Created, created)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to create server")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
            }
        }
    }
}
