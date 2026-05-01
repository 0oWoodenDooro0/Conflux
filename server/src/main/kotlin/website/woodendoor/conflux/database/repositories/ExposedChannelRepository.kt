package website.woodendoor.conflux.database.repositories

import website.woodendoor.conflux.database.DatabaseFactory.dbQuery
import website.woodendoor.conflux.database.models.*
import website.woodendoor.conflux.models.Channel
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*

class ExposedChannelRepository : ChannelRepository {
    override suspend fun createChannel(channel: Channel): Channel? = TODO()
    override suspend fun getChannel(id: String): Channel? = TODO()
    override suspend fun getChannelsByServer(serverId: String): List<Channel> = TODO()
    override suspend fun updateChannel(channel: Channel): Boolean = TODO()
    override suspend fun deleteChannel(id: String): Boolean = TODO()
}
