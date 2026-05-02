package website.woodendoor.conflux

import io.ktor.server.websocket.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

class WebSocketConnectionManager {
    // userId -> set of active sessions
    private val userSessions = ConcurrentHashMap<String, MutableSet<DefaultWebSocketServerSession>>()
    
    // channelId -> set of userIds subscribed to it
    private val channelSubscriptions = ConcurrentHashMap<String, MutableSet<String>>()

    fun addConnection(userId: String, session: DefaultWebSocketServerSession) {
        userSessions.computeIfAbsent(userId) { CopyOnWriteArraySet() }.add(session)
    }

    fun removeConnection(userId: String, session: DefaultWebSocketServerSession) {
        userSessions[userId]?.remove(session)
        if (userSessions[userId]?.isEmpty() == true) {
            userSessions.remove(userId)
            // Optionally unsubscribe from all channels if user has no more sessions
            // But for now, let's keep subscriptions linked to userId
        }
    }

    fun subscribeToChannel(userId: String, channelId: String) {
        channelSubscriptions.computeIfAbsent(channelId) { CopyOnWriteArraySet() }.add(userId)
    }

    fun unsubscribeFromChannel(userId: String, channelId: String) {
        channelSubscriptions[channelId]?.remove(userId)
    }

    fun getConnectionsForChannel(channelId: String): List<DefaultWebSocketServerSession> {
        val userIds = channelSubscriptions[channelId] ?: return emptyList()
        return userIds.flatMap { userId ->
            userSessions[userId] ?: emptyList()
        }
    }
}
