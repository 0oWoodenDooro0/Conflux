package website.woodendoor.conflux.database.repositories

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import website.woodendoor.conflux.database.DatabaseFactory
import website.woodendoor.conflux.database.models.*
import website.woodendoor.conflux.models.*
import kotlin.test.*
import java.util.UUID

import website.woodendoor.conflux.DEFAULT_ROLE_NAME_EVERYONE
import website.woodendoor.conflux.DEFAULT_ROLE_PRIORITY_EVERYONE

class ExposedServerRepositoryTest {
    private val userRepository = ExposedUserRepository()
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
    fun `createServer should only create @everyone role`() = runBlocking {
        val owner = User("owner", "owner", "0001")
        userRepository.createUser(owner)
        val server = Server("test-id", "Test Server", owner.id)
        serverRepository.createServer(server)
        
        val roles = serverRepository.getRoles("test-id")
        assertEquals(1, roles.size)
        assertEquals(DEFAULT_ROLE_NAME_EVERYONE, roles[0].name)
        assertEquals(DEFAULT_ROLE_PRIORITY_EVERYONE, roles[0].priorityLevel)
    }

    @Test
    fun testGetRolesSorting() = runBlocking {
        val owner = User("owner", "owner", "0001")
        userRepository.createUser(owner)
        val serverId = "s1"
        val server = Server(serverId, "Test Server", owner.id)
        serverRepository.createServer(server)

        // createServer adds @everyone (-1) role

        // Add a role with priority 50
        val roleMid = Role(UUID.randomUUID().toString(), serverId, "Middle", 0, null, 50)
        serverRepository.createRole(serverId, roleMid)

        // Add a role with priority 150
        val roleHigh = Role(UUID.randomUUID().toString(), serverId, "Highest", 0, null, 150)
        serverRepository.createRole(serverId, roleHigh)

        val roles = serverRepository.getRoles(serverId)
        
        // Expected order: 150, 50, -1
        assertEquals(3, roles.size)
        assertEquals(150, roles[0].priorityLevel)
        assertEquals(50, roles[1].priorityLevel)
        assertEquals(-1, roles[2].priorityLevel)
    }

    @Test
    fun testGetRolesForMemberSorting() = runBlocking {
        val owner = User("owner", "owner", "0001")
        userRepository.createUser(owner)
        val serverId = "s1"
        val server = Server(serverId, "Test Server", owner.id)
        serverRepository.createServer(server)

        // owner has no roles assigned explicitly, but createServer doesn't assign any anymore.
        // getRolesForMember currently only returns assigned roles. 
        // Note: Task 5 will update getRolesForMember to include @everyone.
        // For now, let's just check assigned roles.

        // Add a role with priority 50 and assign to owner
        val roleMid = Role(UUID.randomUUID().toString(), serverId, "Middle", 0, null, 50)
        serverRepository.createRole(serverId, roleMid)
        serverRepository.assignRoleToMember(serverId, owner.id, roleMid.id)

        // Add a role with priority 150 and assign to owner
        val roleHigh = Role(UUID.randomUUID().toString(), serverId, "Highest", 0, null, 150)
        serverRepository.createRole(serverId, roleHigh)
        serverRepository.assignRoleToMember(serverId, owner.id, roleHigh.id)

        val roles = serverRepository.getRolesForMember(serverId, owner.id)
        
        // Expected order: 150, 50, -1 (@everyone)
        assertEquals(3, roles.size)
        assertEquals(150, roles[0].priorityLevel)
        assertEquals(50, roles[1].priorityLevel)
        assertEquals(-1, roles[2].priorityLevel)
        assertEquals(DEFAULT_ROLE_NAME_EVERYONE, roles[2].name)
    }
}
