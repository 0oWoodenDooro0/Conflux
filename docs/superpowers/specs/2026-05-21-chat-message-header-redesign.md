# Chat Message Header Redesign Spec

Redesign the message header in the ChatRoom UI to display the author's username and the message timestamp with clear visual hierarchy, rather than the raw user UUID.

## Goal

- Replace the raw User UUID (`User <uuid>`) in chat messages with `<username> <time>`.
- Use a harmonious visual hierarchy where the username is prominent (primary color) and the time is subtle (secondary/muted color).
- Dynamically format the time based on the local device date:
  - If the message timestamp is **today**, show only `HH:mm`.
  - If the message timestamp is **not today**, show `yyyy/MM/dd HH:mm`.
- Fall back to `User <uuid>` if the username cannot be resolved.

## Proposed Changes

### 1. Date/Time Formatting Interface (`shared`)

We define a cross-platform date/time formatting function `formatTimestamp` inside the `shared` module, since it compiles to all target platforms (JVM, JS, WasmJS).

#### [MODIFY] [Platform.kt](file:///home/user/IdeaProjects/Conflux/shared/src/commonMain/kotlin/website/woodendoor/conflux/Platform.kt)
Declare the `expect` function:
```kotlin
expect fun formatTimestamp(timestamp: Long): String
```

#### [MODIFY] [Platform.jvm.kt](file:///home/user/IdeaProjects/Conflux/shared/src/jvmMain/kotlin/website/woodendoor/conflux/Platform.jvm.kt)
Implement using Java `java.time` APIs:
```kotlin
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

actual fun formatTimestamp(timestamp: Long): String {
    val instant = Instant.ofEpochMilli(timestamp)
    val zoneId = ZoneId.systemDefault()
    val localDateTime = instant.atZone(zoneId).toLocalDateTime()
    val localDate = localDateTime.toLocalDate()
    val today = LocalDate.now(zoneId)
    
    return if (localDate == today) {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        localDateTime.format(formatter)
    } else {
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
        localDateTime.format(formatter)
    }
}
```

#### [MODIFY] [Platform.js.kt](file:///home/user/IdeaProjects/Conflux/shared/src/jsMain/kotlin/website/woodendoor/conflux/Platform.js.kt)
Implement using JavaScript `Date` APIs:
```kotlin
import kotlin.js.Date

actual fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp.toDouble())
    val today = Date()
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
```

#### [MODIFY] [Platform.wasmJs.kt](file:///home/user/IdeaProjects/Conflux/shared/src/wasmJsMain/kotlin/website/woodendoor/conflux/Platform.wasmJs.kt)
Implement using browser-native Javascript execution:
```kotlin
actual fun formatTimestamp(timestamp: Long): String {
    return jsFormatTimestamp(timestamp.toDouble())
}

private fun jsFormatTimestamp(timestamp: Double): String = js("""
    (function(ts) {
        var date = new Date(ts);
        var today = new Date();
        var isToday = date.getFullYear() === today.getFullYear() &&
                      date.getMonth() === today.getMonth() &&
                      date.getDate() === today.getDate();
                      
        var hours = String(date.getHours()).padStart(2, '0');
        var minutes = String(date.getMinutes()).padStart(2, '0');
        
        if (isToday) {
            return hours + ':' + minutes;
        } else {
            var year = date.getFullYear();
            var month = String(date.getMonth() + 1).padStart(2, '0');
            var day = String(date.getDate()).padStart(2, '0');
            return year + '/' + month + '/' + day + ' ' + hours + ':' + minutes;
        }
    })(timestamp)
""")
```

### 2. User Caching & Chat Room UI (`composeApp`)

#### [MODIFY] [ChatRoom.kt](file:///home/user/IdeaProjects/Conflux/composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/ChatRoom.kt)
- Cache server members when `channel.serverId` changes.
- Lookup sender username in the cache; fallback to `"User ${message.authorId}"`.
- Update `MessageItem` parameter list to take `authorName` and `formattedTime`.
- Position the name and time side-by-side inside a `Row`, separated by space, using distinct styles and colors for visual hierarchy.

---

## Verification Plan

### Automated Build & Unit Tests
Run the project gradle build and test task to ensure all platforms build and unit tests pass:
```bash
./gradlew test
```
