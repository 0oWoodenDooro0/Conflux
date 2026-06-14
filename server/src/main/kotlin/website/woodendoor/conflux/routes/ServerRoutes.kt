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
            val request = call.receive<CreateServerRequest>()
            val result = serverController.createServer(request)
            call.respond(result, HttpStatusCode.Created)
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
            
            val request = call.receive<CreateChannelRequest>()
            val result = channelController.createChannel(serverId, userId, request)
            call.respond(result, HttpStatusCode.Created)
        }

        patch("/{channelId}") {
            val serverId = call.parameters["serverId"] ?: return@patch call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@patch call.respond(HttpStatusCode.BadRequest, "Missing userId")
            val channelId = call.parameters["channelId"] ?: return@patch call.respond(HttpStatusCode.BadRequest, "Missing channelId")
            
            val request = call.receive<UpdateChannelRequest>()
            val result = channelController.editChannel(channelId, userId, request)
            call.respond(result)
        }

        delete("/{channelId}") {
            val serverId = call.parameters["serverId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing userId")
            val channelId = call.parameters["channelId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing channelId")
            
            val result = channelController.deleteChannel(channelId, userId)
            call.respond(result)
        }

        get("/{channelId}/overrides") {
            val serverId = call.parameters["serverId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing userId")
            val channelId = call.parameters["channelId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing channelId")

            val result = channelController.getOverrides(channelId, userId)
            call.respond(result)
        }

        post("/{channelId}/overrides") {
            val serverId = call.parameters["serverId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing userId")
            val channelId = call.parameters["channelId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing channelId")

            val request = call.receive<UpsertOverrideRequest>()
            val result = channelController.upsertOverride(channelId, userId, request)
            call.respond(result)
        }

        delete("/{channelId}/overrides/{overrideId}") {
            val serverId = call.parameters["serverId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing userId")
            val channelId = call.parameters["channelId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing channelId")
            val overrideId = call.parameters["overrideId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing overrideId")

            val result = channelController.deleteOverride(serverId, userId, overrideId)
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
            
            val request = call.receive<CreateRoleRequest>()
            if (request.priorityLevel !in 0..100) {
                return@post call.respond(HttpStatusCode.BadRequest, "Priority level must be between 0 and 100")
            }
            val result = roleController.createRole(serverId, userId, request)
            call.respond(result, HttpStatusCode.Created)
        }

        patch("/{roleId}") {
            val serverId = call.parameters["serverId"] ?: return@patch call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@patch call.respond(HttpStatusCode.BadRequest, "Missing userId")
            val roleId = call.parameters["roleId"] ?: return@patch call.respond(HttpStatusCode.BadRequest, "Missing roleId")

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
            
            val result = roleController.updateRole(serverId, userId, updatedRole)
            call.respond(result)
        }

        delete("/{roleId}") {
            val serverId = call.parameters["serverId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing userId")
            val roleId = call.parameters["roleId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing roleId")

            val existingRole = when (val getResult = roleController.getRole(roleId)) {
                is OperationResult.Success -> getResult.data
                is OperationResult.Failure -> return@delete call.respond(getResult)
            }

            if (existingRole.priorityLevel == DEFAULT_ROLE_PRIORITY_EVERYONE) {
                return@delete call.respond(HttpStatusCode.BadRequest, "Cannot delete the @everyone role")
            }

            val result = roleController.deleteRole(serverId, userId, roleId)
            call.respond(result)
        }
        
        post("/assign") {
            val serverId = call.parameters["serverId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing userId")
            
            val request = call.receive<AssignRoleRequest>()
            val result = roleController.assignRoleToMember(serverId, userId, request.userId, request.roleId)
            call.respond(result)
        }

        post("/remove") {
            val serverId = call.parameters["serverId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing userId")
            
            val request = call.receive<AssignRoleRequest>()
            val result = roleController.removeRoleFromMember(serverId, userId, request.userId, request.roleId)
            call.respond(result)
        }

        get("/{roleId}/members") {
            val serverId = call.parameters["serverId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val roleId = call.parameters["roleId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing roleId")
            
            val result = roleController.getMembersWithRole(serverId, roleId)
            call.respond(result)
        }
    }
}
