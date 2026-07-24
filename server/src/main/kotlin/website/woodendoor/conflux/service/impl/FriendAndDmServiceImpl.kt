package website.woodendoor.conflux.service.impl

import website.woodendoor.conflux.database.repositories.DirectMessageRepository
import website.woodendoor.conflux.database.repositories.FriendRepository
import website.woodendoor.conflux.database.repositories.UserRepository
import website.woodendoor.conflux.exceptions.BadRequestException
import website.woodendoor.conflux.exceptions.ConflictException
import website.woodendoor.conflux.exceptions.UserNotFoundException
import website.woodendoor.conflux.models.*
import website.woodendoor.conflux.service.FriendAndDmService

class FriendAndDmServiceImpl(
    private val friendRepository: FriendRepository,
    private val dmRepository: DirectMessageRepository,
    private val userRepository: UserRepository
) : FriendAndDmService {

    override suspend fun getFriends(userId: String): List<Friendship> {
        return friendRepository.getFriendships(userId)
    }

    override suspend fun sendFriendRequest(userId: String, targetUsername: String): Friendship {
        val targetUser = userRepository.findByUsername(targetUsername)
            ?: throw UserNotFoundException("User $targetUsername not found")

        if (targetUser.id == userId) {
            throw BadRequestException("Cannot send friend request to yourself")
        }

        val existing = friendRepository.findFriendship(userId, targetUser.id)
        if (existing != null) {
            throw ConflictException("Friendship or request already exists")
        }

        return friendRepository.addFriendship(userId, targetUser.id, FriendshipStatus.PENDING)
    }

    override suspend fun acceptFriendRequest(userId: String, friendshipId: String): Boolean {
        return friendRepository.updateFriendshipStatus(friendshipId, FriendshipStatus.ACCEPTED)
    }

    override suspend fun blockUser(userId: String, targetUserId: String): Boolean {
        val existing = friendRepository.findFriendship(userId, targetUserId)
        return if (existing != null) {
            friendRepository.updateFriendshipStatus(existing.id, FriendshipStatus.BLOCKED)
        } else {
            friendRepository.addFriendship(userId, targetUserId, FriendshipStatus.BLOCKED)
            true
        }
    }

    override suspend fun removeFriend(userId: String, friendshipId: String): Boolean {
        return friendRepository.deleteFriendship(friendshipId)
    }

    override suspend fun openDmChannel(userId: String, targetUserId: String): Channel {
        val targetUser = userRepository.getUser(targetUserId)
            ?: throw UserNotFoundException("User $targetUserId not found")
        return dmRepository.getOrCreateDmChannel(userId, targetUser.id)
    }

    override suspend fun getUserDmChannels(userId: String): List<Channel> {
        return dmRepository.getUserDmChannels(userId)
    }
}
