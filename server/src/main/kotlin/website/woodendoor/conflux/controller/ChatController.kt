package website.woodendoor.conflux.controller

import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import website.woodendoor.conflux.WebSocketConnectionManager
import website.woodendoor.conflux.models.*
import website.woodendoor.conflux.service.ChatService
import website.woodendoor.conflux.service.ChannelService
import website.woodendoor.conflux.service.ChannelPermissionService
import website.woodendoor.conflux.service.ServerService

class ChatController(
    private val chatService: ChatService,
    private val channelService: ChannelService,
    private val channelPermissionService: ChannelPermissionService,
    private val serverService: ServerService,
    private val connectionManager: WebSocketConnectionManager
) {

    suspend fun sendMessage(channelId: String, request: SendMessageRequest): OperationResult<Message> {
        val channel = channelService.getChannel(channelId) ?: return OperationResult.Failure.NotFound("Channel not found")
        
        // Verify membership
        val members = serverService.getMembers(channel.serverId)
        if (members.none { it.id == request.senderId }) {
            return OperationResult.Failure.Forbidden("User is not a member of this server")
        }
        
        val permissions = channelPermissionService.getEffectivePermissions(channel.serverId, channelId, request.senderId)
        if (!ConfluxPermission.hasPermission(permissions, ConfluxPermission.MESSAGING)) {
            return OperationResult.Failure.Forbidden("No messaging permission in this channel")
        }

        if (request.content.length > 2000) {
            return OperationResult.Failure.BadRequest("Message too long (max 2000 characters)")
        }
        if (request.content.isBlank()) {
            return OperationResult.Failure.BadRequest("Message cannot be empty")
        }

        val message = chatService.sendMessage(
            channelId = channelId,
            senderId = request.senderId,
            content = request.content
        ) ?: return OperationResult.Failure.InternalError("Failed to save message")

        broadcastMessage(channelId, message)

        return OperationResult.Success(message)
    }

    suspend fun getMessagesByChannel(channelId: String): OperationResult<List<Message>> {
        val messages = chatService.getMessagesByChannel(channelId)
        return OperationResult.Success(messages)
    }

    private suspend fun broadcastMessage(channelId: String, message: Message) {
        val channel = channelService.getChannel(channelId) ?: return
        val serverId = channel.serverId
        
        val event = ConfluxEvent.NewMessage(message, serverId)
        val eventJson = Json.encodeToString<ConfluxEvent>(event)
        
        val sessionsToSend = mutableSetOf<DefaultWebSocketServerSession>()
        
        // 1. Send to all channel subscribers
        sessionsToSend.addAll(connectionManager.getConnectionsForChannel(channelId))
        
        // 2. Send to all server subscribers who have VIEW_CHANNEL permission (queried concurrently)
        val serverSubscribers = connectionManager.getServerSubscribers(serverId)
        if (serverSubscribers.isNotEmpty()) {
            coroutineScope {
                serverSubscribers.map { userId ->
                    async {
                        val perms = channelPermissionService.getEffectivePermissions(serverId, channelId, userId)
                        userId to ConfluxPermission.hasPermission(perms, ConfluxPermission.VIEW_CHANNEL)
                    }
                }.forEach { deferred ->
                    val (userId, hasPerm) = deferred.await()
                    if (hasPerm) {
                        sessionsToSend.addAll(connectionManager.getUserSessions(userId))
                    }
                }
            }
        }
        
        coroutineScope {
            sessionsToSend.forEach { session ->
                launch {
                    try {
                        session.send(Frame.Text(eventJson))
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        // Session might be closed
                    }
                }
            }
        }
    }
}
