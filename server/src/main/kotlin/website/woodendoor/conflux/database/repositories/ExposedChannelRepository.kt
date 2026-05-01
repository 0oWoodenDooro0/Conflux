package website.woodendoor.conflux.database.repositories

import website.woodendoor.conflux.database.DatabaseFactory.dbQuery
import website.woodendoor.conflux.database.models.*
import website.woodendoor.conflux.models.Channel
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
        Channels.deleteWhere { Channels.id eq id } > 0
    }
}
