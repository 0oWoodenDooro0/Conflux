package website.woodendoor.conflux

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import website.woodendoor.conflux.models.ConfluxEvent
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

class WebSocketConnectionManager {
    // userId -> set of active sessions
    private val userSessions = ConcurrentHashMap<String, MutableSet<DefaultWebSocketServerSession>>()
    
    // channelId -> set of userIds subscribed to it
    private val channelSubscriptions = ConcurrentHashMap<String, MutableSet<String>>()

    // serverId -> set of userIds subscribed to it
    private val serverSubscriptions = ConcurrentHashMap<String, MutableSet<String>>()

    fun addConnection(userId: String, session: DefaultWebSocketServerSession) {
        userSessions.computeIfAbsent(userId) { CopyOnWriteArraySet() }.add(session)
    }

    fun removeConnection(userId: String, session: DefaultWebSocketServerSession) {
        userSessions[userId]?.remove(session)
        if (userSessions[userId]?.isEmpty() == true) {
            userSessions.remove(userId)
        }
    }

    fun subscribeToChannel(userId: String, channelId: String) {
        channelSubscriptions.computeIfAbsent(channelId) { CopyOnWriteArraySet() }.add(userId)
    }

    fun unsubscribeFromChannel(userId: String, channelId: String) {
        channelSubscriptions[channelId]?.remove(userId)
    }

    fun subscribeToServer(userId: String, serverId: String) {
        serverSubscriptions.computeIfAbsent(serverId) { CopyOnWriteArraySet() }.add(userId)
    }

    fun unsubscribeFromServer(userId: String, serverId: String) {
        serverSubscriptions[serverId]?.remove(userId)
    }

    fun getConnectionsForChannel(channelId: String): List<DefaultWebSocketServerSession> {
        val userIds = channelSubscriptions[channelId] ?: return emptyList()
        return userIds.flatMap { userId ->
            userSessions[userId] ?: emptyList()
        }
    }

    fun getConnectionsForServer(serverId: String): List<DefaultWebSocketServerSession> {
        val userIds = serverSubscriptions[serverId] ?: return emptyList()
        return userIds.flatMap { userId ->
            userSessions[userId] ?: emptyList()
        }
    }

    suspend fun broadcastToServer(serverId: String, event: ConfluxEvent) {
        val connections = getConnectionsForServer(serverId)
        val eventJson = Json.encodeToString<ConfluxEvent>(event)
        
        coroutineScope {
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
    }
}
