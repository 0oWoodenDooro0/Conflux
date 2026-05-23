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
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import website.woodendoor.conflux.di.sharedModule
import website.woodendoor.conflux.di.appModule


@Composable
@Preview
fun App() {
    KoinApplication(application = {
        modules(sharedModule, appModule)
    }) {
        var isLoggedIn by remember { mutableStateOf(LoginState.currentUser != null) }
        val apiClient = koinInject<ServerApiClient>()

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
}
