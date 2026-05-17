package website.woodendoor.conflux.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import website.woodendoor.conflux.models.ConfluxPermission
import website.woodendoor.conflux.models.Role
import website.woodendoor.conflux.models.User

class RolesAndPermissionsState(
    initialRoles: List<Role> = emptyList()
) {
    var roles by mutableStateOf(initialRoles.sortedByDescending { it.priorityLevel })
    var selectedRole by mutableStateOf<Role?>(null)
        private set

    var pendingPermissions by mutableStateOf<Long?>(null)
        private set

    var pendingPriorityText by mutableStateOf("")
        private set

    var roleMembers by mutableStateOf<List<User>>(emptyList())
        private set

    val isPriorityValid: Boolean
        get() = if (selectedRole?.priorityLevel == -1) true 
                else pendingPriorityText.toIntOrNull() in 0..100

    val isEveryoneSelected: Boolean
        get() = selectedRole?.priorityLevel == -1

    val hasChanges: Boolean
        get() = (pendingPermissions != null && pendingPermissions != selectedRole?.permissions) ||
                (selectedRole?.priorityLevel != -1 && pendingPriorityText.toIntOrNull() != selectedRole?.priorityLevel)

    val canSave: Boolean
        get() = hasChanges && isPriorityValid

    fun selectRole(role: Role?) {
        selectedRole = role
        pendingPermissions = role?.permissions
        pendingPriorityText = role?.priorityLevel?.toString() ?: ""
        roleMembers = emptyList()
    }
    
    fun updateRoleMembers(members: List<User>) {
        roleMembers = members
    }

    fun updateRoles(newRoles: List<Role>) {
        roles = newRoles.sortedByDescending { it.priorityLevel }
        // Maintain selection if possible
        selectedRole = roles.find { it.id == selectedRole?.id }
        if (pendingPermissions == null) {
            pendingPermissions = selectedRole?.permissions
        }
        // If we haven't started editing, update the priority text to match new role data
        if (!hasChanges) {
            pendingPriorityText = selectedRole?.priorityLevel?.toString() ?: ""
        }
    }

    fun updatePendingPermission(permission: Long, enabled: Boolean) {
        val current = pendingPermissions ?: selectedRole?.permissions ?: return
        pendingPermissions = ConfluxPermission.setPermission(current, permission, enabled)
    }

    fun updatePendingPriority(priority: String) {
        pendingPriorityText = priority
    }

    fun revertChanges() {
        pendingPermissions = selectedRole?.permissions
        pendingPriorityText = selectedRole?.priorityLevel?.toString() ?: ""
    }
}
