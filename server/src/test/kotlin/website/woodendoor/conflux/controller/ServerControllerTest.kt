package website.woodendoor.conflux.controller

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import website.woodendoor.conflux.database.repositories.ChannelRepository
import website.woodendoor.conflux.database.repositories.ServerRepository
import website.woodendoor.conflux.database.repositories.UserRepository
import website.woodendoor.conflux.models.CreateServerRequest
import website.woodendoor.conflux.models.Server
import website.woodendoor.conflux.models.User
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServerControllerTest {
    private val serverRepository = mockk<ServerRepository>()
    private val userRepository = mockk<UserRepository>()
    private val channelRepository = mockk<ChannelRepository>()
    private val channelPermissionService = website.woodendoor.conflux.service.impl.ChannelPermissionServiceImpl(channelRepository, serverRepository)
    private val channelService = website.woodendoor.conflux.service.impl.ChannelServiceImpl(channelRepository, channelPermissionService)
    private val serverService = website.woodendoor.conflux.service.impl.ServerServiceImpl(serverRepository, userRepository, channelService)
    private val connectionManager = io.mockk.mockk<website.woodendoor.conflux.WebSocketConnectionManager>(relaxed = true)
    private val controller = ServerController(serverService, connectionManager, channelPermissionService)

    @Test
    fun `test createServer success`() = runBlocking {
        val request = CreateServerRequest("Test Server", ownerId = "owner-1")
        val server = Server("server-1", "Test Server", "owner-1")
        val generalChannel = website.woodendoor.conflux.models.Channel("chan-1", "server-1", "general", website.woodendoor.conflux.models.ChannelType.TEXT)
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
        assertEquals(server, (result as OperationResult.Success<Server>).data)
    }

    @Test
    fun `test getServer not found`() = runBlocking {
        coEvery { serverRepository.getServer("server-1") } returns null
        
        val result = controller.getServer("server-1")
        
        assertTrue(result is OperationResult.Failure.NotFound)
    }

    @Test
    fun `test getServersForUser`() = runBlocking {
        val servers = listOf(Server("server-1", "Test Server", "owner-1"))
        coEvery { serverRepository.getServersForUser("user-1") } returns servers
        
        val result = controller.getServersForUser("user-1")
        
        assertTrue(result is OperationResult.Success)
        assertEquals(servers, (result as OperationResult.Success<List<Server>>).data)
    }

    @Test
    fun `test getMembers`() = runBlocking {
        val members = listOf(User("user-1", "user", "1234"))
        coEvery { serverRepository.getMembers("server-1") } returns members
        
        val result = controller.getMembers("server-1")
        
        assertTrue(result is OperationResult.Success)
        assertEquals(members, (result as OperationResult.Success<List<User>>).data)
    }

    @Test
    fun `test getPermissionsForMember`() = runBlocking {
        coEvery { serverRepository.getPermissionsForMember("server-1", "user-1") } returns 0b11L
        
        val result = controller.getPermissionsForMember("server-1", "user-1")
        
        assertTrue(result is OperationResult.Success)
        assertEquals(0b11L, (result as OperationResult.Success<Long>).data)
    }

    @Test
    fun `test joinServer success`() = runBlocking {
        coEvery { serverRepository.getServer("server-1") } returns Server("server-1", "Test", "owner")
        coEvery { serverRepository.joinServer("user-1", "server-1") } returns true
        
        val result = controller.joinServer("user-1", "server-1")
        
        assertTrue(result is OperationResult.Success)
    }

    @Test
    fun `test joinServer already member`() = runBlocking {
        coEvery { serverRepository.getServer("server-1") } returns Server("server-1", "Test", "owner")
        coEvery { serverRepository.joinServer("user-1", "server-1") } returns false
        
        val result = controller.joinServer("user-1", "server-1")
        
        assertTrue(result is OperationResult.Failure.Conflict)
    }
}
