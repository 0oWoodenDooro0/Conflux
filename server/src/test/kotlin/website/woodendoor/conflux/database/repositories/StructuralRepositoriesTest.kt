package website.woodendoor.conflux.database.repositories

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import website.woodendoor.conflux.database.DatabaseFactory
import website.woodendoor.conflux.database.models.*
import website.woodendoor.conflux.models.*
import kotlin.test.*

class StructuralRepositoriesTest {
    private val userRepository = ExposedUserRepository()
    private val serverRepository = ExposedServerRepository(userRepository)
    private val channelRepository = ExposedChannelRepository()

    @BeforeTest
    fun setup() {
        DatabaseFactory.init()
        transaction {
            SchemaUtils.drop(Friendships, DirectMessageMembers, DirectMessageChannels, Messages, ChannelPermissionOverrides, MemberRoles, ServerMembers, Channels, Roles, Servers, Users)
            SchemaUtils.create(Users, Servers, Roles, Channels, ServerMembers, MemberRoles, ChannelPermissionOverrides, Messages, Friendships, DirectMessageChannels, DirectMessageMembers)
        }
    }

    @Test
    fun testServerOperations() = runBlocking {
        val owner = User("owner", "owner", "0001")
        userRepository.createUser(owner)

        val server = Server("s1", "Test Server", owner.id)
        serverRepository.createServer(server)
        
        val fetched = serverRepository.getServer("s1")
        assertNotNull(fetched)
        assertEquals(server.name, fetched.name)
        assertEquals(server.ownerId, fetched.ownerId)
    }

    @Test
    fun testRoleOperations() = runBlocking {
        val owner = User("owner", "owner", "0001")
        userRepository.createUser(owner)
        val server = Server("s1", "Test Server", owner.id)
        serverRepository.createServer(server)

        val role = Role("r1", "s1", "Admin", 1L, 0xFF0000)
        serverRepository.createRole("s1", role)
        
        val roles = serverRepository.getRoles("s1")
        assertEquals(2, roles.size) // Admin and @everyone
        val adminRole = roles.find { it.name == "Admin" }
        assertNotNull(adminRole)
        assertEquals(role.name, adminRole.name)
        assertEquals(role.permissions, adminRole.permissions)
    }

    @Test
    fun testChannelOperations() = runBlocking {
        val owner = User("owner", "owner", "0001")
        userRepository.createUser(owner)
        val server = Server("s1", "Test Server", owner.id)
        serverRepository.createServer(server)

        val channel = Channel("c1", "s1", "general", ChannelType.TEXT)
        channelRepository.createChannel(channel)
        
        val fetched = channelRepository.getChannel("c1")
        assertNotNull(fetched)
        assertEquals(channel.name, fetched.name)
        
        val serverChannels = channelRepository.getChannelsByServer("s1")
        assertEquals(1, serverChannels.size)
    }

    @Test
    fun testMemberOperations() = runBlocking {
        val owner = User("owner", "owner", "0001")
        val member = User("member", "member", "0002")
        userRepository.createUser(owner)
        userRepository.createUser(member)

        val server = Server("s1", "Test Server", owner.id)
        serverRepository.createServer(server)
        
        serverRepository.addMember("s1", member.id)
        val members = serverRepository.getMembers("s1")
        assertTrue(members.any { it.id == member.id })
        
        serverRepository.removeMember("s1", member.id)
        val membersAfter = serverRepository.getMembers("s1")
        assertFalse(membersAfter.any { it.id == member.id })
    }

    @Test
    fun testGetServersForUser() = runBlocking {
        val user = User("u1", "user1", "0001")
        val other = User("other", "other", "0002")
        userRepository.createUser(user)
        userRepository.createUser(other)

        val ownedServer = Server("s1", "Owned", user.id)
        serverRepository.createServer(ownedServer)

        val memberServer = Server("s2", "Member", other.id)
        serverRepository.createServer(memberServer)
        serverRepository.addMember("s2", user.id)

        val servers = serverRepository.getServersForUser(user.id)
        assertEquals(2, servers.size)
        assertTrue(servers.any { it.id == "s1" })
        assertTrue(servers.any { it.id == "s2" })
    }

    @Test
    fun testChannelOverrideDeletionProtection() = runBlocking {
        val owner = User("owner", "owner", "0001")
        userRepository.createUser(owner)
        val server = Server("s1", "Test Server", owner.id)
        serverRepository.createServer(server)
        
        val roles = serverRepository.getRoles("s1")
        val everyoneRole = roles.find { it.priorityLevel == website.woodendoor.conflux.DEFAULT_ROLE_PRIORITY_EVERYONE }
        assertNotNull(everyoneRole)

        val channel = Channel("c1", "s1", "general", ChannelType.TEXT)
        channelRepository.createChannel(channel)

        // Create everyone override
        val everyoneOverride = ChannelPermissionOverride("o1", "c1", everyoneRole.id, OverrideType.ROLE, 0L, 0L)
        channelRepository.upsertOverride("c1", everyoneOverride)

        // Create other override
        val otherOverride = ChannelPermissionOverride("o2", "c1", "other-role-id", OverrideType.ROLE, 0L, 0L)
        channelRepository.upsertOverride("c1", otherOverride)

        // Try deleting other override - should succeed
        val deleteOtherResult = channelRepository.deleteOverride("o2")
        assertTrue(deleteOtherResult)

        // Try deleting everyone override - should fail
        val deleteEveryoneResult = channelRepository.deleteOverride("o1")
        assertFalse(deleteEveryoneResult)
        
        // Verify everyone override is still there
        val overrides = channelRepository.getOverrides("c1")
        assertTrue(overrides.any { it.id == "o1" })
    }
}
