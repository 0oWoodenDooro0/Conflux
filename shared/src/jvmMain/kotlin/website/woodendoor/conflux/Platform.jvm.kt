package website.woodendoor.conflux

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun formatTimestamp(timestamp: Long): String {
    val instant = java.time.Instant.ofEpochMilli(timestamp)
    val zoneId = java.time.ZoneId.systemDefault()
    val localDateTime = instant.atZone(zoneId).toLocalDateTime()
    val localDate = localDateTime.toLocalDate()
    val today = java.time.LocalDate.now(zoneId)
    
    return if (localDate == today) {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
        localDateTime.format(formatter)
    } else {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
        localDateTime.format(formatter)
    }
}