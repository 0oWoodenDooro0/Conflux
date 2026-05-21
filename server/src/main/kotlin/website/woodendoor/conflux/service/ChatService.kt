package website.woodendoor.conflux.service

import website.woodendoor.conflux.models.Message

interface ChatService {
    suspend fun sendMessage(channelId: String, senderId: String, content: String): Message?
    suspend fun getMessagesByChannel(channelId: String): List<Message>
}
