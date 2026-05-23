package website.woodendoor.conflux.controller

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import website.woodendoor.conflux.database.repositories.UserRepository
import website.woodendoor.conflux.models.LoginRequest
import website.woodendoor.conflux.models.User
import website.woodendoor.conflux.exceptions.BadRequestException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UserControllerTest {
    private val userRepository = mockk<UserRepository>()
    private val userService = website.woodendoor.conflux.service.impl.UserServiceImpl(userRepository)
    private val controller = UserController(userService)

    @Test
    fun `test login with existing user`() {
        runBlocking {
            val request = LoginRequest("existing")
            val user = User("user-1", "existing", "1234")
            
            coEvery { userRepository.findByUsername("existing") } returns user
            
            val result = controller.login(request)
            
            assertEquals(user, result)
        }
    }

    @Test
    fun `test login with new user`() {
        runBlocking {
            val request = LoginRequest("newuser")
            
            coEvery { userRepository.findByUsername("newuser") } returns null
            coEvery { userRepository.createUser(any()) } answers { firstArg() }
            
            val result = controller.login(request)
            
            assertEquals("newuser", result.username)
        }
    }

    @Test
    fun `test login with invalid username`() {
        runBlocking {
            val request = LoginRequest("a") // Too short
            
            assertFailsWith<BadRequestException> {
                controller.login(request)
            }
        }
    }
}
