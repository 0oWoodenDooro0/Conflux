package website.woodendoor.conflux.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChannelSettingsTabTest {
    @Test
    fun testChannelSettingsTabEnum() {
        val tabs = ChannelSettingsTab.entries
        assertEquals(2, tabs.size)
        assertTrue(tabs.contains(ChannelSettingsTab.Overview))
        assertTrue(tabs.contains(ChannelSettingsTab.Permissions))
    }
}
