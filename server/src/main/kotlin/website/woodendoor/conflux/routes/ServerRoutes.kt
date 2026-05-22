package website.woodendoor.conflux.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import website.woodendoor.conflux.controller.*
import website.woodendoor.conflux.database.repositories.ServerRepository
import website.woodendoor.conflux.database.repositories.UserRepository
import website.woodendoor.conflux.models.*
import website.woodendoor.conflux.DEFAULT_ROLE_PRIORITY_EVERYONE
import java.util.*

fun Route.serverRoutes(
    serverController: ServerController
) {
    route("/api/servers") {
        get {
            val userId = call.request.queryParameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing userId")
            val result = serverController.getServersForUser(userId)
            call.respond(result)
        }

        get("/{id}/members") {
            val serverId = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val channelId = call.request.queryParameters["channelId"]
            val result = serverController.getMembers(serverId, channelId)
            call.respond(result)
        }

        post {
            try {
                val request = call.receive<CreateServerRequest>()
                val result = serverController.createServer(request)
                call.respond(result, HttpStatusCode.Created)
            } catch (e: Exception) {
                call.respond(status = HttpStatusCode.BadRequest, message = "Invalid request: ${e.message}")
            }
        }

        post("/{id}/join") {
            val serverId = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing userId")

            val result = serverController.joinServer(userId, serverId)
            call.respond(result, HttpStatusCode.Created)
        }

        get("/{id}/members/{userId}/permissions") {
            val serverId = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing userId")
            val result = serverController.getPermissionsForMember(serverId, userId)
            call.respond(result)
        }
    }
}


fun Route.channelRoutes(
    channelController: ChannelController,
    roleController: RoleController
) {
    route("/api/servers/{serverId}/channels") {
        get {
            val serverId = call.parameters["serverId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"]
            val result = channelController.getChannelsByServer(serverId, userId)
            call.respond(result)
        }

        post {
            val serverId = call.parameters["serverId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing userId")
            
            if (!roleController.hasPermission(serverId, userId, ConfluxPermission.CHANNEL_MANAGEMENT)) {
                return@post call.respond(HttpStatusCode.Forbidden, "Insufficient permissions")
            }

            try {
                val request = call.receive<CreateChannelRequest>()
                val result = channelController.createChannel(serverId, request)
                call.respond(result, HttpStatusCode.Created)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
            }
        }

        patch("/{channelId}") {
            val serverId = call.parameters["serverId"] ?: return@patch call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@patch call.respond(HttpStatusCode.BadRequest, "Missing userId")
            val channelId = call.parameters["channelId"] ?: return@patch call.respond(HttpStatusCode.BadRequest, "Missing channelId")
            
            if (!channelController.hasPermission(serverId, channelId, userId, ConfluxPermission.CHANNEL_MANAGEMENT)) {
                return@patch call.respond(HttpStatusCode.Forbidden, "Insufficient permissions")
            }

            try {
                val request = call.receive<UpdateChannelRequest>()
                val result = channelController.editChannel(channelId, request)
                call.respond(result)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
            }
        }

        delete("/{channelId}") {
            val serverId = call.parameters["serverId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing userId")
            val channelId = call.parameters["channelId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing channelId")
            
            if (!channelController.hasPermission(serverId, channelId, userId, ConfluxPermission.CHANNEL_MANAGEMENT)) {
                return@delete call.respond(HttpStatusCode.Forbidden, "Insufficient permissions")
            }

            try {
                val result = channelController.deleteChannel(channelId)
                call.respond(result)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
            }
        }

        get("/{channelId}/overrides") {
            val serverId = call.parameters["serverId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing userId")
            val channelId = call.parameters["channelId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing channelId")

            if (!channelController.hasPermission(serverId, channelId, userId, ConfluxPermission.CHANNEL_MANAGEMENT)) {
                return@get call.respond(HttpStatusCode.Forbidden, "Insufficient permissions")
            }

            val result = channelController.getOverrides(channelId)
            call.respond(result)
        }

        post("/{channelId}/overrides") {
            val serverId = call.parameters["serverId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing userId")
            val channelId = call.parameters["channelId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing channelId")

            if (!channelController.hasPermission(serverId, channelId, userId, ConfluxPermission.CHANNEL_MANAGEMENT)) {
                return@post call.respond(HttpStatusCode.Forbidden, "Insufficient permissions")
            }

            try {
                val request = call.receive<UpsertOverrideRequest>()
                val result = channelController.upsertOverride(channelId, request)
                call.respond(result)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
            }
        }

        delete("/{channelId}/overrides/{overrideId}") {
            val serverId = call.parameters["serverId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing userId")
            val channelId = call.parameters["channelId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing channelId")
            val overrideId = call.parameters["overrideId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing overrideId")

            if (!channelController.hasPermission(serverId, channelId, userId, ConfluxPermission.CHANNEL_MANAGEMENT)) {
                return@delete call.respond(HttpStatusCode.Forbidden, "Insufficient permissions")
            }

            val result = channelController.deleteOverride(serverId, overrideId)
            call.respond(result)
        }

        get("/{channelId}/permissions") {
            val serverId = call.parameters["serverId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing userId")
            val channelId = call.parameters["channelId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing channelId")

            val result = channelController.getEffectivePermissions(serverId, channelId, userId)
            call.respond(result)
        }
        }
        }
fun Route.roleRoutes(roleController: RoleController) {
    route("/api/servers/{serverId}/roles") {
        get {
            val serverId = call.parameters["serverId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val result = roleController.getRoles(serverId)
            call.respond(result)
        }

        post {
            val serverId = call.parameters["serverId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing userId")
            
            if (!roleController.hasPermission(serverId, userId, ConfluxPermission.ROLE_MANAGEMENT)) {
                return@post call.respond(HttpStatusCode.Forbidden, "Insufficient permissions")
            }

            try {
                val request = call.receive<CreateRoleRequest>()
                if (request.priorityLevel !in 0..100) {
                    return@post call.respond(HttpStatusCode.BadRequest, "Priority level must be between 0 and 100")
                }
                val result = roleController.createRole(serverId, request)
                call.respond(result, HttpStatusCode.Created)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
            }
        }

        patch("/{roleId}") {
            val serverId = call.parameters["serverId"] ?: return@patch call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@patch call.respond(HttpStatusCode.BadRequest, "Missing userId")
            val roleId = call.parameters["roleId"] ?: return@patch call.respond(HttpStatusCode.BadRequest, "Missing roleId")

            if (!roleController.hasPermission(serverId, userId, ConfluxPermission.ROLE_MANAGEMENT)) {
                return@patch call.respond(HttpStatusCode.Forbidden, "Insufficient permissions")
            }

            try {
                val request = call.receive<UpdateRoleRequest>()
                
                val existingRole = when (val getResult = roleController.getRole(roleId)) {
                    is OperationResult.Success -> getResult.data
                    is OperationResult.Failure -> return@patch call.respond(getResult)
                }

                if (existingRole.priorityLevel == DEFAULT_ROLE_PRIORITY_EVERYONE) {
                    if (request.name != null && request.name != existingRole.name) {
                        return@patch call.respond(HttpStatusCode.BadRequest, "Cannot rename the @everyone role")
                    }
                    if (request.priorityLevel != null && request.priorityLevel != existingRole.priorityLevel) {
                        return@patch call.respond(HttpStatusCode.BadRequest, "Cannot change the priority of the @everyone role")
                    }
                } else {
                    // For custom roles, ensure priority is in 0..100
                    if (request.priorityLevel != null && request.priorityLevel !in 0..100) {
                        return@patch call.respond(HttpStatusCode.BadRequest, "Priority level must be between 0 and 100")
                    }
                }
                
                val updatedRole = existingRole.copy(
                    name = request.name ?: existingRole.name,
                    permissions = request.permissions ?: existingRole.permissions,
                    color = if (request.color != null) request.color else existingRole.color,
                    priorityLevel = request.priorityLevel ?: existingRole.priorityLevel
                )
                
                val result = roleController.updateRole(updatedRole)
                call.respond(result)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
            }
        }

        delete("/{roleId}") {
            val serverId = call.parameters["serverId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing userId")
            val roleId = call.parameters["roleId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing roleId")

            if (!roleController.hasPermission(serverId, userId, ConfluxPermission.ROLE_MANAGEMENT)) {
                return@delete call.respond(HttpStatusCode.Forbidden, "Insufficient permissions")
            }

            try {
                val existingRole = when (val getResult = roleController.getRole(roleId)) {
                    is OperationResult.Success -> getResult.data
                    is OperationResult.Failure -> return@delete call.respond(getResult)
                }

                if (existingRole.priorityLevel == DEFAULT_ROLE_PRIORITY_EVERYONE) {
                    return@delete call.respond(HttpStatusCode.BadRequest, "Cannot delete the @everyone role")
                }

                val result = roleController.deleteRole(roleId)
                call.respond(result)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
            }
        }
        
        post("/assign") {
            val serverId = call.parameters["serverId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing userId")
            
            if (!roleController.hasPermission(serverId, userId, ConfluxPermission.ROLE_MANAGEMENT)) {
                return@post call.respond(HttpStatusCode.Forbidden, "Insufficient permissions")
            }

            try {
                val request = call.receive<AssignRoleRequest>()
                val result = roleController.assignRoleToMember(serverId, request.userId, request.roleId)
                call.respond(result)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
            }
        }

        post("/remove") {
            val serverId = call.parameters["serverId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing userId")
            
            if (!roleController.hasPermission(serverId, userId, ConfluxPermission.ROLE_MANAGEMENT)) {
                return@post call.respond(HttpStatusCode.Forbidden, "Insufficient permissions")
            }

            try {
                val request = call.receive<AssignRoleRequest>()
                val result = roleController.removeRoleFromMember(serverId, request.userId, request.roleId)
                call.respond(result)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
            }
        }

        get("/{roleId}/members") {
            val serverId = call.parameters["serverId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val roleId = call.parameters["roleId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing roleId")
            
            val result = roleController.getMembersWithRole(serverId, roleId)
            call.respond(result)
        }
    }
}
