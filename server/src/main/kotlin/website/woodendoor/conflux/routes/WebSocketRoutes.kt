package website.woodendoor.conflux.routes

import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import website.woodendoor.conflux.WebSocketConnectionManager
import website.woodendoor.conflux.auth.WebSocketAuthTokenManager
import website.woodendoor.conflux.models.ConfluxEvent

fun Route.webSocketRoutes(tokenManager: WebSocketAuthTokenManager, connectionManager: WebSocketConnectionManager) {
    post("/api/auth/ws-token") {
        val userId = call.receiveText() // Simple for now
        if (userId.isBlank()) {
            call.respond(io.ktor.http.HttpStatusCode.BadRequest, "User ID required")
            return@post
        }
        val token = tokenManager.generateToken(userId)
        call.respond(mapOf("token" to token))
    }

    webSocket("/ws") {
        val token = call.request.queryParameters["token"]
        if (token == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Token required"))
            return@webSocket
        }

        val userId = tokenManager.validateToken(token)
        if (userId == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid token"))
            return@webSocket
        }

        // Successfully connected
        connectionManager.addConnection(userId, this)
        
        try {
            // No need to send raw text confirmation, use events if needed or just wait
            
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    // Simple protocol: "subscribe:channelId"
                    if (text.startsWith("subscribe:")) {
                        val channelId = text.substringAfter("subscribe:")
                        connectionManager.subscribeToChannel(userId, channelId)
                        send(Frame.Text(Json.encodeToString<ConfluxEvent>(ConfluxEvent.SubscriptionSuccess(channelId))))
                    } else if (text.startsWith("subscribe_server:")) {
                        val serverId = text.substringAfter("subscribe_server:")
                        connectionManager.subscribeToServer(userId, serverId)
                    } else {
                        // Handle other incoming text or ignore
                    }
                }
            }
        } finally {
            connectionManager.removeConnection(userId, this)
        }
    }
}
