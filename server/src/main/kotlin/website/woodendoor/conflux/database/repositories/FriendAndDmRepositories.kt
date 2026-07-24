package website.woodendoor.conflux.database.repositories

import website.woodendoor.conflux.models.Channel
import website.woodendoor.conflux.models.Friendship
import website.woodendoor.conflux.models.FriendshipStatus

interface FriendRepository {
    suspend fun getFriendships(userId: String): List<Friendship>
    suspend fun findFriendship(userId: String, friendId: String): Friendship?
    suspend fun addFriendship(userId: String, friendId: String, status: FriendshipStatus): Friendship
    suspend fun updateFriendshipStatus(id: String, status: FriendshipStatus): Boolean
    suspend fun deleteFriendship(id: String): Boolean
}

interface DirectMessageRepository {
    suspend fun getOrCreateDmChannel(userId1: String, userId2: String): Channel
    suspend fun getUserDmChannels(userId: String): List<Channel>
    suspend fun getDmChannelMembers(channelId: String): List<String>
}
