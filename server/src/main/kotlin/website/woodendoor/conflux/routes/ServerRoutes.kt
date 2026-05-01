package website.woodendoor.conflux.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import website.woodendoor.conflux.database.repositories.ChannelRepository
import website.woodendoor.conflux.database.repositories.ServerRepository
import website.woodendoor.conflux.models.Channel
import website.woodendoor.conflux.models.ChannelType
import website.woodendoor.conflux.models.CreateChannelRequest
import website.woodendoor.conflux.models.CreateServerRequest
import website.woodendoor.conflux.models.Server
import java.util.UUID

fun Route.serverRoutes(serverRepository: ServerRepository) {
    route("/api/servers") {
        get {
            val userId = call.request.queryParameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing userId")
            val servers = serverRepository.getServersForUser(userId)
            call.respond(HttpStatusCode.OK, servers)
        }

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

fun Route.channelRoutes(channelRepository: ChannelRepository, serverRepository: ServerRepository) {
    route("/api/servers/{serverId}/channels") {
        post {
            val serverId = call.parameters["serverId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            if (serverRepository.getServer(serverId) == null) {
                return@post call.respond(HttpStatusCode.NotFound, "Server not found")
            }
            try {
                val request = call.receive<CreateChannelRequest>()
                val channel = Channel(
                    id = UUID.randomUUID().toString(),
                    serverId = serverId,
                    name = request.name,
                    type = ChannelType.TEXT, // Default to text for now
                    topic = null
                )
                val created = channelRepository.createChannel(channel)
                if (created != null) {
                    call.respond(HttpStatusCode.Created, created)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to create channel")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
            }
        }
    }
}
