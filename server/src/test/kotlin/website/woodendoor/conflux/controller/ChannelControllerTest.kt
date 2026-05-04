package website.woodendoor.conflux.controller

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import website.woodendoor.conflux.database.repositories.ChannelRepository
import website.woodendoor.conflux.models.Channel
import website.woodendoor.conflux.models.CreateChannelRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChannelControllerTest {
    private val channelRepository = mockk<ChannelRepository>()
    private val controller = ChannelController(channelRepository)

    @Test
    fun `test createChannel success`() = runBlocking {
        val request = CreateChannelRequest("general")
        val channel = Channel("channel-1", "server-1", "general", website.woodendoor.conflux.models.ChannelType.TEXT)
        
        coEvery { channelRepository.createChannel(any()) } returns channel
        
        val result = controller.createChannel("server-1", request)
        
        assertTrue(result is OperationResult.Success)
        assertEquals(channel, result.data)
    }

    @Test
    fun `test getChannelsByServer`() = runBlocking {
        val channels = listOf(
            Channel("channel-1", "server-1", "general", website.woodendoor.conflux.models.ChannelType.TEXT)
        )
        
        coEvery { channelRepository.getChannelsByServer("server-1") } returns channels
        
        val result = controller.getChannelsByServer("server-1")
        
        assertTrue(result is OperationResult.Success)
        assertEquals(channels, result.data)
    }
}
