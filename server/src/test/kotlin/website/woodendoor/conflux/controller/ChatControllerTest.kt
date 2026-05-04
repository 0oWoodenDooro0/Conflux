package website.woodendoor.conflux.controller

import io.mockk.*
import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking
import website.woodendoor.conflux.WebSocketConnectionManager
import website.woodendoor.conflux.database.repositories.ChannelRepository
import website.woodendoor.conflux.database.repositories.MessageRepository
import website.woodendoor.conflux.models.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChatControllerTest {
    private val messageRepository = mockk<MessageRepository>()
    private val channelRepository = mockk<ChannelRepository>()
    private val roleController = mockk<RoleController>()
    private val connectionManager = mockk<WebSocketConnectionManager>()
    private val controller = ChatController(messageRepository, channelRepository, roleController, connectionManager)

    @Test
    fun `test sendMessage success`() = runBlocking {
        val request = SendMessageRequest("user-1", "Hello")
        val channel = Channel("channel-1", "server-1", "general", ChannelType.TEXT)
        val message = Message("msg-1", "channel-1", "user-1", "Hello", 123456789L)
        
        coEvery { channelRepository.getChannel("channel-1") } returns channel
        coEvery { roleController.hasPermission("server-1", "user-1", ConfluxPermission.MESSAGING) } returns true
        coEvery { messageRepository.saveMessage("channel-1", "user-1", "Hello") } returns message
        every { connectionManager.getConnectionsForChannel("channel-1") } returns emptyList()
        
        val result = controller.sendMessage("channel-1", request)
        
        assertTrue(result is OperationResult.Success)
        assertEquals(message, result.data)
    }

    @Test
    fun `test sendMessage forbidden`() = runBlocking {
        val request = SendMessageRequest("user-1", "Hello")
        val channel = Channel("channel-1", "server-1", "general", ChannelType.TEXT)
        
        coEvery { channelRepository.getChannel("channel-1") } returns channel
        coEvery { roleController.hasPermission("server-1", "user-1", ConfluxPermission.MESSAGING) } returns false
        
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
        coEvery { channelRepository.getChannel("channel-1") } returns channel
        coEvery { roleController.hasPermission("server-1", "user-1", ConfluxPermission.MESSAGING) } returns true
        
        val result = controller.sendMessage("channel-1", request)
        assertTrue(result is OperationResult.Failure.BadRequest)
    }

    @Test
    fun `test sendMessage blank content`() = runBlocking {
        val request = SendMessageRequest("user-1", "   ")
        val channel = Channel("channel-1", "server-1", "general", ChannelType.TEXT)
        coEvery { channelRepository.getChannel("channel-1") } returns channel
        coEvery { roleController.hasPermission("server-1", "user-1", ConfluxPermission.MESSAGING) } returns true
        
        val result = controller.sendMessage("channel-1", request)
        assertTrue(result is OperationResult.Failure.BadRequest)
    }
}
