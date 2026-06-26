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
            val request = LoginRequest("existing", "mypassword")
            val user = User("user-1", "existing", "1234")
            val hashed = website.woodendoor.conflux.util.PasswordHasher.hashPassword("mypassword")
            
            coEvery { userRepository.findByUsername("existing") } returns user
            coEvery { userRepository.getPasswordHash("existing") } returns hashed
            
            val result = controller.login(request)
            
            assertEquals(user, result)
        }
    }

    @Test
    fun `test login with incorrect password`() {
        runBlocking {
            val request = LoginRequest("existing", "wrongpassword")
            val user = User("user-1", "existing", "1234")
            val hashed = website.woodendoor.conflux.util.PasswordHasher.hashPassword("mypassword")
            
            coEvery { userRepository.findByUsername("existing") } returns user
            coEvery { userRepository.getPasswordHash("existing") } returns hashed
            
            assertFailsWith<website.woodendoor.conflux.exceptions.UnauthorizedException> {
                controller.login(request)
            }
        }
    }

    @Test
    fun `test login with non-existent user`() {
        runBlocking {
            val request = LoginRequest("nonexistent", "password")
            coEvery { userRepository.findByUsername("nonexistent") } returns null
            
            assertFailsWith<website.woodendoor.conflux.exceptions.UserNotFoundException> {
                controller.login(request)
            }
        }
    }

    @Test
    fun `test register new user`() {
        runBlocking {
            val request = website.woodendoor.conflux.models.RegisterRequest("newuser", "password123")
            
            coEvery { userRepository.findByUsername("newuser") } returns null
            coEvery { userRepository.createUser(any(), any()) } answers { firstArg() }
            
            val result = controller.register(request)
            
            assertEquals("newuser", result.username)
        }
    }

    @Test
    fun `test register with existing username`() {
        runBlocking {
            val request = website.woodendoor.conflux.models.RegisterRequest("existing", "password123")
            val user = User("user-1", "existing", "1234")
            
            coEvery { userRepository.findByUsername("existing") } returns user
            
            assertFailsWith<website.woodendoor.conflux.exceptions.ConflictException> {
                controller.register(request)
            }
        }
    }

    @Test
    fun `test login with invalid username`() {
        runBlocking {
            val request = LoginRequest("a", "password") // Too short
            
            assertFailsWith<BadRequestException> {
                controller.login(request)
            }
        }
    }
}
