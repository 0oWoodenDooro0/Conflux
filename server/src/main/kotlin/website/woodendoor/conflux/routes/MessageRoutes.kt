package website.woodendoor.conflux.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import website.woodendoor.conflux.database.repositories.MessageRepository
import website.woodendoor.conflux.models.SendMessageRequest

fun Route.messageRoutes(messageRepository: MessageRepository) {
    route("/api/channels/{channelId}/messages") {
        get {
            val channelId = call.parameters["channelId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing channelId")
            val messages = messageRepository.getMessagesByChannel(channelId)
            call.respond(HttpStatusCode.OK, messages)
        }

        post {
            val channelId = call.parameters["channelId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing channelId")
            try {
                val request = call.receive<SendMessageRequest>()
                
                if (request.content.length > 2000) {
                    return@post call.respond(HttpStatusCode.BadRequest, "Message too long (max 2000 characters)")
                }
                if (request.content.isBlank()) {
                    return@post call.respond(HttpStatusCode.BadRequest, "Message cannot be empty")
                }

                val message = messageRepository.saveMessage(
                    channelId = channelId,
                    senderId = request.senderId,
                    content = request.content
                )
                if (message != null) {
                    call.respond(HttpStatusCode.Created, message)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to save message")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
            }
        }
    }
}
