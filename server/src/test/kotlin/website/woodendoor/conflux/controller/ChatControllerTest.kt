package website.woodendoor.conflux.controller

import io.mockk.*
import io.ktor.websocket.*
import io.ktor.server.websocket.DefaultWebSocketServerSession
import kotlinx.coroutines.runBlocking
import website.woodendoor.conflux.WebSocketConnectionManager
import website.woodendoor.conflux.database.repositories.ChannelRepository
import website.woodendoor.conflux.database.repositories.MessageRepository
import website.woodendoor.conflux.database.repositories.ServerRepository
import website.woodendoor.conflux.models.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChatControllerTest {
    private val messageRepository = mockk<MessageRepository>()
    private val channelRepository = mockk<ChannelRepository>()
    private val serverRepository = mockk<ServerRepository>()
    private val roleController = mockk<RoleController>()
    private val connectionManager = mockk<WebSocketConnectionManager>()
    private val controller = ChatController(messageRepository, channelRepository, serverRepository, roleController, connectionManager)

    @Test
    fun `test sendMessage success`() = runBlocking {
        val request = SendMessageRequest("user-1", "Hello")
        val channel = Channel("channel-1", "server-1", "general", ChannelType.TEXT)
        val message = Message("msg-1", "channel-1", "user-1", "Hello", 123456789L)
        val members = listOf(User("user-1", "User 1", "0000"))
        
        coEvery { channelRepository.getChannel("channel-1") } returns channel
        coEvery { serverRepository.getMembers("server-1") } returns members
        coEvery { channelRepository.getEffectivePermissions("server-1", "channel-1", "user-1") } returns ConfluxPermission.MESSAGING
        coEvery { messageRepository.saveMessage("channel-1", "user-1", "Hello") } returns message
        every { connectionManager.getConnectionsForChannel("channel-1") } returns emptyList()
        every { connectionManager.getServerSubscribers("server-1") } returns emptySet()
        
        val result = controller.sendMessage("channel-1", request)
        
        assertTrue(result is OperationResult.Success)
        assertEquals(message, result.data)
    }

    @Test
    fun `test sendMessage forbidden by membership`() = runBlocking {
        val request = SendMessageRequest("user-1", "Hello")
        val channel = Channel("channel-1", "server-1", "general", ChannelType.TEXT)
        
        coEvery { channelRepository.getChannel("channel-1") } returns channel
        coEvery { serverRepository.getMembers("server-1") } returns emptyList()
        
        val result = controller.sendMessage("channel-1", request)
        
        assertTrue(result is OperationResult.Failure.Forbidden)
    }

    @Test
    fun `test sendMessage forbidden by permission`() = runBlocking {
        val request = SendMessageRequest("user-1", "Hello")
        val channel = Channel("channel-1", "server-1", "general", ChannelType.TEXT)
        val members = listOf(User("user-1", "User 1", "0000"))
        
        coEvery { channelRepository.getChannel("channel-1") } returns channel
        coEvery { serverRepository.getMembers("server-1") } returns members
        coEvery { channelRepository.getEffectivePermissions("server-1", "channel-1", "user-1") } returns ConfluxPermission.NONE
        
        val result = controller.sendMessage("channel-1", request)
        
        assertTrue(result is OperationResult.Failure.Forbidden)
    }

    @Test
    fun `test getMessagesByChannel`() = runBlocking {
        val messages = listOf(Message("msg-1", "channel-1", "user-1", "Hello", 123456789L))
        coEvery { messageRepository.getMessagesByChannel("channel-1") } returns messages
        
        val result = controller.getMessagesByChannel("channel-1")
        
        assertTrue(result is OperationResult.Success)
        assertEquals(messages, (result as OperationResult.Success<List<Message>>).data)
    }

    @Test
    fun `test sendMessage channel not found`() = runBlocking {
        val request = SendMessageRequest("user-1", "Hello")
        coEvery { channelRepository.getChannel("channel-1") } returns null
        
        val result = controller.sendMessage("channel-1", request)
        assertTrue(result is OperationResult.Failure.NotFound)
    }

    @Test
    fun `test sendMessage too long`() = runBlocking {
        val request = SendMessageRequest("user-1", "a".repeat(2001))
        val channel = Channel("channel-1", "server-1", "general", ChannelType.TEXT)
        val members = listOf(User("user-1", "User 1", "0000"))
        coEvery { channelRepository.getChannel("channel-1") } returns channel
        coEvery { serverRepository.getMembers("server-1") } returns members
        coEvery { channelRepository.getEffectivePermissions("server-1", "channel-1", "user-1") } returns ConfluxPermission.MESSAGING
        
        val result = controller.sendMessage("channel-1", request)
        assertTrue(result is OperationResult.Failure.BadRequest)
    }

    @Test
    fun `test sendMessage blank content`() = runBlocking {
        val request = SendMessageRequest("user-1", "   ")
        val channel = Channel("channel-1", "server-1", "general", ChannelType.TEXT)
        val members = listOf(User("user-1", "User 1", "0000"))
        coEvery { channelRepository.getChannel("channel-1") } returns channel
        coEvery { serverRepository.getMembers("server-1") } returns members
        coEvery { channelRepository.getEffectivePermissions("server-1", "channel-1", "user-1") } returns ConfluxPermission.MESSAGING
        
        val result = controller.sendMessage("channel-1", request)
        assertTrue(result is OperationResult.Failure.BadRequest)
    }

    @Test
    fun `test sendMessage broadcasts to channel and server subscribers based on permissions`() = runBlocking {
        val request = SendMessageRequest("user-1", "Hello")
        val channel = Channel("channel-1", "server-1", "general", ChannelType.TEXT)
        val message = Message("msg-1", "channel-1", "user-1", "Hello", 123456789L)
        val members = listOf(
            User("user-1", "User 1", "0000"),
            User("user-2", "User 2", "0000"),
            User("user-3", "User 3", "0000"),
            User("user-4", "User 4", "0000")
        )
        
        coEvery { channelRepository.getChannel("channel-1") } returns channel
        coEvery { serverRepository.getMembers("server-1") } returns members
        coEvery { channelRepository.getEffectivePermissions("server-1", "channel-1", "user-1") } returns ConfluxPermission.MESSAGING
        coEvery { messageRepository.saveMessage("channel-1", "user-1", "Hello") } returns message

        val session2 = mockk<DefaultWebSocketServerSession>(relaxed = true)
        val session3 = mockk<DefaultWebSocketServerSession>(relaxed = true)
        val session4 = mockk<DefaultWebSocketServerSession>(relaxed = true)

        // channel subscribers (User 2)
        every { connectionManager.getConnectionsForChannel("channel-1") } returns listOf(session2)

        // server subscribers (User 3 and User 4)
        every { connectionManager.getServerSubscribers("server-1") } returns setOf("user-3", "user-4")

        // getUserSessions
        every { connectionManager.getUserSessions("user-3") } returns setOf(session3)
        every { connectionManager.getUserSessions("user-4") } returns setOf(session4)

        // permissions for server subscribers
        coEvery { channelRepository.getEffectivePermissions("server-1", "channel-1", "user-3") } returns ConfluxPermission.VIEW_CHANNEL
        coEvery { channelRepository.getEffectivePermissions("server-1", "channel-1", "user-4") } returns ConfluxPermission.NONE

        val result = controller.sendMessage("channel-1", request)

        assertTrue(result is OperationResult.Success)
        assertEquals(message, result.data)
        
        // Verify session2 and session3 received the message
        coVerify(exactly = 1) { session2.send(any<Frame.Text>()) }
        coVerify(exactly = 1) { session3.send(any<Frame.Text>()) }
        // Verify session4 did not receive it
        coVerify(exactly = 0) { session4.send(any<Frame.Text>()) }
    }
}
