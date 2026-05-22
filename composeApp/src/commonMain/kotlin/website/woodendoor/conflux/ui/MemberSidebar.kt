package website.woodendoor.conflux.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import website.woodendoor.conflux.api.ServerApiClient
import website.woodendoor.conflux.models.User
import website.woodendoor.conflux.state.MainState

@Composable
fun MemberSidebar(apiClient: ServerApiClient) {
    val members = MainState.currentServerMembers
    val onlineMembers = members.filter { it.isOnline }.sortedBy { it.username.lowercase() }
    val offlineMembers = members.filter { !it.isOnline }.sortedBy { it.username.lowercase() }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(240.dp)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(0.dp)
            )
            .padding(vertical = 16.dp, horizontal = 12.dp)
    ) {
        Text(
            text = "Members",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 12.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Online Section
            if (onlineMembers.isNotEmpty()) {
                item {
                    SectionHeader(title = "Online", count = onlineMembers.size)
                }
                items(onlineMembers) { member ->
                    MemberRow(member = member)
                }
            }

            // Space between sections
            if (onlineMembers.isNotEmpty() && offlineMembers.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Offline Section
            if (offlineMembers.isNotEmpty()) {
                item {
                    SectionHeader(title = "Offline", count = offlineMembers.size)
                }
                items(offlineMembers) { member ->
                    MemberRow(member = member)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, count: Int) {
    Text(
        text = "${title.uppercase()} — $count",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
    )
}

@Composable
fun MemberRow(member: User) {
    Surface(
        onClick = {},
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Container with status badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            ) {
                // Gradient initial avatar
                val gradientColors = getAvatarGradient(member.id)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(gradientColors)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.username.take(1).uppercase(),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Status Dot overlay next to Avatar (like discord)
            Spacer(modifier = Modifier.width(4.dp))
            if (member.isOnline) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFF4CAF50), CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Username & Discriminator
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = member.username,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (member.isOnline) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Text(
                    text = "#${member.discriminator}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                )
            }
        }
    }
}

fun getAvatarGradient(userId: String): List<Color> {
    val hash = userId.hashCode()
    val gradients = listOf(
        listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)), // Royal purple
        listOf(Color(0xFFFF0844), Color(0xFFFFB199)), // Warm sunset
        listOf(Color(0xFF1D976C), Color(0xFF93F9B9)), // Emerald mint
        listOf(Color(0xFFF7971E), Color(0xFFFFD200)), // Golden amber
        listOf(Color(0xFF00c6ff), Color(0xFF0072ff)), // Electric blue
        listOf(Color(0xFFe65c00), Color(0xFFF9D423)), // Mandarin sunset
        listOf(Color(0xFF2193b0), Color(0xFF6dd5ed)), // Cyan pool
        listOf(Color(0xFFee9ca7), Color(0xFFffdde1))  // Blossom pink
    )
    val index = kotlin.math.abs(hash) % gradients.size
    return gradients[index]
}
