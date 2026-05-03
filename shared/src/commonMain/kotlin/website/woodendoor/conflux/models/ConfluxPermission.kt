package website.woodendoor.conflux.models

object ConfluxPermission {
    const val NONE = 0L
    const val MESSAGING = 1L shl 0
    const val CHANNEL_MANAGEMENT = 1L shl 1
    const val ROLE_MANAGEMENT = 1L shl 2
    const val SERVER_MANAGEMENT = 1L shl 3
    const val ALL = MESSAGING or CHANNEL_MANAGEMENT or ROLE_MANAGEMENT or SERVER_MANAGEMENT
}
