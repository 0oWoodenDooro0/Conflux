package website.woodendoor.conflux.database.repositories

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import website.woodendoor.conflux.database.DatabaseFactory
import website.woodendoor.conflux.database.models.*
import website.woodendoor.conflux.models.Server
import website.woodendoor.conflux.models.User
import kotlin.test.*

class OwnerRoleAssignmentTest {
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
    fun testOwnerRoleAssignedOnServerCreation() = runBlocking {
        val owner = User("owner1", "owner", "0001")
        userRepository.createUser(owner)

        val server = Server("s1", "Test Server", owner.id)
        serverRepository.createServer(server)

        val roles = serverRepository.getRoles(server.id)
        val ownerRole = roles.find { it.name == "Owner" }
        
        assertNotNull(ownerRole, "Owner role should be created")
        assertEquals(100, ownerRole.priorityLevel, "Owner role should have high priority")
        
        // Check if role is assigned to the member
        // For now I'll use a direct DB query since the repository doesn't have the method yet
        val isAssigned = transaction {
            MemberRoles.selectAll().where { 
                (MemberRoles.serverId eq server.id) and 
                (MemberRoles.userId eq owner.id) and 
                (MemberRoles.roleId eq ownerRole.id) 
            }.any()
        }
        assertTrue(isAssigned, "Owner role should be assigned to the creator")
    }
}
