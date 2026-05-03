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
}
