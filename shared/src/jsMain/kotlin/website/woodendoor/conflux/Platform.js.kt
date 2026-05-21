package website.woodendoor.conflux

class JsPlatform: Platform {
    override val name: String = "Web with Kotlin/JS"
}

actual fun getPlatform(): Platform = JsPlatform()

actual fun formatTimestamp(timestamp: Long): String {
    val date = kotlin.js.Date(timestamp.toDouble())
    val today = kotlin.js.Date()
    val isToday = date.getFullYear() == today.getFullYear() &&
                  date.getMonth() == today.getMonth() &&
                  date.getDate() == today.getDate()
                  
    val hours = date.getHours().toString().padStart(2, '0')
    val minutes = date.getMinutes().toString().padStart(2, '0')
    
    return if (isToday) {
        "$hours:$minutes"
    } else {
        val year = date.getFullYear()
        val month = (date.getMonth() + 1).toString().padStart(2, '0')
        val day = date.getDate().toString().padStart(2, '0')
        "$year/$month/$day $hours:$minutes"
    }
}