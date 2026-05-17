package website.woodendoor.conflux.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import website.woodendoor.conflux.api.ServerApiClient
import website.woodendoor.conflux.models.*
import website.woodendoor.conflux.state.MainState
import website.woodendoor.conflux.validation.ChannelValidator
import website.woodendoor.conflux.validation.ValidationResult

@Composable
fun ChannelSettingsDialog(
    channel: Channel,
    apiClient: ServerApiClient,
    onDismissRequest: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Permissions")

    AlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxSize().padding(16.dp),
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Channel Settings: #${channel.name}")
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        },
        text = {
            Box(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
                when (selectedTab) {
                    0 -> ChannelOverviewTab(channel, apiClient, onDismissRequest)
                    1 -> ChannelPermissionsTab(channel, apiClient)
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@Composable
fun ChannelOverviewTab(
    channel: Channel,
    apiClient: ServerApiClient,
    onDismissRequest: () -> Unit
) {
    var name by remember { mutableStateOf(channel.name) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var isDeleting by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
        ) {
            if (errorMessage != null) {
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text("Channel Name", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = name,
                onValueChange = {
                    name = it
                    validationError = when (val result = ChannelValidator.validateName(it)) {
                        is ValidationResult.Error -> result.message
                        ValidationResult.Success -> null
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("new-channel-name") },
                isError = validationError != null,
                supportingText = {
                    if (validationError != null) {
                        Text(validationError!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        try {
                            val userId = MainState.currentUserId ?: return@launch
                            apiClient.updateChannel(
                                serverId = channel.serverId,
                                channelId = channel.id,
                                userId = userId,
                                name = name
                            )
                            onDismissRequest()
                        } catch (e: Exception) {
                            errorMessage = "Failed to update channel: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = name.isNotBlank() && name != channel.name && !isLoading && validationError == null,
                modifier = Modifier.align(Alignment.End)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Save Changes")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            HorizontalDivider()
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Danger Zone", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { isDeleting = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Channel")
            }
        }
    }

    if (isDeleting) {
        AlertDialog(
            onDismissRequest = { isDeleting = false },
            title = { Text("Delete Channel") },
            text = { Text("Are you sure you want to delete #${channel.name}? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                val userId = MainState.currentUserId ?: return@launch
                                val success = apiClient.deleteChannel(channel.serverId, channel.id, userId)
                                if (success) {
                                    onDismissRequest()
                                } else {
                                    errorMessage = "Failed to delete channel"
                                }
                            } catch (e: Exception) {
                                errorMessage = "Error: ${e.message}"
                            } finally {
                                isLoading = false
                                isDeleting = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { isDeleting = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ChannelPermissionsTab(
    channel: Channel,
    apiClient: ServerApiClient
) {
    var overrides by remember { mutableStateOf<List<ChannelPermissionOverride>>(emptyList()) }
    var roles by remember { mutableStateOf<List<Role>>(emptyList()) }
    var members by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedOverride by remember { mutableStateOf<ChannelPermissionOverride?>(null) }
    var isAddingOverride by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(channel.id) {
        scope.launch {
            try {
                overrides = apiClient.getChannelOverrides(channel.serverId, channel.id)
                roles = apiClient.getRoles(channel.serverId)
                members = apiClient.getMembers(channel.serverId)
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left side: Override list
            Column(modifier = Modifier.width(250.dp).fillMaxHeight().padding(end = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Overrides", style = MaterialTheme.typography.titleSmall)
                    IconButton(onClick = { isAddingOverride = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Override")
                    }
                }
                
                LazyColumn {
                    items(overrides) { override ->
                        val label = when (override.targetType) {
                            OverrideType.ROLE -> roles.find { it.id == override.targetId }?.name ?: "Unknown Role"
                            OverrideType.USER -> members.find { it.id == override.targetId }?.username ?: "Unknown User"
                        }
                        
                        NavigationDrawerItem(
                            label = { Text(label) },
                            selected = selectedOverride?.id == override.id,
                            onClick = { selectedOverride = override },
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }

            VerticalDivider()

            // Right side: Permission editor
            Column(modifier = Modifier.weight(1f).fillMaxHeight().padding(start = 16.dp)) {
                if (selectedOverride != null) {
                    val override = selectedOverride!!
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Permissions for ${
                            when (override.targetType) {
                                OverrideType.ROLE -> roles.find { it.id == override.targetId }?.name ?: "Unknown Role"
                                OverrideType.USER -> members.find { it.id == override.targetId }?.username ?: "Unknown User"
                            }
                        }", style = MaterialTheme.typography.titleMedium)
                        
                        IconButton(onClick = {
                            scope.launch {
                                val userId = MainState.currentUserId ?: return@launch
                                if (apiClient.deleteChannelOverride(channel.serverId, channel.id, userId, override.id)) {
                                    overrides = overrides.filter { it.id != override.id }
                                    selectedOverride = null
                                }
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Override", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    PermissionOverrideRow(
                        name = "Send Messages",
                        permission = ConfluxPermission.MESSAGING,
                        allow = override.allow,
                        deny = override.deny,
                        onChanged = { newAllow, newDeny ->
                            scope.launch {
                                val userId = MainState.currentUserId ?: return@launch
                                val request = UpsertOverrideRequest(override.targetId, override.targetType, newAllow, newDeny)
                                if (apiClient.upsertChannelOverride(channel.serverId, channel.id, userId, request)) {
                                    overrides = apiClient.getChannelOverrides(channel.serverId, channel.id)
                                    selectedOverride = overrides.find { it.targetId == override.targetId && it.targetType == override.targetType }
                                }
                            }
                        }
                    )

                    PermissionOverrideRow(
                        name = "Manage Channel",
                        permission = ConfluxPermission.CHANNEL_MANAGEMENT,
                        allow = override.allow,
                        deny = override.deny,
                        onChanged = { newAllow, newDeny ->
                            scope.launch {
                                val userId = MainState.currentUserId ?: return@launch
                                val request = UpsertOverrideRequest(override.targetId, override.targetType, newAllow, newDeny)
                                if (apiClient.upsertChannelOverride(channel.serverId, channel.id, userId, request)) {
                                    overrides = apiClient.getChannelOverrides(channel.serverId, channel.id)
                                    selectedOverride = overrides.find { it.targetId == override.targetId && it.targetType == override.targetType }
                                }
                            }
                        }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Select an override to edit permissions", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    if (isAddingOverride) {
        AddOverrideDialog(
            roles = roles.filter { role -> overrides.none { it.targetId == role.id && it.targetType == OverrideType.ROLE } },
            members = members.filter { member -> overrides.none { it.targetId == member.id && it.targetType == OverrideType.USER } },
            onDismiss = { isAddingOverride = false },
            onAdd = { targetId, type ->
                scope.launch {
                    val userId = MainState.currentUserId ?: return@launch
                    val request = UpsertOverrideRequest(targetId, type, 0L, 0L)
                    if (apiClient.upsertChannelOverride(channel.serverId, channel.id, userId, request)) {
                        overrides = apiClient.getChannelOverrides(channel.serverId, channel.id)
                        selectedOverride = overrides.find { it.targetId == targetId && it.targetType == type }
                    }
                    isAddingOverride = false
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionOverrideRow(
    name: String,
    permission: Long,
    allow: Long,
    deny: Long,
    onChanged: (newAllow: Long, newDeny: Long) -> Unit
) {
    val isAllowed = (allow and permission) != 0L
    val isDenied = (deny and permission) != 0L
    
    val state = when {
        isAllowed -> 1 // Allow
        isDenied -> 2 // Deny
        else -> 0 // Inherit
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name)
        
        SingleChoiceSegmentedButtonRow {
            SegmentedButton(
                selected = state == 1,
                onClick = {
                    onChanged(
                        ConfluxPermission.setPermission(allow, permission, true),
                        ConfluxPermission.setPermission(deny, permission, false)
                    )
                },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                colors = SegmentedButtonDefaults.colors(activeContainerColor = Color(0xFF4CAF50))
            ) {
                Text("Allow")
            }
            SegmentedButton(
                selected = state == 0,
                onClick = {
                    onChanged(
                        ConfluxPermission.setPermission(allow, permission, false),
                        ConfluxPermission.setPermission(deny, permission, false)
                    )
                },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
            ) {
                Text("Inherit")
            }
            SegmentedButton(
                selected = state == 2,
                onClick = {
                    onChanged(
                        ConfluxPermission.setPermission(allow, permission, false),
                        ConfluxPermission.setPermission(deny, permission, true)
                    )
                },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                colors = SegmentedButtonDefaults.colors(activeContainerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Deny")
            }
        }
    }
}

@Composable
fun AddOverrideDialog(
    roles: List<Role>,
    members: List<User>,
    onDismiss: () -> Unit,
    onAdd: (String, OverrideType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Permission Override") },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                item { Text("Roles", style = MaterialTheme.typography.labelLarge) }
                items(roles) { role ->
                    TextButton(
                        onClick = { onAdd(role.id, OverrideType.ROLE) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(role.name, modifier = Modifier.fillMaxWidth())
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item { Text("Members", style = MaterialTheme.typography.labelLarge) }
                items(members) { member ->
                    TextButton(
                        onClick = { onAdd(member.id, OverrideType.USER) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(member.username, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
