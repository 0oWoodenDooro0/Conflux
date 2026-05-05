package website.woodendoor.conflux.controller

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import website.woodendoor.conflux.database.repositories.ChannelRepository
import website.woodendoor.conflux.database.repositories.ServerRepository
import website.woodendoor.conflux.database.repositories.UserRepository
import website.woodendoor.conflux.models.CreateServerRequest
import website.woodendoor.conflux.models.Server
import website.woodendoor.conflux.models.User
import website.woodendoor.conflux.models.Channel
import website.woodendoor.conflux.models.ChannelType
import kotlin.test.Test
import kotlin.test.assertTrue

class AutoChannelServerControllerTest {
    private val serverRepository = mockk<ServerRepository>()
    private val userRepository = mockk<UserRepository>()
    private val channelRepository = mockk<ChannelRepository>()
    // Note: This will fail to compile initially because ServerController doesn't take channelRepository yet
    private val controller = ServerController(serverRepository, userRepository, channelRepository)

    @Test
    fun `test createServer creates default general channel`() = runBlocking {
        val request = CreateServerRequest("Test Server", ownerId = "owner-1")
        val server = Server("server-1", "Test Server", "owner-1")
        
        coEvery { userRepository.getUser("owner-1") } returns User("owner-1", "owner", "1234")
        coEvery { serverRepository.createServer(any()) } returns server
        coEvery { channelRepository.createChannel(any()) } returns mockk<Channel>()
        
        val result = controller.createServer(request)
        
        assertTrue(result is OperationResult.Success)
        coVerify { 
            channelRepository.createChannel(match { 
                it.serverId == "server-1" && it.name == "general" && it.type == ChannelType.TEXT 
            }) 
        }
    }
}
