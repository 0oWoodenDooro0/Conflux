package website.woodendoor.conflux.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import website.woodendoor.conflux.models.Role

class RolesAndPermissionsState(
    initialRoles: List<Role> = emptyList()
) {
    var roles by mutableStateOf(initialRoles)
    var selectedRole by mutableStateOf<Role?>(null)
        private set

    fun selectRole(role: Role?) {
        selectedRole = role
    }
    
    fun updateRoles(newRoles: List<Role>) {
        roles = newRoles
        // Maintain selection if possible
        selectedRole = roles.find { it.id == selectedRole?.id }
    }
}
