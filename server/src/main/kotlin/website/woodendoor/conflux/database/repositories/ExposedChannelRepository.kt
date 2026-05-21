package website.woodendoor.conflux.database.repositories

import website.woodendoor.conflux.database.DatabaseFactory.dbQuery
import website.woodendoor.conflux.database.models.*
import website.woodendoor.conflux.models.Channel
import website.woodendoor.conflux.models.ChannelPermissionOverride
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*

class ExposedChannelRepository : ChannelRepository {
    private fun resultRowToChannel(row: ResultRow) = Channel(
        id = row[Channels.id],
        serverId = row[Channels.serverId],
        name = row[Channels.name],
        type = row[Channels.type],
        topic = row[Channels.topic]
    )

    private fun resultRowToOverride(row: ResultRow) = ChannelPermissionOverride(
        id = row[ChannelPermissionOverrides.id],
        channelId = row[ChannelPermissionOverrides.channelId],
        targetId = row[ChannelPermissionOverrides.targetId],
        targetType = row[ChannelPermissionOverrides.targetType],
        allow = row[ChannelPermissionOverrides.allow],
        deny = row[ChannelPermissionOverrides.deny]
    )

    override suspend fun createChannel(channel: Channel): Channel? = dbQuery {
        val insertStatement = Channels.insert {
            it[id] = channel.id
            it[serverId] = channel.serverId
            it[name] = channel.name
            it[type] = channel.type
            it[topic] = channel.topic
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToChannel)
    }

    override suspend fun getChannel(id: String): Channel? = dbQuery {
        Channels.selectAll().where { Channels.id eq id }
            .map(::resultRowToChannel)
            .singleOrNull()
    }

    override suspend fun getChannelsByServer(serverId: String): List<Channel> = dbQuery {
        Channels.selectAll().where { Channels.serverId eq serverId }
            .map(::resultRowToChannel)
    }

    override suspend fun updateChannel(channel: Channel): Boolean = dbQuery {
        Channels.update({ Channels.id eq channel.id }) {
            it[name] = channel.name
            it[type] = channel.type
            it[topic] = channel.topic
        } > 0
    }

    override suspend fun deleteChannel(id: String): Boolean = dbQuery {
        ChannelPermissionOverrides.deleteWhere { ChannelPermissionOverrides.channelId eq id }
        Channels.deleteWhere { Channels.id eq id } > 0
    }

    override suspend fun getOverrides(channelId: String): List<ChannelPermissionOverride> = dbQuery {
        ChannelPermissionOverrides.selectAll().where { ChannelPermissionOverrides.channelId eq channelId }
            .map(::resultRowToOverride)
    }

    override suspend fun upsertOverride(channelId: String, override: ChannelPermissionOverride): Boolean = dbQuery {
        val existing = ChannelPermissionOverrides.selectAll()
            .where { 
                (ChannelPermissionOverrides.channelId eq channelId) and 
                (ChannelPermissionOverrides.targetId eq override.targetId) and 
                (ChannelPermissionOverrides.targetType eq override.targetType)
            }
            .singleOrNull()

        if (existing != null) {
            ChannelPermissionOverrides.update({ ChannelPermissionOverrides.id eq existing[ChannelPermissionOverrides.id] }) {
                it[allow] = override.allow
                it[deny] = override.deny
            } > 0
        } else {
            ChannelPermissionOverrides.insert {
                it[id] = override.id
                it[ChannelPermissionOverrides.channelId] = channelId
                it[targetId] = override.targetId
                it[targetType] = override.targetType
                it[allow] = override.allow
                it[deny] = override.deny
            }.insertedCount > 0
        }
    }

    override suspend fun deleteOverride(overrideId: String): Boolean = dbQuery {
        val override = ChannelPermissionOverrides.selectAll()
            .where { ChannelPermissionOverrides.id eq overrideId }
            .singleOrNull() ?: return@dbQuery false

        val channelId = override[ChannelPermissionOverrides.channelId]
        val channel = Channels.selectAll().where { Channels.id eq channelId }.singleOrNull() ?: return@dbQuery false
        val serverId = channel[Channels.serverId]

        val everyoneRole = Roles.selectAll()
            .where { (Roles.serverId eq serverId) and (Roles.priorityLevel eq website.woodendoor.conflux.DEFAULT_ROLE_PRIORITY_EVERYONE) }
            .singleOrNull() ?: return@dbQuery false

        if (override[ChannelPermissionOverrides.targetId] == everyoneRole[Roles.id]) {
            false
        } else {
            ChannelPermissionOverrides.deleteWhere { id eq overrideId } > 0
        }
    }

    override suspend fun getEffectivePermissions(serverId: String, channelId: String, userId: String): Long = dbQuery {
        // 1. Owner Exemption
        val server = Servers.selectAll().where { Servers.id eq serverId }.singleOrNull() ?: return@dbQuery 0L
        if (server[Servers.ownerId] == userId) {
            return@dbQuery website.woodendoor.conflux.models.ConfluxPermission.ALL
        }

        // 2. Server Base
        val everyoneRole = Roles.selectAll()
            .where { (Roles.serverId eq serverId) and (Roles.priorityLevel eq website.woodendoor.conflux.DEFAULT_ROLE_PRIORITY_EVERYONE) }
            .singleOrNull() ?: return@dbQuery 0L

        val userRoles = (Roles innerJoin MemberRoles)
            .select(Roles.id, Roles.permissions)
            .where { (MemberRoles.serverId eq serverId) and (MemberRoles.userId eq userId) }
            .map { it[Roles.id] to it[Roles.permissions] }
        
        val serverBasePerms = userRoles.fold(everyoneRole[Roles.permissions]) { acc, p -> acc or p.second }

        // 3. Channel Overrides
        val overrides = ChannelPermissionOverrides.selectAll()
            .where { ChannelPermissionOverrides.channelId eq channelId }
            .map(::resultRowToOverride)
        
        var effective = serverBasePerms

        // Layer 1: @everyone
        overrides.find { it.targetId == everyoneRole[Roles.id] }?.let { override ->
            effective = (effective and override.deny.inv()) or override.allow
        }

        // Layer 2: Roles (Union)
        val roleIds = userRoles.map { it.first }.toSet() - everyoneRole[Roles.id]
        val roleOverrides = overrides.filter { it.targetId in roleIds && it.targetType == website.woodendoor.conflux.models.OverrideType.ROLE }
        
        if (roleOverrides.isNotEmpty()) {
            val rolesAllow = roleOverrides.fold(0L) { acc, o -> acc or o.allow }
            val rolesDeny = roleOverrides.fold(0L) { acc, o -> acc or o.deny }
            effective = (effective and rolesDeny.inv()) or rolesAllow
        }

        // Layer 3: User
        overrides.find { it.targetId == userId && it.targetType == website.woodendoor.conflux.models.OverrideType.USER }?.let { override ->
            effective = (effective and override.deny.inv()) or override.allow
        }

        effective
    }
}
