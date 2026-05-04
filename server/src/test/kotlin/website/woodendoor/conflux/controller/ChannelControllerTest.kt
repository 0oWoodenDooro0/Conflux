package website.woodendoor.conflux.controller

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import website.woodendoor.conflux.database.repositories.ChannelRepository
import website.woodendoor.conflux.database.repositories.ServerRepository
import website.woodendoor.conflux.models.Channel
import website.woodendoor.conflux.models.CreateChannelRequest
import website.woodendoor.conflux.models.Server
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChannelControllerTest {
    private val channelRepository = mockk<ChannelRepository>()
    private val serverRepository = mockk<ServerRepository>()
    private val controller = ChannelController(channelRepository, serverRepository)

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
