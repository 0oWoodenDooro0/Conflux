package website.woodendoor.conflux.database.repositories

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import website.woodendoor.conflux.database.DatabaseFactory
import website.woodendoor.conflux.database.models.*
import website.woodendoor.conflux.models.*
import kotlin.test.*

class JoinServerTest {
    private val userRepository = ExposedUserRepository()
    private val serverRepository = ExposedServerRepository(userRepository)

    @BeforeTest
    fun setup() {
        DatabaseFactory.init()
        transaction {
            SchemaUtils.drop(Messages, ServerMembers, Channels, Roles, Servers, Users)
            SchemaUtils.create(Users, Servers, Roles, Channels, ServerMembers, Messages)
        }
    }

    @Test
    fun testJoinServerSuccess() = runBlocking {
        val owner = User("owner", "owner", "0001")
        val user = User("user", "user", "0002")
        userRepository.createUser(owner)
        userRepository.createUser(user)

        val server = Server("s1", "Test Server", owner.id)
        serverRepository.createServer(server)

        val result = serverRepository.joinServer(user.id, server.id)
        assertTrue(result, "Should successfully join server")

        val members = serverRepository.getMembers(server.id)
        assertTrue(members.any { it.id == user.id }, "User should be in members list")
    }

    @Test
    fun testJoinServerNotFound() = runBlocking {
        val user = User("user", "user", "0001")
        userRepository.createUser(user)

        val result = serverRepository.joinServer(user.id, "nonexistent")
        assertFalse(result, "Should not join nonexistent server")
    }

    @Test
    fun testJoinServerAlreadyMember() = runBlocking {
        val owner = User("owner", "owner", "0001")
        val user = User("user", "user", "0002")
        userRepository.createUser(owner)
        userRepository.createUser(user)

        val server = Server("s1", "Test Server", owner.id)
        serverRepository.createServer(server)
        serverRepository.addMember(server.id, user.id)

        val result = serverRepository.joinServer(user.id, server.id)
        assertFalse(result, "Should not join if already a member")
    }

    @Test
    fun testJoinServerAsOwner() = runBlocking {
        val owner = User("owner", "owner", "0001")
        userRepository.createUser(owner)

        val server = Server("s1", "Test Server", owner.id)
        serverRepository.createServer(server)

        val result = serverRepository.joinServer(owner.id, server.id)
        assertFalse(result, "Owner should be considered already a member or not allowed to join explicitly")
    }
}
