package website.woodendoor.conflux.controller

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import website.woodendoor.conflux.database.repositories.ChannelRepository
import website.woodendoor.conflux.database.repositories.ServerRepository
import website.woodendoor.conflux.models.Channel
import website.woodendoor.conflux.models.CreateChannelRequest
import website.woodendoor.conflux.models.UpdateChannelRequest
import website.woodendoor.conflux.models.Server
import website.woodendoor.conflux.WebSocketConnectionManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChannelControllerTest {
    private val channelRepository = mockk<ChannelRepository>()
    private val serverRepository = mockk<ServerRepository>()
    private val connectionManager = mockk<WebSocketConnectionManager>(relaxed = true)
    private val controller = ChannelController(channelRepository, serverRepository, connectionManager)

    @Test
    fun `test editChannel success`() = runBlocking {
        val channelId = "channel-1"
        val request = UpdateChannelRequest("renamed-general")
        val existingChannel = Channel(channelId, "server-1", "general", website.woodendoor.conflux.models.ChannelType.TEXT)
        val updatedChannel = existingChannel.copy(name = "renamed-general")
        
        coEvery { channelRepository.getChannel(channelId) } returns existingChannel
        coEvery { channelRepository.updateChannel(any()) } returns true
        
        val result = controller.editChannel(channelId, request)
        
        assertTrue(result is OperationResult.Success<*>)
        assertEquals(updatedChannel, (result as OperationResult.Success<Channel>).data)
    }

    @Test
    fun `test editChannel not found`() = runBlocking {
        val channelId = "channel-1"
        val request = UpdateChannelRequest("renamed-general")
        
        coEvery { channelRepository.getChannel(channelId) } returns null
        
        val result = controller.editChannel(channelId, request)
        
        assertTrue(result is OperationResult.Failure.NotFound)
    }

    @Test
    fun `test deleteChannel success`() = runBlocking {
        val channelId = "channel-1"
        val existingChannel = Channel(channelId, "server-1", "general", website.woodendoor.conflux.models.ChannelType.TEXT)
        
        coEvery { channelRepository.getChannel(channelId) } returns existingChannel
        coEvery { channelRepository.deleteChannel(channelId) } returns true
        
        val result = controller.deleteChannel(channelId)
        
        assertTrue(result is OperationResult.Success)
        assertEquals(Unit, result.data)
    }

    @Test
    fun `test deleteChannel not found`() = runBlocking {
        val channelId = "channel-1"
        
        coEvery { channelRepository.getChannel(channelId) } returns null
        
        val result = controller.deleteChannel(channelId)
        
        assertTrue(result is OperationResult.Failure.NotFound)
    }

    @Test
    fun `test createChannel success`() = runBlocking {
        val request = CreateChannelRequest("general")
        val channel = Channel("channel-1", "server-1", "general", website.woodendoor.conflux.models.ChannelType.TEXT)
        
        coEvery { serverRepository.getServer("server-1") } returns Server("server-1", "Test", "owner")
        coEvery { channelRepository.createChannel(any()) } returns channel
        
        val result = controller.createChannel("server-1", request)
        
        assertTrue(result is OperationResult.Success)
        assertEquals(channel, result.data)
    }

    @Test
    fun `test createChannel server not found`() = runBlocking {
        val request = CreateChannelRequest("general")
        coEvery { serverRepository.getServer("server-1") } returns null
        
        val result = controller.createChannel("server-1", request)
        
        assertTrue(result is OperationResult.Failure.NotFound)
    }

    @Test
    fun `test getChannelsByServer`() = runBlocking {
        val channels = listOf(
            Channel("channel-1", "server-1", "general", website.woodendoor.conflux.models.ChannelType.TEXT)
        )
        
        coEvery { serverRepository.getServer("server-1") } returns Server("server-1", "Test", "owner")
        coEvery { channelRepository.getChannelsByServer("server-1") } returns channels
        
        val result = controller.getChannelsByServer("server-1")
        
        assertTrue(result is OperationResult.Success)
        assertEquals(channels, (result as OperationResult.Success<List<Channel>>).data)
    }

    @Test
    fun `test getChannel success`() = runBlocking {
        val channel = Channel("channel-1", "server-1", "general", website.woodendoor.conflux.models.ChannelType.TEXT)
        coEvery { channelRepository.getChannel("channel-1") } returns channel
        
        val result = controller.getChannel("channel-1")
        
        assertTrue(result is OperationResult.Success)
        assertEquals(channel, (result as OperationResult.Success<Channel>).data)
    }

    @Test
    fun `test getChannel not found`() = runBlocking {
        coEvery { channelRepository.getChannel("channel-1") } returns null
        
        val result = controller.getChannel("channel-1")
        
        assertTrue(result is OperationResult.Failure.NotFound)
    }
}
