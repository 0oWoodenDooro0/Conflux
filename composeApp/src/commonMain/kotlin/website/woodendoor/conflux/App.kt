package website.woodendoor.conflux

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import website.woodendoor.conflux.state.LoginState
import website.woodendoor.conflux.state.MainState
import website.woodendoor.conflux.api.ServerApiClient
import website.woodendoor.conflux.ui.LoginScreen
import website.woodendoor.conflux.ui.MainScreen

@Composable
@Preview
fun App() {
    var isLoggedIn by remember { mutableStateOf(LoginState.currentUser != null) }
    val apiClient = remember { ServerApiClient(DEFAULT_BASE_URL) }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (!isLoggedIn) {
                LoginScreen(
                    onLoginSuccess = { user ->
                        LoginState.login(user)
                        MainState.initializeWebSocket(apiClient, user.id, DEFAULT_BASE_URL)
                        isLoggedIn = true
                    }
                )
            } else {
                MainScreen()
            }
        }
    }
}
