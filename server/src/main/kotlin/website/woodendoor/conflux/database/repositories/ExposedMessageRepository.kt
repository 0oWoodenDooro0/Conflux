package website.woodendoor.conflux.database.repositories

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import website.woodendoor.conflux.database.DatabaseFactory.dbQuery
import website.woodendoor.conflux.database.models.Messages
import website.woodendoor.conflux.models.Message
import java.util.*

class ExposedMessageRepository : MessageRepository {
    private fun resultRowToMessage(row: ResultRow) = Message(
        id = row[Messages.id],
        channelId = row[Messages.channelId],
        authorId = row[Messages.senderId],
        content = row[Messages.content],
        timestamp = row[Messages.timestamp]
    )

    override suspend fun saveMessage(channelId: String, senderId: String, content: String): Message? = dbQuery {
        val messageId = UUID.randomUUID().toString()
        val insertStatement = Messages.insert {
            it[id] = messageId
            it[Messages.channelId] = channelId
            it[Messages.senderId] = senderId
            it[Messages.content] = content
            it[timestamp] = System.currentTimeMillis()
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToMessage)
    }

    override suspend fun getMessagesByChannel(channelId: String): List<Message> = dbQuery {
        Messages.selectAll().where { Messages.channelId eq channelId }
            .orderBy(Messages.timestamp, SortOrder.ASC)
            .map(::resultRowToMessage)
    }
}
