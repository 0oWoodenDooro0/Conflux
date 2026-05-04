package website.woodendoor.conflux.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import website.woodendoor.conflux.controller.ChatController
import website.woodendoor.conflux.controller.respond
import website.woodendoor.conflux.models.SendMessageRequest

fun Route.messageRoutes(
    chatController: ChatController
) {
    route("/api/channels/{channelId}/messages") {
        get {
            val channelId = call.parameters["channelId"] ?: return@get call.respond(status = HttpStatusCode.BadRequest, message = "Missing channelId")
            val result = chatController.getMessagesByChannel(channelId)
            call.respond(result)
        }

        post {
            val channelId = call.parameters["channelId"] ?: return@post call.respond(status = HttpStatusCode.BadRequest, message = "Missing channelId")
            try {
                val request = call.receive<SendMessageRequest>()
                val result = chatController.sendMessage(channelId, request)
                call.respond(result, HttpStatusCode.Created)
            } catch (e: Exception) {
                call.respond(status = HttpStatusCode.BadRequest, message = "Invalid request: ${e.message}")
            }
        }
    }
}
