package website.woodendoor.conflux.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import website.woodendoor.conflux.models.ConfluxPermission
import website.woodendoor.conflux.models.Role
import website.woodendoor.conflux.models.User

@Composable
fun RolesAndPermissionsTab(
    state: RolesAndPermissionsState,
    allServerMembers: List<User>,
    onAddRole: () -> Unit,
    onSaveChanges: (Role, Long, Int) -> Unit,
    onAssignRole: (Role, User) -> Unit,
    onRemoveRole: (Role, User) -> Unit
) {
    val copyToClipboard = rememberClipboardHelper()

    Row(modifier = Modifier.fillMaxSize()) {
        // Left Column: Role List
        Column(
            modifier = Modifier
                .width(240.dp)
                .fillMaxHeight()
                .padding(end = 16.dp)
        ) {
            Button(
                onClick = onAddRole,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add Role")
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                val everyoneRole = state.roles.find { it.priorityLevel == -1 }
                val customRoles = state.roles.filter { it.priorityLevel != -1 }

                if (everyoneRole != null) {
                    item {
                        Text(
                            "Default Role",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 4.dp)
                        )
                        RoleNavigationItem(everyoneRole, state, copyToClipboard)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp))
                    }
                }

                if (customRoles.isNotEmpty()) {
                    item {
                        Text(
                            "Custom Roles",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(customRoles) { role ->
                        RoleNavigationItem(role, state, copyToClipboard)
                    }
                }
            }
        }

        VerticalDivider(modifier = Modifier.fillMaxHeight().width(1.dp))

        // Right Column: Role Details
        Column(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .padding(start = 24.dp)
        ) {
            val selectedRole = state.selectedRole
            if (selectedRole == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Select a role to view details", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                var detailTabIndex by remember(selectedRole.id) { mutableStateOf(0) }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Role: ${selectedRole.name}",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    if (state.hasChanges) {
                        Row {
                            TextButton(onClick = { state.revertChanges() }) {
                                Text("Revert")
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = { 
                                    val perms = state.pendingPermissions ?: selectedRole.permissions
                                    val priority = if (selectedRole.priorityLevel == -1) -1 
                                                   else state.pendingPriorityText.toIntOrNull() ?: selectedRole.priorityLevel
                                    onSaveChanges(selectedRole, perms, priority)
                                },
                                enabled = state.canSave
                            ) {
                                Text("Save Changes")
                            }
                        }
                    }
                }

                TabRow(selectedTabIndex = detailTabIndex, modifier = Modifier.padding(bottom = 16.dp)) {
                    Tab(selected = detailTabIndex == 0, onClick = { detailTabIndex = 0 }, text = { Text("Permissions") })
                    if (selectedRole.priorityLevel != -1) {
                        Tab(selected = detailTabIndex == 1, onClick = { detailTabIndex = 1 }, text = { Text("Members") })
                    }
                }

                when (detailTabIndex) {
                    0 -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            if (selectedRole.priorityLevel != -1) {
                                RoleGeneralSettings(
                                    priorityText = state.pendingPriorityText,
                                    isError = !state.isPriorityValid,
                                    onPriorityChange = { state.updatePendingPriority(it) }
                                )
                            } else {
                                Text(
                                    "This is the default role for everyone. Priority is fixed.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 16.dp, start = 4.dp)
                                )
                            }

                            PermissionList(
                                permissions = state.pendingPermissions ?: selectedRole.permissions,
                                onPermissionChange = { permission, enabled ->
                                    state.updatePendingPermission(permission, enabled)
                                }
                            )
                        }
                    }
                    1 -> {
                        if (selectedRole.priorityLevel != -1) {
                            MemberAssignmentView(
                                allMembers = allServerMembers,
                                roleMembers = state.roleMembers,
                                onAddMember = { onAddMember -> onAssignRole(selectedRole, onAddMember) },
                                onRemoveMember = { onRemoveMember -> onRemoveRole(selectedRole, onRemoveMember) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleNavigationItem(
    role: Role,
    state: RolesAndPermissionsState,
    copyToClipboard: (String) -> Unit
) {
    DebugContextMenu(
        ids = mapOf("Role ID" to role.id),
        onCopy = copyToClipboard
    ) {
        NavigationDrawerItem(
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val roleColor = role.color
                    if (roleColor != null) {
                        Surface(
                            color = Color(roleColor),
                            shape = CircleShape,
                            modifier = Modifier.size(12.dp)
                        ) {}
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(role.name)
                }
            },
            selected = state.selectedRole?.id == role.id,
            onClick = { state.selectRole(role) },
            modifier = Modifier.padding(vertical = 2.dp),
            shape = MaterialTheme.shapes.medium
        )
    }
}

@Composable
fun RoleGeneralSettings(
    priorityText: String,
    isError: Boolean,
    onPriorityChange: (String) -> Unit
) {
    OutlinedTextField(
        value = priorityText,
        onValueChange = onPriorityChange,
        label = { Text("Priority Level") },
        isError = isError,
        supportingText = if (isError) {
            { Text("Priority must be between 0 and 100") }
        } else null,
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
        ),
        singleLine = true
    )
}

@Composable
fun MemberAssignmentView(
    allMembers: List<User>,
    roleMembers: List<User>,
    onAddMember: (User) -> Unit,
    onRemoveMember: (User) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val copyToClipboard = rememberClipboardHelper()

    Column(modifier = Modifier.fillMaxSize()) {
        // Search and Add
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    isDropdownExpanded = it.isNotEmpty()
                },
                label = { Text("Add Member") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = ""; isDropdownExpanded = false }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                singleLine = true
            )

            DropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false },
                properties = PopupProperties(focusable = false),
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                val filteredMembers = allMembers.filter { 
                    it.username.contains(searchQuery, ignoreCase = true) && 
                    roleMembers.none { rm -> rm.id == it.id }
                }
                
                if (filteredMembers.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No members found") },
                        onClick = { isDropdownExpanded = false }
                    )
                } else {
                    filteredMembers.forEach { user ->
                        DropdownMenuItem(
                            text = { Text("${user.username}#${user.discriminator}") },
                            onClick = {
                                onAddMember(user)
                                searchQuery = ""
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Text(
            "Members in this role",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(roleMembers) { member ->
                DebugContextMenu(
                    ids = mapOf("User ID" to member.id),
                    onCopy = copyToClipboard
                ) {
                    ListItem(
                        headlineContent = { Text("${member.username}#${member.discriminator}") },
                        trailingContent = {
                            IconButton(onClick = { onRemoveMember(member) }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionList(
    permissions: Long,
    onPermissionChange: (Long, Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        PermissionToggle(
            name = "View Channel",
            description = "Allows members to view channels and their lists.",
            enabled = ConfluxPermission.hasPermission(permissions, ConfluxPermission.VIEW_CHANNEL),
            onCheckedChange = { onPermissionChange(ConfluxPermission.VIEW_CHANNEL, it) }
        )
        PermissionToggle(
            name = "Messaging",
            description = "Allows members to send messages in channels.",
            enabled = ConfluxPermission.hasPermission(permissions, ConfluxPermission.MESSAGING),
            onCheckedChange = { onPermissionChange(ConfluxPermission.MESSAGING, it) }
        )
        PermissionToggle(
            name = "Channel Management",
            description = "Allows members to create, edit, and delete channels.",
            enabled = ConfluxPermission.hasPermission(permissions, ConfluxPermission.CHANNEL_MANAGEMENT),
            onCheckedChange = { onPermissionChange(ConfluxPermission.CHANNEL_MANAGEMENT, it) }
        )
        PermissionToggle(
            name = "Role Management",
            description = "Allows members to create, edit, and delete roles.",
            enabled = ConfluxPermission.hasPermission(permissions, ConfluxPermission.ROLE_MANAGEMENT),
            onCheckedChange = { onPermissionChange(ConfluxPermission.ROLE_MANAGEMENT, it) }
        )
        PermissionToggle(
            name = "Server Management",
            description = "Allows members to edit server settings.",
            enabled = ConfluxPermission.hasPermission(permissions, ConfluxPermission.SERVER_MANAGEMENT),
            onCheckedChange = { onPermissionChange(ConfluxPermission.SERVER_MANAGEMENT, it) }
        )
    }
}

@Composable
fun PermissionToggle(
    name: String,
    description: String,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(name) },
        supportingContent = { Text(description) },
        trailingContent = {
            Switch(checked = enabled, onCheckedChange = onCheckedChange)
        }
    )
}
