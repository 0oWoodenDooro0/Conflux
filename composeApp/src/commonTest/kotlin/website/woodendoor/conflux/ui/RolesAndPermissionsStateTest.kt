package website.woodendoor.conflux.ui

import website.woodendoor.conflux.models.Role
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
    fun testPendingPermissions() {
        val role = Role("r1", "Admin", 1L)
        val state = RolesAndPermissionsState(listOf(role))
        state.selectRole(role)
        
        assertEquals(1L, state.pendingPermissions)
        state.updatePendingPermission(1L shl 1, true)
        assertEquals(1L or (1L shl 1), state.pendingPermissions)
        assertTrue(state.hasChanges)
        
        state.revertChanges()
        assertEquals(1L, state.pendingPermissions)
        assertEquals(1L, state.selectedRole?.permissions)
    }

    @Test
    fun testHasChanges() {
        val role = Role("r1", "Admin", 1L)
        val state = RolesAndPermissionsState(listOf(role))
        state.selectRole(role)
        
        assertTrue(!state.hasChanges)
        state.updatePendingPermission(1L, false)
        assertTrue(state.hasChanges)
        
        state.updatePendingPermission(1L, true) // Back to original
        assertTrue(!state.hasChanges)
    }
}
