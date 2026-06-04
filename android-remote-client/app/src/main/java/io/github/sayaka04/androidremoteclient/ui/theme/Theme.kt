package io.github.sayaka04.androidremoteclient.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// ==========================================
// 1. ZINC COLOR PALETTE (Shadcn aesthetic)
// ==========================================
val Zinc50 = Color(0xFFFAFAFA)
val Zinc100 = Color(0xFFF4F4F5)
val Zinc200 = Color(0xFFE4E4E7)
val Zinc300 = Color(0xFFD4D4D8)
val Zinc800 = Color(0xFF27272A)
val Zinc900 = Color(0xFF18181B)
val Zinc950 = Color(0xFF09090B)

private val LightColorScheme = lightColorScheme(
    background = Color.White,
    surface = Color.White,
    surfaceVariant = Zinc100,
    onBackground = Zinc950,
    onSurface = Zinc950,
    onSurfaceVariant = Zinc800,
    primary = Zinc900,
    onPrimary = Zinc50,
    secondary = Zinc200,
    onSecondary = Zinc900,
    outline = Zinc200,
    outlineVariant = Zinc300,
    error = Color(0xFFEF4444),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    background = Zinc950,
    surface = Zinc950,
    surfaceVariant = Zinc800,
    onBackground = Zinc50,
    onSurface = Zinc50,
    onSurfaceVariant = Zinc300,
    primary = Zinc50,
    onPrimary = Zinc900,
    secondary = Zinc800,
    onSecondary = Zinc50,
    outline = Zinc800,
    outlineVariant = Zinc800,
    error = Color(0xFF7F1D1D),
    onError = Color.White
)

// ==========================================
// 2. SHAPES (Sharp, subtle curves)
// ==========================================
val ShadcnShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(8.dp)
)

// ==========================================
// 3. TYPOGRAPHY (Clean and readable)
// ==========================================
val ShadcnTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        letterSpacing = (-0.5).sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        letterSpacing = (-0.5).sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
    )
)

// ==========================================
// 4. MAIN THEME COMPOSABLE
// ==========================================
@Composable
fun ShadcnTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    // Handles coloring the system status bar at the top of the phone
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ShadcnTypography,
        shapes = ShadcnShapes,
        content = content
    )
}