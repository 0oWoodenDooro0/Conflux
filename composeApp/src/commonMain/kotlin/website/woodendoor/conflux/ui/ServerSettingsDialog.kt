package website.woodendoor.conflux.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import website.woodendoor.conflux.api.ServerApiClient
import website.woodendoor.conflux.models.Role
import website.woodendoor.conflux.models.Server
import website.woodendoor.conflux.models.User
import website.woodendoor.conflux.state.MainState

@Composable
fun ServerSettingsDialog(
    server: Server,
    apiClient: ServerApiClient,
    onDismissRequest: () -> Unit
) {
    var roles by remember { mutableStateOf<List<Role>>(emptyList()) }
    var members by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var tabIndex by remember { mutableStateOf(0) }
    var userToAssignRole by remember { mutableStateOf<User?>(null) }
    var isAddingRole by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun refreshData() {
        scope.launch {
            try {
                roles = apiClient.getRoles(server.id)
                members = apiClient.getMembers(server.id)
            } catch (e: Exception) {
                errorMessage = "Failed to load settings: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(server.id) {
        refreshData()
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Server Settings: ${server.name}") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp, max = 500.dp)) {
                TabRow(selectedTabIndex = tabIndex) {
                    Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Roles") })
                    Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Members") })
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    if (errorMessage != null) {
                        Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                    }

                    when (tabIndex) {
                        0 -> {
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                items(roles) { role ->
                                    ListItem(
                                        headlineContent = { Text(role.name) },
                                        supportingContent = { Text("Permissions: ${role.permissions}") },
                                        trailingContent = { Text("Priority: ${role.priorityLevel}") }
                                    )
                                }
                            }
                            Button(
                                onClick = { isAddingRole = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Add Role")
                            }
                        }
                        1 -> {
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                items(members) { user ->
                                    ListItem(
                                        headlineContent = { Text("${user.username}#${user.discriminator}") },
                                        supportingContent = { Text("ID: ${user.id}") },
                                        trailingContent = {
                                            TextButton(onClick = { userToAssignRole = user }) {
                                                Text("Assign Role")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismissRequest) {
                Text("Close")
            }
        }
    )

    if (userToAssignRole != null) {
        RoleAssignmentDialog(
            user = userToAssignRole!!,
            roles = roles,
            onDismiss = { userToAssignRole = null },
            onAssign = { roleId ->
                scope.launch {
                    val adminId = MainState.currentUserId ?: return@launch
                    apiClient.assignRole(server.id, adminId, userToAssignRole!!.id, roleId)
                    userToAssignRole = null
                    refreshData()
                }
            }
        )
    }

    if (isAddingRole) {
        RoleCreationDialog(
            onDismiss = { isAddingRole = false },
            onAdd = { name, permissions, priority ->
                scope.launch {
                    val adminId = MainState.currentUserId ?: return@launch
                    apiClient.createRole(server.id, adminId, name, permissions, priority = priority)
                    isAddingRole = false
                    refreshData()
                }
            }
        )
    }
}

@Composable
fun RoleCreationDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Long, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var permissions by remember { mutableStateOf(0L) }
    var priority by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Role") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Role Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = priority.toString(),
                    onValueChange = { priority = it.toIntOrNull() ?: 0 },
                    label = { Text("Priority Level") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text("Permissions: $permissions (Bitmask)")
                // Simplified: Just a text field for bitmask for now
                TextField(
                    value = permissions.toString(),
                    onValueChange = { permissions = it.toLongOrNull() ?: 0L },
                    label = { Text("Permissions Bitmask") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(name, permissions, priority) },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RoleAssignmentDialog(
    user: User,
    roles: List<Role>,
    onDismiss: () -> Unit,
    onAssign: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Role to ${user.username}") },
        text = {
            LazyColumn {
                items(roles) { role ->
                    ListItem(
                        headlineContent = { Text(role.name) },
                        trailingContent = {
                            Button(onClick = { onAssign(role.id) }) {
                                Text("Assign")
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
