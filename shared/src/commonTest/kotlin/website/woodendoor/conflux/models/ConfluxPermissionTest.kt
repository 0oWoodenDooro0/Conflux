package website.woodendoor.conflux.models

import kotlin.test.Test
import kotlin.test.assertTrue

class ConfluxPermissionTest {
    @Test
    fun testServerManagementPermissionExists() {
        // This will fail to compile until SERVER_MANAGEMENT is added
        val serverManagement = ConfluxPermission.SERVER_MANAGEMENT
        assertTrue(serverManagement > 0)
    }

    @Test
    fun testAllPermissionsIncludesServerManagement() {
        val serverManagement = ConfluxPermission.SERVER_MANAGEMENT
        assertTrue((ConfluxPermission.ALL and serverManagement) == serverManagement)
    }

    @Test
    fun testHasPermission() {
        val permissions = ConfluxPermission.MESSAGING or ConfluxPermission.ROLE_MANAGEMENT
        assertTrue(ConfluxPermission.hasPermission(permissions, ConfluxPermission.MESSAGING))
        assertTrue(ConfluxPermission.hasPermission(permissions, ConfluxPermission.ROLE_MANAGEMENT))
        assertTrue(!ConfluxPermission.hasPermission(permissions, ConfluxPermission.CHANNEL_MANAGEMENT))
    }

    @Test
    fun testSetPermission() {
        var permissions = ConfluxPermission.NONE
        permissions = ConfluxPermission.setPermission(permissions, ConfluxPermission.MESSAGING, true)
        assertTrue(ConfluxPermission.hasPermission(permissions, ConfluxPermission.MESSAGING))
        
        permissions = ConfluxPermission.setPermission(permissions, ConfluxPermission.MESSAGING, false)
        assertTrue(!ConfluxPermission.hasPermission(permissions, ConfluxPermission.MESSAGING))
    }
}
