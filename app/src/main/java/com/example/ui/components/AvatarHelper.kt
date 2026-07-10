package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AvatarConfig(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val startColor: Color,
    val endColor: Color
)

object AvatarRegistry {
    val avatars = listOf(
        AvatarConfig("avatar_beast", "Beast Mode", Icons.Filled.FitnessCenter, Color(0xFFF97316), Color(0xFFEF4444)),
        AvatarConfig("avatar_yoga", "Yogi", Icons.Filled.SelfImprovement, Color(0xFF10B981), Color(0xFF06B6D4)),
        AvatarConfig("avatar_power", "Powerlifter", Icons.Filled.SportsGymnastics, Color(0xFF6366F1), Color(0xFF8B5CF6)),
        AvatarConfig("avatar_cardio", "Cardio Pro", Icons.Filled.DirectionsRun, Color(0xFFEC4899), Color(0xFFF43F5E)),
        AvatarConfig("avatar_flex", "Bodybuilder", Icons.Filled.AccessibilityNew, Color(0xFFF59E0B), Color(0xFFD97706)),
        AvatarConfig("avatar_speed", "Athlete", Icons.Filled.LightningToot, Color(0xFF3B82F6), Color(0xFF2563EB)) // fallback to Bolt or similar
    )

    fun get(id: String): AvatarConfig {
        return avatars.find { it.id == id } ?: avatars[0]
    }
}

// Custom fallback to handle icons not present or customized
val Icons.Filled.LightningToot: ImageVector get() = Icons.Filled.Bolt

@Composable
fun GymAvatar(
    avatarId: String,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    showBorder: Boolean = false,
    borderColor: Color = MaterialTheme.colorScheme.primary
) {
    val config = AvatarRegistry.get(avatarId)
    val gradient = Brush.linearGradient(listOf(config.startColor, config.endColor))

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(gradient)
            .then(
                if (showBorder) Modifier.border(2.5.dp, borderColor, CircleShape)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = config.icon,
            contentDescription = config.name,
            tint = Color.White,
            modifier = Modifier.size(size * 0.55f)
        )
    }
}
