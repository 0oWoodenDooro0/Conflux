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
    private val channelPermissionService = website.woodendoor.conflux.service.impl.ChannelPermissionServiceImpl(channelRepository, serverRepository)
    private val channelService = website.woodendoor.conflux.service.impl.ChannelServiceImpl(channelRepository, channelPermissionService)
    private val serverService = website.woodendoor.conflux.service.impl.ServerServiceImpl(serverRepository, mockk(), channelService)
    private val controller = ChannelController(channelService, channelPermissionService, serverService, connectionManager)

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
        val everyoneRole = website.woodendoor.conflux.models.Role("role-1", "server-1", "@everyone", 0L, null, website.woodendoor.conflux.DEFAULT_ROLE_PRIORITY_EVERYONE)
        
        coEvery { serverRepository.getServer("server-1") } returns Server("server-1", "Test", "owner")
        coEvery { channelRepository.createChannel(any()) } returns channel
        coEvery { serverRepository.getRoles("server-1") } returns listOf(everyoneRole)
        coEvery { channelRepository.upsertOverride(any(), any()) } returns true
        
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
    fun `test getChannelsByServer with userId having view permission`() = runBlocking {
        val channels = listOf(
            Channel("channel-1", "server-1", "general", website.woodendoor.conflux.models.ChannelType.TEXT)
        )
        
        coEvery { serverRepository.getServer("server-1") } returns Server("server-1", "Test", "owner")
        coEvery { channelRepository.getChannelsByServer("server-1") } returns channels
        coEvery { channelRepository.getEffectivePermissions("server-1", "channel-1", "user-1") } returns website.woodendoor.conflux.models.ConfluxPermission.VIEW_CHANNEL
        
        val result = controller.getChannelsByServer("server-1", "user-1")
        
        assertTrue(result is OperationResult.Success)
        assertEquals(channels, (result as OperationResult.Success<List<Channel>>).data)
    }

    @Test
    fun `test getChannelsByServer sets canManage to true when user has channel management permission`() = runBlocking {
        val channels = listOf(
            Channel("channel-1", "server-1", "general", website.woodendoor.conflux.models.ChannelType.TEXT)
        )
        
        coEvery { serverRepository.getServer("server-1") } returns Server("server-1", "Test", "owner")
        coEvery { channelRepository.getChannelsByServer("server-1") } returns channels
        coEvery { channelRepository.getEffectivePermissions("server-1", "channel-1", "user-1") } returns (website.woodendoor.conflux.models.ConfluxPermission.VIEW_CHANNEL or website.woodendoor.conflux.models.ConfluxPermission.CHANNEL_MANAGEMENT)
        
        val result = controller.getChannelsByServer("server-1", "user-1")
        
        assertTrue(result is OperationResult.Success)
        val returnedChannels = (result as OperationResult.Success<List<Channel>>).data
        assertEquals(1, returnedChannels.size)
        assertTrue(returnedChannels[0].canManage)
    }

    @Test
    fun `test getChannelsByServer with userId lacking view permission`() = runBlocking {
        val channels = listOf(
            Channel("channel-1", "server-1", "general", website.woodendoor.conflux.models.ChannelType.TEXT),
            Channel("channel-2", "server-1", "secret", website.woodendoor.conflux.models.ChannelType.TEXT)
        )
        
        coEvery { serverRepository.getServer("server-1") } returns Server("server-1", "Test", "owner")
        coEvery { channelRepository.getChannelsByServer("server-1") } returns channels
        coEvery { channelRepository.getEffectivePermissions("server-1", "channel-1", "user-1") } returns website.woodendoor.conflux.models.ConfluxPermission.VIEW_CHANNEL
        coEvery { channelRepository.getEffectivePermissions("server-1", "channel-2", "user-1") } returns website.woodendoor.conflux.models.ConfluxPermission.NONE
        
        val result = controller.getChannelsByServer("server-1", "user-1")
        
        assertTrue(result is OperationResult.Success)
        val returnedChannels = (result as OperationResult.Success<List<Channel>>).data
        assertEquals(1, returnedChannels.size)
        assertEquals("channel-1", returnedChannels[0].id)
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

    @Test
    fun `test deleteOverride success`() = runBlocking {
        val serverId = "server-1"
        val overrideId = "override-1"
        val everyoneRole = website.woodendoor.conflux.models.Role("role-everyone", "server-1", "@everyone", 0L, null, website.woodendoor.conflux.DEFAULT_ROLE_PRIORITY_EVERYONE)
        val channels = listOf(
            Channel("channel-1", "server-1", "general", website.woodendoor.conflux.models.ChannelType.TEXT)
        )
        val overrides = listOf(
            website.woodendoor.conflux.models.ChannelPermissionOverride(
                id = overrideId,
                channelId = "channel-1",
                targetId = "some-other-role",
                targetType = website.woodendoor.conflux.models.OverrideType.ROLE,
                allow = 0L,
                deny = 0L
            )
        )

        coEvery { serverRepository.getRoles(serverId) } returns listOf(everyoneRole)
        coEvery { channelRepository.getChannelsByServer(serverId) } returns channels
        coEvery { channelRepository.getOverrides("channel-1") } returns overrides
        coEvery { channelRepository.deleteOverride(overrideId) } returns true

        val result = controller.deleteOverride(serverId, overrideId)

        assertTrue(result is OperationResult.Success)
    }

    @Test
    fun `test deleteOverride cannot delete everyone override`() = runBlocking {
        val serverId = "server-1"
        val overrideId = "override-everyone"
        val everyoneRole = website.woodendoor.conflux.models.Role("role-everyone", "server-1", "@everyone", 0L, null, website.woodendoor.conflux.DEFAULT_ROLE_PRIORITY_EVERYONE)
        val channels = listOf(
            Channel("channel-1", "server-1", "general", website.woodendoor.conflux.models.ChannelType.TEXT)
        )
        val overrides = listOf(
            website.woodendoor.conflux.models.ChannelPermissionOverride(
                id = overrideId,
                channelId = "channel-1",
                targetId = "role-everyone",
                targetType = website.woodendoor.conflux.models.OverrideType.ROLE,
                allow = 0L,
                deny = 0L
            )
        )

        coEvery { serverRepository.getRoles(serverId) } returns listOf(everyoneRole)
        coEvery { channelRepository.getChannelsByServer(serverId) } returns channels
        coEvery { channelRepository.getOverrides("channel-1") } returns overrides

        val result = controller.deleteOverride(serverId, overrideId)

        assertTrue(result is OperationResult.Failure.BadRequest)
        assertEquals("Cannot delete the @everyone override", result.message)
    }
}
