package website.woodendoor.conflux.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import website.woodendoor.conflux.models.Role

@Composable
fun RolesAndPermissionsTab(
    state: RolesAndPermissionsState,
    onAddRole: () -> Unit
) {
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
                items(state.roles) { role ->
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
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        VerticalDivider(modifier = Modifier.fillMaxHeight().width(1.dp))

        // Right Column: Role Details
        Column(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .padding(start = 16.dp)
        ) {
            if (state.selectedRole == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Select a role to view details", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                Text(
                    text = "Role: ${state.selectedRole!!.name}",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Tabs for Permissions and Members will go here in later tasks
                Text("Role details will be implemented in subsequent tasks.")
            }
        }
    }
}
