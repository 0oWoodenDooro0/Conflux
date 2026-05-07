package website.woodendoor.conflux.database.models

import org.jetbrains.exposed.v1.core.Table

object Servers : Table("servers") {
    val id = varchar("id", 36)
    val name = varchar("name", 100)
    val ownerId = varchar("owner_id", 36).references(Users.id)

    override val primaryKey = PrimaryKey(id)
}

object Roles : Table("roles") {
    val id = varchar("id", 36)
    val serverId = varchar("server_id", 36).references(Servers.id)
    val name = varchar("name", 100)
    val permissions = long("permissions")
    val color = integer("color").nullable()
    val priorityLevel = integer("priority_level").default(0)

    override val primaryKey = PrimaryKey(id)
}

object MemberRoles : Table("member_roles") {
    val serverId = varchar("server_id", 36).references(Servers.id)
    val userId = varchar("user_id", 36).references(Users.id)
    val roleId = varchar("role_id", 36).references(Roles.id)

    override val primaryKey = PrimaryKey(serverId, userId, roleId)
}

object Channels : Table("channels") {
    val id = varchar("id", 36)
    val serverId = varchar("server_id", 36).references(Servers.id)
    val name = varchar("name", 100)
    val type = enumerationByName("type", 20, website.woodendoor.conflux.models.ChannelType::class)
    val topic = varchar("topic", 255).nullable()

    override val primaryKey = PrimaryKey(id)
}

// Junction table for Server Members (User <-> Server)
object ServerMembers : Table("server_members") {
    val serverId = varchar("server_id", 36).references(Servers.id)
    val userId = varchar("user_id", 36).references(Users.id)

    override val primaryKey = PrimaryKey(serverId, userId)
}
