package website.woodendoor.conflux.auth

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class WebSocketAuthTokenManager {
    private val tokens = ConcurrentHashMap<String, String>()

    fun generateToken(userId: String): String {
        val token = UUID.randomUUID().toString()
        tokens[token] = userId
        return token
    }

    fun validateToken(token: String): String? {
        return tokens.remove(token)
    }
}
