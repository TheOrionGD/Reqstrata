package com.theoriongd.reqstrata.ui.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theoriongd.reqstrata.ui.MainViewModel

/**
 * Supported Theme Modes
 */
enum class AppThemeMode(val id: String, val title: String) {
    DARK("dark", "Dark"),
    LIGHT("light", "Light"),
    SYSTEM("system", "System Default");

    companion object {
        fun fromId(id: String?): AppThemeMode {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: DARK
        }
    }
}

/**
 * Distinct Color Palettes for the engineering platform
 */
enum class AppColorPalette(
    val id: String,
    val displayName: String,
    val primaryLight: Color,
    val primaryDark: Color,
    val containerLight: Color,
    val containerDark: Color,
    val accentLight: Color,
    val accentDark: Color
) {
    VIOLET(
        id = "violet",
        displayName = "Violet Horizon",
        primaryLight = Color(0xFF6D28D9),
        primaryDark = Color(0xFF8B5CF6),
        containerLight = Color(0xFFEDE9FE),
        containerDark = Color(0xFF2E1065),
        accentLight = Color(0xFF7C3AED),
        accentDark = Color(0xFFA78BFA)
    ),
    OCEAN(
        id = "ocean",
        displayName = "Cyber Ocean",
        primaryLight = Color(0xFF0284C7),
        primaryDark = Color(0xFF38BDF8),
        containerLight = Color(0xFFE0F2FE),
        containerDark = Color(0xFF0C4A6E),
        accentLight = Color(0xFF0EA5E9),
        accentDark = Color(0xFF7DD3FC)
    ),
    EMERALD(
        id = "emerald",
        displayName = "Emerald Aurora",
        primaryLight = Color(0xFF059669),
        primaryDark = Color(0xFF34D399),
        containerLight = Color(0xFFD1FAE5),
        containerDark = Color(0xFF064E3B),
        accentLight = Color(0xFF10B981),
        accentDark = Color(0xFF6EE7B7)
    ),
    AMBER(
        id = "amber",
        displayName = "Solar Amber",
        primaryLight = Color(0xFFD97706),
        primaryDark = Color(0xFFFBBF24),
        containerLight = Color(0xFFFEF3C7),
        containerDark = Color(0xFF451A03),
        accentLight = Color(0xFFF59E0B),
        accentDark = Color(0xFFFDE68A)
    ),
    ROSE(
        id = "rose",
        displayName = "Electric Rose",
        primaryLight = Color(0xFFE11D48),
        primaryDark = Color(0xFFFB7185),
        containerLight = Color(0xFFFFE4E6),
        containerDark = Color(0xFF4C0519),
        accentLight = Color(0xFFF43F5E),
        accentDark = Color(0xFFFDA4AF)
    );

    companion object {
        fun fromId(id: String?): AppColorPalette {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: VIOLET
        }
    }
}

fun createDarkColorScheme(palette: AppColorPalette = AppColorPalette.VIOLET): ColorScheme = darkColorScheme(
    primary = palette.primaryDark,
    onPrimary = Color.White,
    primaryContainer = palette.containerDark,
    onPrimaryContainer = palette.accentDark,
    secondary = palette.accentLight,
    onSecondary = Color.White,
    secondaryContainer = palette.containerDark,
    onSecondaryContainer = palette.accentDark,
    tertiary = DarkAccent,
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = DarkWarningContainer,
    onTertiaryContainer = DarkWarning,
    error = DarkError,
    onError = Color.White,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkError,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkElevatedSurface,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = Color(0xFF27272A)
)

fun createLightColorScheme(palette: AppColorPalette = AppColorPalette.VIOLET): ColorScheme = lightColorScheme(
    primary = palette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = palette.containerLight,
    onPrimaryContainer = palette.primaryLight,
    secondary = palette.accentLight,
    onSecondary = Color.White,
    secondaryContainer = palette.containerLight,
    onSecondaryContainer = palette.primaryDark,
    tertiary = SecondaryAmber,
    onTertiary = Color.White,
    tertiaryContainer = LightWarningContainer,
    onTertiaryContainer = LightWarning,
    error = LightError,
    onError = Color.White,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightError,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightElevatedSurface,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = LightDivider
)

val DarkColorScheme = createDarkColorScheme(AppColorPalette.VIOLET)
val LightColorScheme = createLightColorScheme(AppColorPalette.VIOLET)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    colorPalette: AppColorPalette = AppColorPalette.VIOLET,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> createDarkColorScheme(colorPalette)
        else -> createLightColorScheme(colorPalette)
    }
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

/**
 * Universal theme toggle button for screens to allow one-tap Dark/Light theme switching
 */
@Composable
fun ThemeToggleIconButton(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isDark by viewModel.isDarkMode.collectAsState()
    IconButton(
        onClick = { viewModel.toggleDarkMode() },
        modifier = modifier.testTag("app_theme_toggle_btn")
    ) {
        Icon(
            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
            contentDescription = if (isDark) "Switch to Light Mode" else "Switch to Dark Mode",
            tint = if (isDark) Color(0xFFFBBF24) else MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Quick theme customization dialog accessible anywhere in the app
 */
@Composable
fun ThemeSelectionDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val isDark by viewModel.isDarkMode.collectAsState()
    val dynamicColors by viewModel.useDynamicColors.collectAsState()
    val currentPalette by viewModel.selectedColorPalette.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Customize Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Dark / Light Mode Switch
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Theme Mode", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                if (isDark) "Dark Slate Engineering" else "Bright Crisp Studio",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isDark,
                            onCheckedChange = { viewModel.toggleDarkMode() }
                        )
                    }
                }

                // Dynamic Colors
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Dynamic Wallpaper", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Material You (Android 12+)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = dynamicColors,
                            onCheckedChange = { viewModel.toggleDynamicColors() }
                        )
                    }
                }

                // Color Palette Chooser
                Text("Accent Color Palette", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Dynamic Wallpaper Accent Option
                    val isDynamicSelected = dynamicColors && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S
                    Surface(
                        color = if (isDynamicSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        border = if (isDynamicSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setDynamicColors(true) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Dynamic Wallpaper Accent",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isDynamicSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        "Synchronized with Android OS Wallpaper",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (isDynamicSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    AppColorPalette.entries.forEach { palette ->
                        val isSelected = !dynamicColors && currentPalette == palette
                        Surface(
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setColorPalette(palette) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(if (isDark) palette.primaryDark else palette.primaryLight)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        palette.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
