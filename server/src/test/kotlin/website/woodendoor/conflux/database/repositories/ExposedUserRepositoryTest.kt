package website.woodendoor.conflux.database.repositories

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import website.woodendoor.conflux.database.DatabaseFactory
import website.woodendoor.conflux.database.models.*
import website.woodendoor.conflux.models.User
import kotlin.test.*

class ExposedUserRepositoryTest {
    private val repository = ExposedUserRepository()

    @BeforeTest
    fun setup() {
        DatabaseFactory.init()
        transaction {
            SchemaUtils.drop(Messages, ChannelPermissionOverrides, MemberRoles, ServerMembers, Channels, Roles, Servers, Users)
            SchemaUtils.create(Users, Servers, Roles, Channels, ServerMembers, MemberRoles, ChannelPermissionOverrides, Messages)
        }
    }

    @Test
    fun testCreateAndGetUser() = runBlocking {
        val user = User("1", "testuser", "1234", "avatar_url")
        repository.createUser(user)
        val fetched = repository.getUser("1")
        assertEquals(user, fetched)
    }

    @Test
    fun testUpdateUser() = runBlocking {
        val user = User("2", "oldname", "1111")
        repository.createUser(user)
        
        val updatedUser = user.copy(username = "newname")
        val success = repository.updateUser(updatedUser)
        assertTrue(success)
        
        val fetched = repository.getUser("2")
        assertEquals("newname", fetched?.username)
    }

    @Test
    fun testFindByUsername() = runBlocking {
        val user = User("4", "findme", "2222")
        repository.createUser(user)
        
        val found = repository.findByUsername("findme")
        assertEquals(user, found)
    }

    @Test
    fun testFindByUsernameNotFound() = runBlocking {
        val found = repository.findByUsername("nonexistent")
        assertNull(found)
    }
}
