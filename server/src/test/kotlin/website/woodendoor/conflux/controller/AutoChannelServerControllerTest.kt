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
    private val channelPermissionService = website.woodendoor.conflux.service.impl.ChannelPermissionServiceImpl(channelRepository, serverRepository)
    private val channelService = website.woodendoor.conflux.service.impl.ChannelServiceImpl(channelRepository, channelPermissionService)
    private val serverService = website.woodendoor.conflux.service.impl.ServerServiceImpl(serverRepository, userRepository, channelService)
    private val connectionManager = io.mockk.mockk<website.woodendoor.conflux.WebSocketConnectionManager>(relaxed = true)
    private val controller = ServerController(serverService, connectionManager, channelPermissionService)

    @Test
    fun `test createServer creates default general channel with everyone override`() = runBlocking {
        val request = CreateServerRequest("Test Server", ownerId = "owner-1")
        val server = Server("server-1", "Test Server", "owner-1")
        val generalChannel = Channel("chan-1", "server-1", "general", ChannelType.TEXT)
        val everyoneRole = website.woodendoor.conflux.models.Role(
            "role-everyone", "server-1", "@everyone", 0L, null, website.woodendoor.conflux.DEFAULT_ROLE_PRIORITY_EVERYONE
        )
        
        coEvery { userRepository.getUser("owner-1") } returns User("owner-1", "owner", "1234")
        coEvery { serverRepository.createServer(any()) } returns server
        coEvery { channelRepository.createChannel(any()) } returns generalChannel
        coEvery { serverRepository.getRoles("server-1") } returns listOf(everyoneRole)
        coEvery { channelRepository.upsertOverride(any(), any()) } returns true
        
        val result = controller.createServer(request)
        
        assertTrue(result is OperationResult.Success)
        coVerify { 
            channelRepository.createChannel(match { 
                it.serverId == "server-1" && it.name == "general" && it.type == ChannelType.TEXT 
            })
            serverRepository.getRoles("server-1")
            channelRepository.upsertOverride("chan-1", match {
                it.targetId == "role-everyone" && 
                it.targetType == website.woodendoor.conflux.models.OverrideType.ROLE && 
                it.allow == 0L && 
                it.deny == 0L
            })
        }
    }
}
