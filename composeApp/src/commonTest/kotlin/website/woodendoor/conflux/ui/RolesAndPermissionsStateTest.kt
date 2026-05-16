package website.woodendoor.conflux.ui

import website.woodendoor.conflux.models.Role
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RolesAndPermissionsStateTest {
    @Test
    fun testSelectRole() {
        val role = Role("r1", "s1", "Admin", 1L)
        val state = RolesAndPermissionsState(listOf(role))
        
        assertNull(state.selectedRole)
        state.selectRole(role)
        assertEquals(role, state.selectedRole)
    }

    @Test
    fun testUpdateRolesMaintainsSelection() {
        val role1 = Role("r1", "s1", "Admin", 1L)
        val role2 = Role("r2", "s1", "User", 0L)
        val state = RolesAndPermissionsState(listOf(role1, role2))
        
        state.selectRole(role1)
        
        val updatedRole1 = role1.copy(name = "Super Admin")
        state.updateRoles(listOf(updatedRole1, role2))
        
        assertEquals("Super Admin", state.selectedRole?.name)
        assertEquals("r1", state.selectedRole?.id)
    }

    @Test
    fun testPendingPermissions() {
        val role = Role("r1", "s1", "Admin", 1L)
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
        val role = Role("r1", "s1", "Admin", 1L)
        val state = RolesAndPermissionsState(listOf(role))
        state.selectRole(role)
        
        assertTrue(!state.hasChanges)
        state.updatePendingPermission(1L, false)
        assertTrue(state.hasChanges)
        
        state.updatePendingPermission(1L, true) // Back to original
        assertTrue(!state.hasChanges)
    }

    @Test
    fun testRolesSorting() {
        val role1 = Role("r1", "s1", "User", 0L, priorityLevel = 0)
        val role2 = Role("r2", "s1", "Admin", 1L, priorityLevel = 10)
        val state = RolesAndPermissionsState(listOf(role1, role2))
        
        assertEquals(role2, state.roles[0])
        assertEquals(role1, state.roles[1])
    }

    @Test
    fun testPendingPriority() {
        val role = Role("r1", "s1", "Admin", 1L, priorityLevel = 5)
        val state = RolesAndPermissionsState(listOf(role))
        state.selectRole(role)
        
        assertEquals("5", state.pendingPriorityText)
        state.updatePendingPriority("10")
        assertEquals("10", state.pendingPriorityText)
        assertTrue(state.hasChanges)
        assertTrue(state.canSave)
        
        state.revertChanges()
        assertEquals("5", state.pendingPriorityText)
        assertEquals(5, state.selectedRole?.priorityLevel)
    }

    @Test
    fun testHasChangesWithPriority() {
        val role = Role("r1", "s1", "Admin", 1L, priorityLevel = 5)
        val state = RolesAndPermissionsState(listOf(role))
        state.selectRole(role)
        
        assertTrue(!state.hasChanges)
        state.updatePendingPriority("6")
        assertTrue(state.hasChanges)
        
        state.updatePendingPriority("5") // Back to original
        assertTrue(!state.hasChanges)
    }

    @Test
    fun testPriorityValidation() {
        val role = Role("r1", "s1", "Admin", 1L, priorityLevel = 50)
        val state = RolesAndPermissionsState(listOf(role))
        state.selectRole(role)
        
        assertTrue(state.isPriorityValid)
        
        state.updatePendingPriority("-1")
        assertTrue(!state.isPriorityValid)
        
        state.updatePendingPriority("101")
        assertTrue(!state.isPriorityValid)
        
        state.updatePendingPriority("100")
        assertTrue(state.isPriorityValid)
        
        state.updatePendingPriority("0")
        assertTrue(state.isPriorityValid)

        state.updatePendingPriority("abc")
        assertTrue(!state.isPriorityValid)

        state.updatePendingPriority("")
        assertTrue(!state.isPriorityValid)
    }

    @Test
    fun testHasChangesWithInvalidPriority() {
        val role = Role("r1", "s1", "Admin", 1L, priorityLevel = 50)
        val state = RolesAndPermissionsState(listOf(role))
        state.selectRole(role)
        
        assertTrue(!state.hasChanges)
        
        state.updatePendingPriority("51")
        assertTrue(state.hasChanges)
        assertTrue(state.canSave)
        
        state.updatePendingPriority("101")
        assertTrue(state.hasChanges) // Now hasChanges remains true even if invalid
        assertTrue(!state.canSave) // But canSave is false
        
        state.updatePendingPriority("100")
        assertTrue(state.hasChanges)
        assertTrue(state.canSave)
        
        state.updatePendingPriority("-1")
        assertTrue(state.hasChanges)
        assertTrue(!state.canSave)

        state.updatePendingPriority("abc")
        assertTrue(state.hasChanges)
        assertTrue(!state.canSave)

        state.updatePendingPriority("")
        assertTrue(state.hasChanges)
        assertTrue(!state.canSave)
    }
}
