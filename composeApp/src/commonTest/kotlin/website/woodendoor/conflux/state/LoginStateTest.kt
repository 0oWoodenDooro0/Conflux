package website.woodendoor.conflux.state

import website.woodendoor.conflux.models.User
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LoginStateTest {

    @BeforeTest
    fun setup() {
        LoginState.logout()
    }

    @Test
    fun testInitialState() {
        assertNull(LoginState.currentUser)
    }

    @Test
    fun testLogin() {
        val user = User("1", "testuser", "1234")
        LoginState.login(user)
        assertEquals(user, LoginState.currentUser)
    }

    @Test
    fun testLogout() {
        val user = User("1", "testuser", "1234")
        LoginState.login(user)
        LoginState.logout()
        assertNull(LoginState.currentUser)
    }
}
