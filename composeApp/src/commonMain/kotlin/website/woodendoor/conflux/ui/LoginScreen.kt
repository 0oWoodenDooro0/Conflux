package website.woodendoor.conflux.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import website.woodendoor.conflux.validation.UsernameValidator
import website.woodendoor.conflux.validation.ValidationResult

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                errorMessage = null
            },
            label = { Text("Username") },
            isError = errorMessage != null,
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
                        onLoginSuccess(username)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Login")
        }
    }
}
