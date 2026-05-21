package website.woodendoor.conflux

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

actual fun formatTimestamp(timestamp: Long): String {
    return jsFormatTimestamp(timestamp.toDouble())
}

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
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