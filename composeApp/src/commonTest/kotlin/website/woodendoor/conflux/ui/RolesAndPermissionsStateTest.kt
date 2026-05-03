package website.woodendoor.conflux.ui

import website.woodendoor.conflux.models.Role
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RolesAndPermissionsStateTest {
    @Test
    fun testSelectRole() {
        val role = Role("r1", "Admin", 1L)
        val state = RolesAndPermissionsState(listOf(role))
        
        assertNull(state.selectedRole)
        state.selectRole(role)
        assertEquals(role, state.selectedRole)
    }

    @Test
    fun testUpdateRolesMaintainsSelection() {
        val role1 = Role("r1", "Admin", 1L)
        val role2 = Role("r2", "User", 0L)
        val state = RolesAndPermissionsState(listOf(role1, role2))
        
        state.selectRole(role1)
        
        val updatedRole1 = role1.copy(name = "Super Admin")
        state.updateRoles(listOf(updatedRole1, role2))
        
        assertEquals("Super Admin", state.selectedRole?.name)
        assertEquals("r1", state.selectedRole?.id)
    }

    @Test
    fun testUpdateRolesClearsSelectionIfMissing() {
        val role1 = Role("r1", "Admin", 1L)
        val state = RolesAndPermissionsState(listOf(role1))
        
        state.selectRole(role1)
        state.updateRoles(emptyList())
        
        assertNull(state.selectedRole)
    }
}
