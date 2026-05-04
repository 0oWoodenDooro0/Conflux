package website.woodendoor.conflux.controller

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import website.woodendoor.conflux.database.repositories.ServerRepository
import website.woodendoor.conflux.database.repositories.UserRepository
import website.woodendoor.conflux.models.CreateServerRequest
import website.woodendoor.conflux.models.Server
import website.woodendoor.conflux.models.User
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServerControllerTest {
    private val serverRepository = mockk<ServerRepository>()
    private val userRepository = mockk<UserRepository>()
    private val controller = ServerController(serverRepository, userRepository)

    @Test
    fun `test createServer success`() = runBlocking {
        val request = CreateServerRequest("Test Server", ownerId = "owner-1")
        val server = Server("server-1", "Test Server", "owner-1")
        
        coEvery { userRepository.getUser("owner-1") } returns User("owner-1", "owner", "1234")
        coEvery { serverRepository.createServer(any()) } returns server
        
        val result = controller.createServer(request)
        
        assertTrue(result is OperationResult.Success)
        assertEquals(server, (result as OperationResult.Success<Server>).data)
    }

    @Test
    fun `test getServer not found`() = runBlocking {
        coEvery { serverRepository.getServer("server-1") } returns null
        
        val result = controller.getServer("server-1")
        
        assertTrue(result is OperationResult.Failure.NotFound)
    }
}
