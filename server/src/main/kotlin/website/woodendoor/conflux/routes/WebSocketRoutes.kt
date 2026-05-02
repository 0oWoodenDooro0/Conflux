package website.woodendoor.conflux.routes

import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import website.woodendoor.conflux.auth.WebSocketAuthTokenManager

fun Route.webSocketRoutes(tokenManager: WebSocketAuthTokenManager) {
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
        send("Connected as $userId")
        
        for (frame in incoming) {
            if (frame is Frame.Text) {
                val text = frame.readText()
                send("Echo: $text")
            }
        }
    }
}
