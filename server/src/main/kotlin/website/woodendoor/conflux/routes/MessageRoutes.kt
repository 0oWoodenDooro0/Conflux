package website.woodendoor.conflux.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import website.woodendoor.conflux.WebSocketConnectionManager
import website.woodendoor.conflux.database.repositories.ChannelRepository
import website.woodendoor.conflux.database.repositories.MessageRepository
import website.woodendoor.conflux.database.repositories.ServerRepository
import website.woodendoor.conflux.models.ConfluxEvent
import website.woodendoor.conflux.models.SendMessageRequest
import website.woodendoor.conflux.models.ConfluxPermission

fun Route.messageRoutes(
    messageRepository: MessageRepository,
    channelRepository: ChannelRepository,
    serverRepository: ServerRepository,
    connectionManager: WebSocketConnectionManager
) {
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
                
                // Permission Check
                val channel = channelRepository.getChannel(channelId) ?: return@post call.respond(HttpStatusCode.NotFound, "Channel not found")
                val permissions = serverRepository.getPermissionsForMember(channel.serverId, request.senderId)
                if ((permissions and ConfluxPermission.MESSAGING) == 0L) {
                    return@post call.respond(HttpStatusCode.Forbidden, "Insufficient permissions")
                }

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
                    // Broadcast to WebSocket subscribers
                    val connections = connectionManager.getConnectionsForChannel(channelId)
                    val event = ConfluxEvent.NewMessage(message)
                    val eventJson = Json.encodeToString<ConfluxEvent>(event)
                    kotlinx.coroutines.coroutineScope {
                        connections.forEach { session ->
                            launch {
                                try {
                                    session.send(Frame.Text(eventJson))
                                } catch (e: Exception) {
                                    // Session might be closed
                                }
                            }
                        }
                    }
                    
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
