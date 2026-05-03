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
    var roles by mutableStateOf(initialRoles)
    var selectedRole by mutableStateOf<Role?>(null)
        private set

    var pendingPermissions by mutableStateOf<Long?>(null)
        private set

    var roleMembers by mutableStateOf<List<User>>(emptyList())
        private set

    val hasChanges: Boolean
        get() = pendingPermissions != null && pendingPermissions != selectedRole?.permissions

    fun selectRole(role: Role?) {
        selectedRole = role
        pendingPermissions = role?.permissions
        roleMembers = emptyList()
    }
    
    fun updateRoleMembers(members: List<User>) {
        roleMembers = members
    }

    fun updateRoles(newRoles: List<Role>) {
        roles = newRoles
        // Maintain selection if possible
        selectedRole = roles.find { it.id == selectedRole?.id }
        if (pendingPermissions == null) {
            pendingPermissions = selectedRole?.permissions
        }
    }

    fun updatePendingPermission(permission: Long, enabled: Boolean) {
        val current = pendingPermissions ?: selectedRole?.permissions ?: return
        pendingPermissions = ConfluxPermission.setPermission(current, permission, enabled)
    }

    fun revertChanges() {
        pendingPermissions = selectedRole?.permissions
    }
}
