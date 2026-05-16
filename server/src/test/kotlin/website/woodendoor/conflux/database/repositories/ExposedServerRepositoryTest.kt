package website.woodendoor.conflux.database.repositories

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import website.woodendoor.conflux.database.DatabaseFactory
import website.woodendoor.conflux.database.models.*
import website.woodendoor.conflux.models.*
import kotlin.test.*
import java.util.UUID

class ExposedServerRepositoryTest {
    private val userRepository = ExposedUserRepository()
    private val serverRepository = ExposedServerRepository(userRepository)

    @BeforeTest
    fun setup() {
        DatabaseFactory.init()
        transaction {
            SchemaUtils.drop(Messages, MemberRoles, ServerMembers, Channels, Roles, Servers, Users)
            SchemaUtils.create(Users, Servers, Roles, Channels, ServerMembers, MemberRoles, Messages)
        }
    }

    @Test
    fun testGetRolesSorting() = runBlocking {
        val owner = User("owner", "owner", "0001")
        userRepository.createUser(owner)
        val serverId = "s1"
        val server = Server(serverId, "Test Server", owner.id)
        serverRepository.createServer(server)

        // createServer adds Owner (100) and Member (0) roles

        // Add a role with priority 50
        val roleMid = Role(UUID.randomUUID().toString(), serverId, "Middle", 0, null, 50)
        serverRepository.createRole(serverId, roleMid)

        // Add a role with priority 150
        val roleHigh = Role(UUID.randomUUID().toString(), serverId, "Highest", 0, null, 150)
        serverRepository.createRole(serverId, roleHigh)

        val roles = serverRepository.getRoles(serverId)
        
        // Expected order: 150, 100, 50, 0
        assertEquals(4, roles.size)
        assertEquals(150, roles[0].priorityLevel)
        assertEquals(100, roles[1].priorityLevel)
        assertEquals(50, roles[2].priorityLevel)
        assertEquals(0, roles[3].priorityLevel)
    }

    @Test
    fun testGetRolesForMemberSorting() = runBlocking {
        val owner = User("owner", "owner", "0001")
        userRepository.createUser(owner)
        val serverId = "s1"
        val server = Server(serverId, "Test Server", owner.id)
        serverRepository.createServer(server)

        // owner already has Owner role (100)

        // Add a role with priority 50 and assign to owner
        val roleMid = Role(UUID.randomUUID().toString(), serverId, "Middle", 0, null, 50)
        serverRepository.createRole(serverId, roleMid)
        serverRepository.assignRoleToMember(serverId, owner.id, roleMid.id)

        // Add a role with priority 150 and assign to owner
        val roleHigh = Role(UUID.randomUUID().toString(), serverId, "Highest", 0, null, 150)
        serverRepository.createRole(serverId, roleHigh)
        serverRepository.assignRoleToMember(serverId, owner.id, roleHigh.id)

        val roles = serverRepository.getRolesForMember(serverId, owner.id)
        
        // Expected order: 150, 100, 50
        assertEquals(3, roles.size)
        assertEquals(150, roles[0].priorityLevel)
        assertEquals(100, roles[1].priorityLevel)
        assertEquals(50, roles[2].priorityLevel)
    }
}
