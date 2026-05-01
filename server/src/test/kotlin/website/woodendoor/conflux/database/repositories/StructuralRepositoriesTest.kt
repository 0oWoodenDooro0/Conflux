package website.woodendoor.conflux.database.repositories

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import website.woodendoor.conflux.database.DatabaseFactory
import website.woodendoor.conflux.database.models.*
import website.woodendoor.conflux.models.*
import kotlin.test.*

class StructuralRepositoriesTest {
    private val serverRepository = ExposedServerRepository()
    private val channelRepository = ExposedChannelRepository()
    private val userRepository = ExposedUserRepository()

    @BeforeTest
    fun setup() {
        DatabaseFactory.init()
        transaction {
            SchemaUtils.drop(ServerMembers, Channels, Roles, Servers, Users)
            SchemaUtils.create(Users, Servers, Roles, Channels, ServerMembers)
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

        val role = Role("r1", "Admin", 1L, 0xFF0000)
        serverRepository.createRole("s1", role)
        
        val roles = serverRepository.getRoles("s1")
        assertEquals(1, roles.size)
        assertEquals(role, roles[0])
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
}
