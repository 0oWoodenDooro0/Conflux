package website.woodendoor.conflux.database.repositories

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import website.woodendoor.conflux.database.DatabaseFactory
import website.woodendoor.conflux.database.models.*
import website.woodendoor.conflux.models.Channel
import website.woodendoor.conflux.models.ChannelType
import website.woodendoor.conflux.models.User
import website.woodendoor.conflux.models.Server
import kotlin.test.*

class ExposedMessageRepositoryTest {
    private val repository = ExposedMessageRepository()
    private val userRepository = ExposedUserRepository()
    private val channelRepository = ExposedChannelRepository()
    private val serverRepository = ExposedServerRepository(userRepository)

    @BeforeTest
    fun setup() {
        DatabaseFactory.init()
        transaction {
            SchemaUtils.drop(Friendships, DirectMessageMembers, DirectMessageChannels, Messages, ChannelPermissionOverrides, MemberRoles, ServerMembers, Channels, Roles, Servers, Users)
            SchemaUtils.create(Users, Servers, Roles, Channels, ServerMembers, MemberRoles, ChannelPermissionOverrides, Messages, Friendships, DirectMessageChannels, DirectMessageMembers)
        }
    }

    @Test
    fun testSaveAndGetMessages() = runBlocking {
        val user = User("u1", "test", "0001")
        userRepository.createUser(user)
        
        val server = Server("s1", "server", "u1")
        serverRepository.createServer(server)
        
        val channel = Channel("c1", "s1", "general", ChannelType.TEXT)
        channelRepository.createChannel(channel)

        val message = repository.saveMessage("c1", "u1", "Hello World")
        assertNotNull(message)
        assertEquals("c1", message.channelId)
        assertEquals("u1", message.authorId)
        assertEquals("Hello World", message.content)

        val messages = repository.getMessagesByChannel("c1")
        assertEquals(1, messages.size)
        assertEquals(message.id, messages[0].id)
        assertEquals("Hello World", messages[0].content)
    }

    @Test
    fun testMessagesOrder() = runBlocking {
        val user = User("u1", "test", "0001")
        userRepository.createUser(user)
        val server = Server("s1", "server", "u1")
        serverRepository.createServer(server)
        val channel = Channel("c1", "s1", "general", ChannelType.TEXT)
        channelRepository.createChannel(channel)

        repository.saveMessage("c1", "u1", "First")
        Thread.sleep(10) // Ensure different timestamps
        repository.saveMessage("c1", "u1", "Second")

        val messages = repository.getMessagesByChannel("c1")
        assertEquals(2, messages.size)
        assertEquals("First", messages[0].content)
        assertEquals("Second", messages[1].content)
    }
}
