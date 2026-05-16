package website.woodendoor.conflux.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
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
    var selectedTab by remember { mutableStateOf(ServerSettingsTab.Roles) }
    var userToAssignRole by remember { mutableStateOf<User?>(null) }
    var isAddingRole by remember { mutableStateOf(false) }
    val rolesState = remember { RolesAndPermissionsState() }
    val scope = rememberCoroutineScope()

    fun refreshData() {
        scope.launch {
            try {
                roles = apiClient.getRoles(server.id)
                rolesState.updateRoles(roles)
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

    LaunchedEffect(rolesState.selectedRole?.id) {
        val selectedRole = rolesState.selectedRole
        if (selectedRole != null) {
            try {
                val roleMembers = apiClient.getRoleMembers(server.id, selectedRole.id)
                rolesState.updateRoleMembers(roleMembers)
            } catch (e: Exception) {
                // Silently fail or log
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxSize().padding(16.dp),
        title = { 
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Server Settings: ${server.name}")
                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .width(240.dp)
                        .fillMaxHeight()
                ) {
                    ServerSettingsTab.entries.forEach { tab ->
                        NavigationDrawerItem(
                            label = { Text(tab.name) },
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            shape = MaterialTheme.shapes.medium
                        )
                    }
                }

                VerticalDivider(modifier = Modifier.fillMaxHeight().width(1.dp))

                Column(modifier = Modifier.weight(1f).padding(start = 24.dp)) {
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        if (errorMessage != null) {
                            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                        }

                        when (selectedTab) {
                            ServerSettingsTab.Overview -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Overview Placeholder", style = MaterialTheme.typography.headlineSmall)
                                }
                            }
                            ServerSettingsTab.Channels -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Channel Management Placeholder", style = MaterialTheme.typography.headlineSmall)
                                }
                            }
                            ServerSettingsTab.Roles -> {
                                RolesAndPermissionsTab(
                                    state = rolesState,
                                    allServerMembers = members,
                                    onAddRole = { isAddingRole = true },
                                    onSaveChanges = { role, newPermissions, newPriority ->
                                        scope.launch {
                                            try {
                                                val adminId = MainState.currentUserId ?: return@launch
                                                apiClient.updateRole(
                                                    serverId = server.id,
                                                    userId = adminId,
                                                    roleId = role.id,
                                                    permissions = newPermissions,
                                                    priority = newPriority
                                                )
                                                refreshData()
                                            } catch (e: Exception) {
                                                errorMessage = "Failed to save changes: ${e.message}"
                                            }
                                        }
                                    },
                                    onAssignRole = { role, user ->
                                        scope.launch {
                                            try {
                                                val adminId = MainState.currentUserId ?: return@launch
                                                apiClient.assignRole(server.id, adminId, user.id, role.id)
                                                val updatedMembers = apiClient.getRoleMembers(server.id, role.id)
                                                rolesState.updateRoleMembers(updatedMembers)
                                            } catch (e: Exception) {
                                                errorMessage = "Failed to assign role: ${e.message}"
                                            }
                                        }
                                    },
                                    onRemoveRole = { role, user ->
                                        scope.launch {
                                            try {
                                                val adminId = MainState.currentUserId ?: return@launch
                                                apiClient.removeRole(server.id, adminId, user.id, role.id)
                                                val updatedMembers = apiClient.getRoleMembers(server.id, role.id)
                                                rolesState.updateRoleMembers(updatedMembers)
                                            } catch (e: Exception) {
                                                errorMessage = "Failed to remove role: ${e.message}"
                                            }
                                        }
                                    }
                                )
                            }
                            ServerSettingsTab.Members -> {
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
            }
        },
        confirmButton = {},
        dismissButton = {}
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
    var priorityText by remember { mutableStateOf("0") }
    
    val isPriorityValid = priorityText.toIntOrNull() in 0..100
    val canCreate = name.isNotBlank() && isPriorityValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Role") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Role Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = priorityText,
                    onValueChange = { priorityText = it },
                    label = { Text("Priority Level") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isPriorityValid,
                    supportingText = {
                        if (!isPriorityValid) {
                            Text(
                                "Priority must be between 0 and 100",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
                Spacer(Modifier.height(16.dp))
                Text("Permissions", style = MaterialTheme.typography.titleSmall)
                PermissionList(
                    permissions = permissions,
                    onPermissionChange = { permission, enabled ->
                        permissions = website.woodendoor.conflux.models.ConfluxPermission.setPermission(permissions, permission, enabled)
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(name, permissions, priorityText.toIntOrNull() ?: 0) },
                enabled = canCreate
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
