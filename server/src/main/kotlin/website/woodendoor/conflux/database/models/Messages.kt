package website.woodendoor.conflux.database.models

import org.jetbrains.exposed.v1.core.Table

object Messages : Table("messages") {
    val id = varchar("id", 36)
    val channelId = varchar("channel_id", 36).references(Channels.id)
    val senderId = varchar("sender_id", 36).references(Users.id)
    val content = varchar("content", 2000)
    val timestamp = long("timestamp")

    override val primaryKey = PrimaryKey(id)
}
