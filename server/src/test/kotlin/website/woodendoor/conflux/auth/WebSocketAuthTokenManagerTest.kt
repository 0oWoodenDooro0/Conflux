package website.woodendoor.conflux.auth

import kotlin.test.*

class WebSocketAuthTokenManagerTest {

    private lateinit var tokenManager: WebSocketAuthTokenManager

    @BeforeTest
    fun setup() {
        tokenManager = WebSocketAuthTokenManager()
    }

    @Test
    fun `generateToken should return a non-empty string`() {
        val userId = "user-123"
        val token = tokenManager.generateToken(userId)
        assertTrue(token.isNotEmpty(), "Token should not be empty")
    }

    @Test
    fun `validateToken should return userId for a valid token`() {
        val userId = "user-123"
        val token = tokenManager.generateToken(userId)
        val validatedUserId = tokenManager.validateToken(token)
        assertEquals(userId, validatedUserId, "Should return the correct user ID for a valid token")
    }

    @Test
    fun `validateToken should return null for an invalid token`() {
        val validatedUserId = tokenManager.validateToken("invalid-token")
        assertNull(validatedUserId, "Should return null for an invalid token")
    }

    @Test
    fun `token should be invalid after it is consumed`() {
        val userId = "user-123"
        val token = tokenManager.generateToken(userId)
        tokenManager.validateToken(token)
        val secondValidation = tokenManager.validateToken(token)
        assertNull(secondValidation, "Token should be one-time use or invalid after validation if that's the design")
    }
}
