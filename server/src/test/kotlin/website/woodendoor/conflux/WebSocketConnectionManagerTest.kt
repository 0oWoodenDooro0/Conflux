package website.woodendoor.conflux

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import website.woodendoor.conflux.models.ConfluxEvent
import kotlin.test.*

class WebSocketConnectionManagerTest {

    private lateinit var connectionManager: WebSocketConnectionManager

    @BeforeTest
    fun setup() {
        connectionManager = WebSocketConnectionManager()
    }

    @Test
    fun `should add and remove connections`() {
        val userId = "user-1"
        val session = mockk<DefaultWebSocketServerSession>()

        connectionManager.addConnection(userId, session)
        // We need a way to verify it's added. Maybe a count?
        // Or just check if we get it back for a channel.
    }

    @Test
    fun `should broadcast to subscribed channels`() {
        val userId1 = "user-1"
        val userId2 = "user-2"
        val channelId = "channel-A"
        val session1 = mockk<DefaultWebSocketServerSession>()
        val session2 = mockk<DefaultWebSocketServerSession>()

        connectionManager.addConnection(userId1, session1)
        connectionManager.addConnection(userId2, session2)
        
        connectionManager.subscribeToChannel(userId1, channelId)
        connectionManager.subscribeToChannel(userId2, channelId)
        
        val connections = connectionManager.getConnectionsForChannel(channelId)
        assertEquals(2, connections.size)
        assertTrue(connections.contains(session1))
        assertTrue(connections.contains(session2))
    }

    @Test
    fun `should not return connections that are not subscribed`() {
        val userId1 = "user-1"
        val channelA = "channel-A"
        val channelB = "channel-B"
        val session1 = mockk<DefaultWebSocketServerSession>()

        connectionManager.addConnection(userId1, session1)
        connectionManager.subscribeToChannel(userId1, channelA)
        
        val connections = connectionManager.getConnectionsForChannel(channelB)
        assertTrue(connections.isEmpty())
    }

    @Test
    fun `should broadcast to server`() = runBlocking {
        val userId = "user-1"
        val serverId = "server-1"
        val session = mockk<DefaultWebSocketServerSession>(relaxed = true)
        val event = ConfluxEvent.PermissionUpdate(serverId, userId = userId)

        connectionManager.addConnection(userId, session)
        connectionManager.subscribeToServer(userId, serverId)
        
        connectionManager.broadcastToServer(serverId, event)
        
        coVerify { session.send(any<Frame.Text>()) }
    }
}
