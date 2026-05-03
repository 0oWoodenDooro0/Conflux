package website.woodendoor.conflux.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServerSettingsTabTest {
    @Test
    fun testServerSettingsTabEnum() {
        // This will fail to compile until ServerSettingsTab is created
        val tabs = ServerSettingsTab.entries
        assertEquals(4, tabs.size)
        assertTrue(tabs.contains(ServerSettingsTab.Overview))
        assertTrue(tabs.contains(ServerSettingsTab.Channels))
        assertTrue(tabs.contains(ServerSettingsTab.Roles))
        assertTrue(tabs.contains(ServerSettingsTab.Members))
    }
}
