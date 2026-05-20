package website.woodendoor.conflux.database.repositories

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import website.woodendoor.conflux.database.DatabaseFactory
import website.woodendoor.conflux.database.models.*
import website.woodendoor.conflux.models.Server
import website.woodendoor.conflux.models.User
import website.woodendoor.conflux.models.Role
import website.woodendoor.conflux.models.ConfluxPermission
import kotlin.test.*

class PermissionQueryTest {
    private val userRepository = ExposedUserRepository()
    private val serverRepository = ExposedServerRepository(userRepository)

    @BeforeTest
    fun setup() {
        DatabaseFactory.init()
        transaction {
            SchemaUtils.drop(Messages, ChannelPermissionOverrides, MemberRoles, ServerMembers, Channels, Roles, Servers, Users)
            SchemaUtils.create(Users, Servers, Roles, Channels, ServerMembers, MemberRoles, ChannelPermissionOverrides, Messages)
        }
    }

    @Test
    fun testGetPermissionsForMember() = runBlocking {
        val owner = User("owner1", "owner", "0001")
        val member = User("member1", "member", "0002")
        userRepository.createUser(owner)
        userRepository.createUser(member)

        val server = Server("s1", "Test Server", owner.id)
        serverRepository.createServer(server)

        // Owner should have ALL permissions
        val ownerPerms = serverRepository.getPermissionsForMember(server.id, owner.id)
        assertEquals(ConfluxPermission.ALL, ownerPerms)

        // New member should have MESSAGING permissions by default due to @everyone role (Task 4)
        serverRepository.joinServer(member.id, server.id)
        val memberPerms = serverRepository.getPermissionsForMember(server.id, member.id)
        assertEquals(ConfluxPermission.MESSAGING, memberPerms)

        // Add another role with CHANNEL_MANAGEMENT
        val adminRole = serverRepository.createRole(server.id, Role("r1", server.id, "Admin", ConfluxPermission.CHANNEL_MANAGEMENT, null, 50))
        assertNotNull(adminRole)
        serverRepository.assignRoleToMember(server.id, member.id, adminRole.id)

        // Member should now have CHANNEL_MANAGEMENT OR MESSAGING
        val updatedPerms = serverRepository.getPermissionsForMember(server.id, member.id)
        assertEquals(ConfluxPermission.CHANNEL_MANAGEMENT or ConfluxPermission.MESSAGING, updatedPerms)
    }

    @Test
    fun testOwnerAlwaysHasAllPermissions() = runBlocking {
        val owner = User("owner1", "owner", "0001")
        userRepository.createUser(owner)

        val server = Server("s1", "Test Server", owner.id)
        serverRepository.createServer(server)

        // Remove all roles from owner
        val ownerRoles = serverRepository.getRolesForMember(server.id, owner.id)
        for (role in ownerRoles) {
            serverRepository.removeRoleFromMember(server.id, owner.id, role.id)
        }

        // Owner should still have ALL permissions
        val ownerPerms = serverRepository.getPermissionsForMember(server.id, owner.id)
        assertEquals(ConfluxPermission.ALL, ownerPerms)
    }
}
