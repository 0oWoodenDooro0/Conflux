package website.woodendoor.conflux.database.repositories

import website.woodendoor.conflux.models.Message

interface MessageRepository {
    suspend fun saveMessage(channelId: String, senderId: String, content: String): Message?
    suspend fun getMessagesByChannel(channelId: String): List<Message>
}
