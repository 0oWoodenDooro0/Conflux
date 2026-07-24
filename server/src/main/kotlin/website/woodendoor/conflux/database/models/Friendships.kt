package website.woodendoor.conflux.database.models

import org.jetbrains.exposed.v1.core.Table
import website.woodendoor.conflux.models.ChannelType
import website.woodendoor.conflux.models.FriendshipStatus

object Friendships : Table("friendships") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36).references(Users.id)
    val friendId = varchar("friend_id", 36).references(Users.id)
    val status = enumerationByName("status", 20, FriendshipStatus::class)

    override val primaryKey = PrimaryKey(id)
}

object DirectMessageChannels : Table("direct_message_channels") {
    val id = varchar("id", 36)
    val type = enumerationByName("type", 20, ChannelType::class).default(ChannelType.DM)
    val name = varchar("name", 100).nullable()

    override val primaryKey = PrimaryKey(id)
}

object DirectMessageMembers : Table("direct_message_members") {
    val channelId = varchar("channel_id", 36).references(DirectMessageChannels.id)
    val userId = varchar("user_id", 36).references(Users.id)

    override val primaryKey = PrimaryKey(channelId, userId)
}
