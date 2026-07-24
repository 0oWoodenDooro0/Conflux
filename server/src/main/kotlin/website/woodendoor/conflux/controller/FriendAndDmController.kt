package website.woodendoor.conflux.controller

import website.woodendoor.conflux.exceptions.BadRequestException
import website.woodendoor.conflux.models.*
import website.woodendoor.conflux.service.FriendAndDmService

class FriendAndDmController(
    private val friendAndDmService: FriendAndDmService
) {
    suspend fun getFriends(userId: String): List<Friendship> {
        return friendAndDmService.getFriends(userId)
    }

    suspend fun sendFriendRequest(userId: String, request: FriendRequestPayload): Friendship {
        if (request.targetUsername.isBlank()) {
            throw BadRequestException("Username cannot be blank")
        }
        return friendAndDmService.sendFriendRequest(userId, request.targetUsername)
    }

    suspend fun acceptFriendRequest(userId: String, friendshipId: String): Boolean {
        return friendAndDmService.acceptFriendRequest(userId, friendshipId)
    }

    suspend fun removeFriend(userId: String, friendshipId: String): Boolean {
        return friendAndDmService.removeFriend(userId, friendshipId)
    }

    suspend fun openDmChannel(userId: String, request: OpenDmRequest): Channel {
        return friendAndDmService.openDmChannel(userId, request.targetUserId)
    }

    suspend fun getUserDmChannels(userId: String): List<Channel> {
        return friendAndDmService.getUserDmChannels(userId)
    }
}
