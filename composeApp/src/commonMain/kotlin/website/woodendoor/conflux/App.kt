package website.woodendoor.conflux

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import website.woodendoor.conflux.state.LoginState
import website.woodendoor.conflux.ui.LoginScreen
import website.woodendoor.conflux.ui.MainScreen

@Composable
@Preview
fun App() {
    var isLoggedIn by remember { mutableStateOf(LoginState.currentUser != null) }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (!isLoggedIn) {
                LoginScreen(
                    onLoginSuccess = { user ->
                        LoginState.login(user)
                        isLoggedIn = true
                    }
                )
            } else {
                MainScreen()
            }
        }
    }
}
