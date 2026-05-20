package website.woodendoor.conflux.database.repositories

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import website.woodendoor.conflux.database.DatabaseFactory
import website.woodendoor.conflux.database.models.*
import website.woodendoor.conflux.models.*
import kotlin.test.*
import java.util.UUID

class ChannelPermissionLogicTest {
    private val userRepository = ExposedUserRepository()
    private val serverRepository = ExposedServerRepository(userRepository)
    private val channelRepository = ExposedChannelRepository()

    @BeforeTest
    fun setup() {
        DatabaseFactory.init()
        transaction {
            SchemaUtils.drop(Messages, ChannelPermissionOverrides, MemberRoles, ServerMembers, Channels, Roles, Servers, Users)
            SchemaUtils.create(Users, Servers, Roles, Channels, ServerMembers, MemberRoles, ChannelPermissionOverrides, Messages)
        }
    }

    @Test
    fun testOwnerExemption() = runBlocking {
        val owner = User("owner1", "owner", "0001")
        userRepository.createUser(owner)
        val server = serverRepository.createServer(Server("s1", "Test Server", owner.id))!!
        val channel = channelRepository.createChannel(Channel("c1", server.id, "general", ChannelType.TEXT))!!

        // Deny everything in channel for everyone
        channelRepository.upsertOverride(channel.id, ChannelPermissionOverride(UUID.randomUUID().toString(), channel.id, "EVERYONE", OverrideType.ROLE, 0L, ConfluxPermission.ALL))

        val perms = channelRepository.getEffectivePermissions(server.id, channel.id, owner.id)
        assertEquals(ConfluxPermission.ALL, perms, "Owner should have ALL permissions regardless of overrides")
    }

    @Test
    fun testEveryoneOverride() = runBlocking {
        val owner = User("owner1", "owner", "0001")
        val member = User("member1", "member", "0002")
        userRepository.createUser(owner)
        userRepository.createUser(member)
        val server = serverRepository.createServer(Server("s1", "Test Server", owner.id))!!
        serverRepository.joinServer(member.id, server.id)
        val channel = channelRepository.createChannel(Channel("c1", server.id, "general", ChannelType.TEXT))!!

        // Get everyone role id
        val roles = serverRepository.getRoles(server.id)
        val everyoneRole = roles.find { it.priorityLevel == website.woodendoor.conflux.DEFAULT_ROLE_PRIORITY_EVERYONE }!!

        // Initially member has MESSAGING and VIEW_CHANNEL from @everyone server role (check server repository createServer for default perms)
        val initialPerms = channelRepository.getEffectivePermissions(server.id, channel.id, member.id)
        assertEquals(ConfluxPermission.MESSAGING or ConfluxPermission.VIEW_CHANNEL, initialPerms)

        // Deny MESSAGING for @everyone in channel
        channelRepository.upsertOverride(channel.id, ChannelPermissionOverride(UUID.randomUUID().toString(), channel.id, everyoneRole.id, OverrideType.ROLE, 0L, ConfluxPermission.MESSAGING))
        val afterDenyPerms = channelRepository.getEffectivePermissions(server.id, channel.id, member.id)
        assertEquals(ConfluxPermission.VIEW_CHANNEL, afterDenyPerms, "Messaging should be denied by @everyone override, but VIEW_CHANNEL should still be present")

        // Allow CHANNEL_MANAGEMENT for @everyone in channel (Expandable)
        // Note: Channel settings replace server base for the specific bit. 
        // If we allow CHANNEL_MANAGEMENT, it doesn't necessarily mean we deny MESSAGING unless we explicitly set it.
        // In our implementation: effective = (serverBase & ~deny) | allow.
        // If serverBase has MESSAGING (1) and we allow CHANNEL_MANAGEMENT (2) without denying MESSAGING, 
        // then effective = (1 & ~0) | 2 = 3 (MESSAGING | CHANNEL_MANAGEMENT).
        channelRepository.upsertOverride(channel.id, ChannelPermissionOverride(UUID.randomUUID().toString(), channel.id, everyoneRole.id, OverrideType.ROLE, ConfluxPermission.CHANNEL_MANAGEMENT, 0L))
        val afterAllowPerms = channelRepository.getEffectivePermissions(server.id, channel.id, member.id)
        assertEquals(ConfluxPermission.MESSAGING or ConfluxPermission.CHANNEL_MANAGEMENT or ConfluxPermission.VIEW_CHANNEL, afterAllowPerms, "Channel management should be allowed, and messaging and view channel should still be inherited from server base")
    }

    @Test
    fun testRoleOverridesUnion() = runBlocking {
        val owner = User("owner1", "owner", "0001")
        val member = User("member1", "member", "0002")
        userRepository.createUser(owner)
        userRepository.createUser(member)
        val server = serverRepository.createServer(Server("s1", "Test Server", owner.id))!!
        serverRepository.joinServer(member.id, server.id)
        val channel = channelRepository.createChannel(Channel("c1", server.id, "general", ChannelType.TEXT))!!

        val role1 = serverRepository.createRole(server.id, Role("r1", server.id, "Role 1", 0L, null, 10))!!
        val role2 = serverRepository.createRole(server.id, Role("r2", server.id, "Role 2", 0L, null, 20))!!
        serverRepository.assignRoleToMember(server.id, member.id, role1.id)
        serverRepository.assignRoleToMember(server.id, member.id, role2.id)

        // role1 allows MESSAGING, role2 denies it -> Deny wins if Union is handled as (Effective & ~rolesDeny) | rolesAllow
        // Wait, Discord Union is: (Effective & ~rolesDeny) | rolesAllow WHERE rolesAllow is union of all allows, rolesDeny is union of all denies.
        // If any role allows, it should be allowed? Discord's logic is:
        // 1. Base perms
        // 2. @everyone denies, then allows
        // 3. Union of role denies, then union of role allows
        // 4. Member denies, then member allows

        channelRepository.upsertOverride(channel.id, ChannelPermissionOverride("o1", channel.id, role1.id, OverrideType.ROLE, ConfluxPermission.MESSAGING, 0L))
        channelRepository.upsertOverride(channel.id, ChannelPermissionOverride("o2", channel.id, role2.id, OverrideType.ROLE, 0L, ConfluxPermission.MESSAGING))

        val perms = channelRepository.getEffectivePermissions(server.id, channel.id, member.id)
        // If union of allows is MESSAGING and union of denies is MESSAGING, and we apply denies THEN allows, it should be allowed.
        assertTrue(ConfluxPermission.hasPermission(perms, ConfluxPermission.MESSAGING), "If any role allows, it should be allowed (Discord style union)")
    }

    @Test
    fun testUserOverride() = runBlocking {
        val owner = User("owner1", "owner", "0001")
        val member = User("member1", "member", "0002")
        userRepository.createUser(owner)
        userRepository.createUser(member)
        val server = serverRepository.createServer(Server("s1", "Test Server", owner.id))!!
        serverRepository.joinServer(member.id, server.id)
        val channel = channelRepository.createChannel(Channel("c1", server.id, "general", ChannelType.TEXT))!!

        // Roles deny MESSAGING
        val everyoneRole = serverRepository.getRoles(server.id).find { it.name == "@everyone" }!!
        channelRepository.upsertOverride(channel.id, ChannelPermissionOverride("o1", channel.id, everyoneRole.id, OverrideType.ROLE, 0L, ConfluxPermission.MESSAGING))

        // Specific user override allows MESSAGING
        channelRepository.upsertOverride(channel.id, ChannelPermissionOverride("o2", channel.id, member.id, OverrideType.USER, ConfluxPermission.MESSAGING, 0L))

        val perms = channelRepository.getEffectivePermissions(server.id, channel.id, member.id)
        assertTrue(ConfluxPermission.hasPermission(perms, ConfluxPermission.MESSAGING), "User override should take precedence over role overrides")
    }
}
