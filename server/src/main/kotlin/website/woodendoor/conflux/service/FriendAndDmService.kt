package website.woodendoor.conflux.service

import website.woodendoor.conflux.models.*

interface FriendAndDmService {
    suspend fun getFriends(userId: String): List<Friendship>
    suspend fun sendFriendRequest(userId: String, targetUsername: String): Friendship
    suspend fun acceptFriendRequest(userId: String, friendshipId: String): Boolean
    suspend fun blockUser(userId: String, targetUserId: String): Boolean
    suspend fun removeFriend(userId: String, friendshipId: String): Boolean
    suspend fun openDmChannel(userId: String, targetUserId: String): Channel
    suspend fun getUserDmChannels(userId: String): List<Channel>
}
