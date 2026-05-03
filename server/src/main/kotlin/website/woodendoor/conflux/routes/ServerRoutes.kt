package website.woodendoor.conflux.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import website.woodendoor.conflux.database.repositories.ChannelRepository
import website.woodendoor.conflux.database.repositories.ServerRepository
import website.woodendoor.conflux.database.repositories.UserRepository
import website.woodendoor.conflux.models.Channel
import website.woodendoor.conflux.models.ChannelType
import website.woodendoor.conflux.models.CreateChannelRequest
import website.woodendoor.conflux.models.CreateServerRequest
import website.woodendoor.conflux.models.Server
import website.woodendoor.conflux.models.User
import java.util.UUID

fun Route.serverRoutes(serverRepository: ServerRepository, userRepository: UserRepository) {
    route("/api/servers") {
        get {
            val userId = call.request.queryParameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing userId")
            val servers = serverRepository.getServersForUser(userId)
            call.respond(HttpStatusCode.OK, servers)
        }

        post {
            try {
                val request = call.receive<CreateServerRequest>()
                
                // TODO: Replace with proper authentication check. 
                // Currently auto-creates user if not found to facilitate development.
                // Resolve or create owner with UUID
                val owner = userRepository.getUser(request.ownerId) ?: userRepository.findByUsername(request.ownerId)
                val resolvedOwnerId = if (owner == null) {
                    val newUserId = UUID.randomUUID().toString()
                    userRepository.createUser(
                        User(
                            id = newUserId,
                            username = request.ownerId,
                            discriminator = "0000"
                        )
                    )
                    newUserId
                } else {
                    owner.id
                }

                val server = Server(
                    id = UUID.randomUUID().toString(),
                    name = request.name,
                    ownerId = resolvedOwnerId,
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

        post("/{id}/join") {
            val serverId = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing userId")
            
            val server = serverRepository.getServer(serverId)
            if (server == null) {
                return@post call.respond(HttpStatusCode.NotFound, "Server not found")
            }

            val result = serverRepository.joinServer(userId, serverId)
            if (result) {
                call.respond(HttpStatusCode.Created, "Joined successfully")
            } else {
                call.respond(HttpStatusCode.Conflict, "Already a member or owner")
            }
        }
    }
}

fun Route.channelRoutes(channelRepository: ChannelRepository, serverRepository: ServerRepository) {
    route("/api/servers/{serverId}/channels") {
        get {
            val serverId = call.parameters["serverId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            if (serverRepository.getServer(serverId) == null) {
                return@get call.respond(HttpStatusCode.NotFound, "Server not found")
            }
            val channels = channelRepository.getChannelsByServer(serverId)
            call.respond(HttpStatusCode.OK, channels)
        }

        post {
            val serverId = call.parameters["serverId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing userId")
            
            if (serverRepository.getServer(serverId) == null) {
                return@post call.respond(HttpStatusCode.NotFound, "Server not found")
            }

            val permissions = serverRepository.getPermissionsForMember(serverId, userId)
            if ((permissions and website.woodendoor.conflux.models.ConfluxPermission.CHANNEL_MANAGEMENT) == 0L) {
                return@post call.respond(HttpStatusCode.Forbidden, "Insufficient permissions")
            }

            try {
                val request = call.receive<CreateChannelRequest>()
                val channel = Channel(
                    id = UUID.randomUUID().toString(),
                    serverId = serverId,
                    name = request.name,
                    type = request.type,
                    topic = request.topic
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

fun Route.roleRoutes(serverRepository: ServerRepository) {
    route("/api/servers/{serverId}/roles") {
        get {
            val serverId = call.parameters["serverId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val roles = serverRepository.getRoles(serverId)
            call.respond(HttpStatusCode.OK, roles)
        }

        post {
            val serverId = call.parameters["serverId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing userId")
            
            val permissions = serverRepository.getPermissionsForMember(serverId, userId)
            if ((permissions and website.woodendoor.conflux.models.ConfluxPermission.ROLE_MANAGEMENT) == 0L) {
                return@post call.respond(HttpStatusCode.Forbidden, "Insufficient permissions")
            }

            try {
                val request = call.receive<website.woodendoor.conflux.models.CreateRoleRequest>()
                val role = website.woodendoor.conflux.models.Role(
                    id = UUID.randomUUID().toString(),
                    name = request.name,
                    permissions = request.permissions,
                    color = request.color,
                    priorityLevel = request.priorityLevel
                )
                val created = serverRepository.createRole(serverId, role)
                if (created != null) {
                    call.respond(HttpStatusCode.Created, created)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to create role")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
            }
        }
        
        post("/assign") {
            val serverId = call.parameters["serverId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing userId")
            
            val permissions = serverRepository.getPermissionsForMember(serverId, userId)
            if ((permissions and website.woodendoor.conflux.models.ConfluxPermission.ROLE_MANAGEMENT) == 0L) {
                return@post call.respond(HttpStatusCode.Forbidden, "Insufficient permissions")
            }

            try {
                val request = call.receive<website.woodendoor.conflux.models.AssignRoleRequest>()
                val result = serverRepository.assignRoleToMember(serverId, request.userId, request.roleId)
                if (result) {
                    call.respond(HttpStatusCode.OK, "Role assigned successfully")
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to assign role")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
            }
        }
    }
}
