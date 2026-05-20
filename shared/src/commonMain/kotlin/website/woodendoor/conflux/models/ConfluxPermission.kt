package website.woodendoor.conflux.models

object ConfluxPermission {
    const val NONE = 0L
    const val MESSAGING = 1L shl 0
    const val CHANNEL_MANAGEMENT = 1L shl 1
    const val ROLE_MANAGEMENT = 1L shl 2
    const val SERVER_MANAGEMENT = 1L shl 3
    const val VIEW_CHANNEL = 1L shl 4
    const val ALL = MESSAGING or CHANNEL_MANAGEMENT or ROLE_MANAGEMENT or SERVER_MANAGEMENT or VIEW_CHANNEL

    fun hasPermission(current: Long, permission: Long): Boolean {
        return (current and permission) == permission
    }

    fun setPermission(current: Long, permission: Long, enabled: Boolean): Long {
        return if (enabled) {
            current or permission
        } else {
            current and permission.inv()
        }
    }
}
