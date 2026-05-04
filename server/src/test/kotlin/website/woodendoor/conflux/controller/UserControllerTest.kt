package website.woodendoor.conflux.controller

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import website.woodendoor.conflux.database.repositories.UserRepository
import website.woodendoor.conflux.models.LoginRequest
import website.woodendoor.conflux.models.User
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserControllerTest {
    private val userRepository = mockk<UserRepository>()
    private val controller = UserController(userRepository)

    @Test
    fun `test login with existing user`() = runBlocking {
        val request = LoginRequest("existing")
        val user = User("user-1", "existing", "1234")
        
        coEvery { userRepository.findByUsername("existing") } returns user
        
        val result = controller.login(request)
        
        assertTrue(result is OperationResult.Success)
        assertEquals(user, (result as OperationResult.Success<User>).data)
    }

    @Test
    fun `test login with new user`() = runBlocking {
        val request = LoginRequest("newuser")
        
        coEvery { userRepository.findByUsername("newuser") } returns null
        coEvery { userRepository.createUser(any()) } answers { firstArg() }
        
        val result = controller.login(request)
        
        assertTrue(result is OperationResult.Success)
        val createdUser = (result as OperationResult.Success<User>).data
        assertEquals("newuser", createdUser.username)
    }

    @Test
    fun `test login with invalid username`() = runBlocking {
        val request = LoginRequest("a") // Too short
        
        val result = controller.login(request)
        
        assertTrue(result is OperationResult.Failure.BadRequest)
    }
}
