@file:Suppress("DEPRECATION")
package website.woodendoor.conflux.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

@Composable
fun rememberClipboardHelper(): (String) -> Unit {
    val clipboardManager = LocalClipboardManager.current
    return { text ->
        clipboardManager.setText(AnnotatedString(text))
    }
}
