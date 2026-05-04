package website.woodendoor.conflux.controller

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import website.woodendoor.conflux.database.repositories.ServerRepository
import website.woodendoor.conflux.models.CreateRoleRequest
import website.woodendoor.conflux.models.Role
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RoleControllerTest {
    private val serverRepository = mockk<ServerRepository>()
    private val controller = RoleController(serverRepository)

    @Test
    fun `test createRole success`() = runBlocking {
        val request = CreateRoleRequest("Admin", 1L)
        val role = Role("role-1", "Admin", 1L)
        
        coEvery { serverRepository.createRole("server-1", any()) } returns role
        
        val result = controller.createRole("server-1", request)
        
        assertTrue(result is OperationResult.Success)
        assertEquals(role, result.data)
    }

    @Test
    fun `test createRole failure`() = runBlocking {
        val request = CreateRoleRequest("Admin", 1L)
        
        coEvery { serverRepository.createRole("server-1", any()) } returns null
        
        val result = controller.createRole("server-1", request)
        
        assertTrue(result is OperationResult.Failure.InternalError)
    }
}
