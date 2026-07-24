package website.woodendoor.conflux.database.repositories

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import website.woodendoor.conflux.database.DatabaseFactory.dbQuery
import website.woodendoor.conflux.database.models.*
import website.woodendoor.conflux.models.*
import java.util.UUID

class ExposedFriendRepository(
    private val userRepository: UserRepository
) : FriendRepository {

    override suspend fun getFriendships(userId: String): List<Friendship> = dbQuery {
        val rows = Friendships.selectAll()
            .where { (Friendships.userId eq userId) or (Friendships.friendId eq userId) }
            .toList()

        rows.map { row ->
            val fUserId = row[Friendships.userId]
            val fFriendId = row[Friendships.friendId]
            val otherId = if (fUserId == userId) fFriendId else fUserId
            val otherUser = userRepository.getUser(otherId)
            Friendship(
                id = row[Friendships.id],
                userId = fUserId,
                friendId = fFriendId,
                status = row[Friendships.status],
                friendUser = otherUser
            )
        }
    }

    override suspend fun findFriendship(userId: String, friendId: String): Friendship? = dbQuery {
        val row = Friendships.selectAll()
            .where {
                ((Friendships.userId eq userId) and (Friendships.friendId eq friendId)) or
                ((Friendships.userId eq friendId) and (Friendships.friendId eq userId))
            }
            .singleOrNull() ?: return@dbQuery null

        val fUserId = row[Friendships.userId]
        val fFriendId = row[Friendships.friendId]
        val otherId = if (fUserId == userId) fFriendId else fUserId
        val otherUser = userRepository.getUser(otherId)
        Friendship(
            id = row[Friendships.id],
            userId = fUserId,
            friendId = fFriendId,
            status = row[Friendships.status],
            friendUser = otherUser
        )
    }

    override suspend fun addFriendship(userId: String, friendId: String, status: FriendshipStatus): Friendship = dbQuery {
        val fId = UUID.randomUUID().toString()
        Friendships.insert {
            it[Friendships.id] = fId
            it[Friendships.userId] = userId
            it[Friendships.friendId] = friendId
            it[Friendships.status] = status
        }
        val friendUser = userRepository.getUser(friendId)
        Friendship(fId, userId, friendId, status, friendUser)
    }

    override suspend fun updateFriendshipStatus(id: String, status: FriendshipStatus): Boolean = dbQuery {
        Friendships.update({ Friendships.id eq id }) {
            it[Friendships.status] = status
        } > 0
    }

    override suspend fun deleteFriendship(id: String): Boolean = dbQuery {
        Friendships.deleteWhere { Friendships.id eq id } > 0
    }
}

class ExposedDirectMessageRepository(
    private val userRepository: UserRepository
) : DirectMessageRepository {

    override suspend fun getOrCreateDmChannel(userId1: String, userId2: String): Channel = dbQuery {
        val user1Channels = DirectMessageMembers.selectAll()
            .where { DirectMessageMembers.userId eq userId1 }
            .map { it[DirectMessageMembers.channelId] }
            .toSet()

        val user2Channels = DirectMessageMembers.selectAll()
            .where { DirectMessageMembers.userId eq userId2 }
            .map { it[DirectMessageMembers.channelId] }
            .toSet()

        val commonChannels = user1Channels.intersect(user2Channels)

        val existingChannelId = commonChannels.firstOrNull { chId ->
            val count = DirectMessageMembers.selectAll()
                .where { DirectMessageMembers.channelId eq chId }
                .count()
            count == 2L
        }

        if (existingChannelId != null) {
            val user1Obj = userRepository.getUser(userId1)
            val user2Obj = userRepository.getUser(userId2)
            val recipients = listOfNotNull(user1Obj, user2Obj)
            val otherNames = recipients.filter { it.id != userId1 }.map { it.username }
            val nameStr = if (otherNames.isEmpty()) "DM" else otherNames.joinToString(", ")
            return@dbQuery Channel(
                id = existingChannelId,
                serverId = "@me",
                name = nameStr,
                type = ChannelType.DM,
                recipients = recipients
            )
        }

        val newChannelId = UUID.randomUUID().toString()
        DirectMessageChannels.insert {
            it[DirectMessageChannels.id] = newChannelId
            it[DirectMessageChannels.type] = ChannelType.DM
        }
        DirectMessageMembers.insert {
            it[DirectMessageMembers.channelId] = newChannelId
            it[DirectMessageMembers.userId] = userId1
        }
        DirectMessageMembers.insert {
            it[DirectMessageMembers.channelId] = newChannelId
            it[DirectMessageMembers.userId] = userId2
        }

        val user1Obj = userRepository.getUser(userId1)
        val user2Obj = userRepository.getUser(userId2)
        val recipients = listOfNotNull(user1Obj, user2Obj)
        val otherNames = recipients.filter { it.id != userId1 }.map { it.username }
        val nameStr = if (otherNames.isEmpty()) "DM" else otherNames.joinToString(", ")

        Channel(
            id = newChannelId,
            serverId = "@me",
            name = nameStr,
            type = ChannelType.DM,
            recipients = recipients
        )
    }

    override suspend fun getUserDmChannels(userId: String): List<Channel> = dbQuery {
        val channelIds = DirectMessageMembers.selectAll()
            .where { DirectMessageMembers.userId eq userId }
            .map { it[DirectMessageMembers.channelId] }

        val channels = mutableListOf<Channel>()
        for (chId in channelIds) {
            val memberIds = DirectMessageMembers.selectAll()
                .where { DirectMessageMembers.channelId eq chId }
                .map { it[DirectMessageMembers.userId] }

            val recipients = memberIds.mapNotNull { userRepository.getUser(it) }
            val otherNames = recipients.filter { it.id != userId }.map { it.username }
            val nameStr = if (otherNames.isEmpty()) "Direct Message" else otherNames.joinToString(", ")

            channels.add(
                Channel(
                    id = chId,
                    serverId = "@me",
                    name = nameStr,
                    type = ChannelType.DM,
                    recipients = recipients
                )
            )
        }
        channels
    }

    override suspend fun getDmChannelMembers(channelId: String): List<String> = dbQuery {
        DirectMessageMembers.selectAll()
            .where { DirectMessageMembers.channelId eq channelId }
            .map { it[DirectMessageMembers.userId] }
    }
}
