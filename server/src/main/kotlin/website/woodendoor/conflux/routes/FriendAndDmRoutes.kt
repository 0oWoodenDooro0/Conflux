package website.woodendoor.conflux.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import website.woodendoor.conflux.controller.FriendAndDmController
import website.woodendoor.conflux.models.FriendRequestPayload
import website.woodendoor.conflux.models.OpenDmRequest

fun Route.friendAndDmRoutes(friendAndDmController: FriendAndDmController) {
    route("/api/friends") {
        get {
            val userId = call.request.queryParameters["userId"] ?: throw BadRequestException("Missing userId")
            val friends = friendAndDmController.getFriends(userId)
            call.respond(HttpStatusCode.OK, friends)
        }

        post("/request") {
            val userId = call.request.queryParameters["userId"] ?: throw BadRequestException("Missing userId")
            val payload = call.receive<FriendRequestPayload>()
            val friendship = friendAndDmController.sendFriendRequest(userId, payload)
            call.respond(HttpStatusCode.Created, friendship)
        }

        post("/{friendshipId}/accept") {
            val userId = call.request.queryParameters["userId"] ?: throw BadRequestException("Missing userId")
            val friendshipId = call.parameters["friendshipId"] ?: throw BadRequestException("Missing friendshipId")
            val success = friendAndDmController.acceptFriendRequest(userId, friendshipId)
            call.respond(HttpStatusCode.OK, mapOf("success" to success))
        }

        delete("/{friendshipId}") {
            val userId = call.request.queryParameters["userId"] ?: throw BadRequestException("Missing userId")
            val friendshipId = call.parameters["friendshipId"] ?: throw BadRequestException("Missing friendshipId")
            val success = friendAndDmController.removeFriend(userId, friendshipId)
            call.respond(HttpStatusCode.OK, mapOf("success" to success))
        }
    }

    route("/api/dm") {
        get("/channels") {
            val userId = call.request.queryParameters["userId"] ?: throw BadRequestException("Missing userId")
            val channels = friendAndDmController.getUserDmChannels(userId)
            call.respond(HttpStatusCode.OK, channels)
        }

        post("/open") {
            val userId = call.request.queryParameters["userId"] ?: throw BadRequestException("Missing userId")
            val payload = call.receive<OpenDmRequest>()
            val channel = friendAndDmController.openDmChannel(userId, payload)
            call.respond(HttpStatusCode.OK, channel)
        }
    }
}
