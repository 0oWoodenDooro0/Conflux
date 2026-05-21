package website.woodendoor.conflux.controller

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import website.woodendoor.conflux.WebSocketConnectionManager
import website.woodendoor.conflux.database.repositories.ServerRepository
import website.woodendoor.conflux.models.CreateRoleRequest
import website.woodendoor.conflux.models.Role
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RoleControllerTest {
    private val serverRepository = mockk<ServerRepository>()
    private val connectionManager = mockk<WebSocketConnectionManager>(relaxed = true)
    private val roleService = website.woodendoor.conflux.service.impl.RoleServiceImpl(serverRepository)
    private val controller = RoleController(roleService, connectionManager)

    @Test
    fun `test createRole success`() = runBlocking {
        val request = CreateRoleRequest("Admin", 1L)
        val role = Role("role-1", "server-1", "Admin", 1L)
        
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

    @Test
    fun `test hasPermission`() = runBlocking {
        coEvery { serverRepository.getPermissionsForMember("server-1", "user-1") } returns 0b11L
        
        assertTrue(controller.hasPermission("server-1", "user-1", 0b01L))
        assertTrue(controller.hasPermission("server-1", "user-1", 0b10L))
        assertTrue(!controller.hasPermission("server-1", "user-1", 0b100L))
    }

    @Test
    fun `test getRoles`() = runBlocking {
        val roles = listOf(Role("role-1", "server-1", "Admin", 1L))
        coEvery { serverRepository.getRoles("server-1") } returns roles
        
        val result = controller.getRoles("server-1")
        assertTrue(result is OperationResult.Success)
        assertEquals(roles, (result as OperationResult.Success<List<Role>>).data)
    }

    @Test
    fun `test getRole success`() = runBlocking {
        val role = Role("role-1", "server-1", "Admin", 1L)
        coEvery { serverRepository.getRole("role-1") } returns role
        
        val result = controller.getRole("role-1")
        assertTrue(result is OperationResult.Success)
        assertEquals(role, (result as OperationResult.Success<Role>).data)
    }

    @Test
    fun `test updateRole success`() = runBlocking {
        val role = Role("role-1", "server-1", "Admin", 1L)
        coEvery { serverRepository.updateRole(role) } returns true
        
        val result = controller.updateRole(role)
        assertTrue(result is OperationResult.Success)
        coVerify { connectionManager.broadcastToServer("server-1", any()) }
    }

    @Test
    fun `test assignRoleToMember success`() = runBlocking {
        coEvery { serverRepository.assignRoleToMember("server-1", "user-1", "role-1") } returns true
        
        val result = controller.assignRoleToMember("server-1", "user-1", "role-1")
        assertTrue(result is OperationResult.Success)
        coVerify { connectionManager.broadcastToServer("server-1", any()) }
    }

    @Test
    fun `test removeRoleFromMember success`() = runBlocking {
        coEvery { serverRepository.removeRoleFromMember("server-1", "user-1", "role-1") } returns true
        
        val result = controller.removeRoleFromMember("server-1", "user-1", "role-1")
        assertTrue(result is OperationResult.Success)
        coVerify { connectionManager.broadcastToServer("server-1", any()) }
    }
}
