package website.woodendoor.conflux.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import website.woodendoor.conflux.DEFAULT_BASE_URL
import website.woodendoor.conflux.api.ServerApiClient
import org.koin.compose.koinInject

import website.woodendoor.conflux.models.User
import website.woodendoor.conflux.validation.UsernameValidator
import website.woodendoor.conflux.validation.ValidationResult

@Composable
fun LoginScreen(
    onLoginSuccess: (User) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val apiClient = koinInject<ServerApiClient>()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome to Conflux", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = username,
            onValueChange = { 
                username = it
                errorMessage = when (val result = UsernameValidator.validateCharacters(it)) {
                    is ValidationResult.Error -> result.message
                    ValidationResult.Success -> null
                }
            },
            label = { Text("Username") },
            isError = errorMessage != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                when (val result = UsernameValidator.validateUsername(username)) {
                    is ValidationResult.Error -> {
                        errorMessage = result.message
                    }
                    ValidationResult.Success -> {
                        scope.launch {
                            isLoading = true
                            try {
                                val user = apiClient.login(username)
                                onLoginSuccess(user)
                            } catch (e: Exception) {
                                errorMessage = "Login failed: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Login")
            }
        }
    }
}
