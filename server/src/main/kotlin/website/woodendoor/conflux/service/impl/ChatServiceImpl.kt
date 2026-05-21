package website.woodendoor.conflux.service.impl

import website.woodendoor.conflux.database.repositories.MessageRepository
import website.woodendoor.conflux.models.Message
import website.woodendoor.conflux.service.ChatService

class ChatServiceImpl(
    private val messageRepository: MessageRepository
) : ChatService {

    override suspend fun sendMessage(channelId: String, senderId: String, content: String): Message? {
        return messageRepository.saveMessage(channelId, senderId, content)
    }

    override suspend fun getMessagesByChannel(channelId: String): List<Message> {
        return messageRepository.getMessagesByChannel(channelId)
    }
}
