package com.example.polyquiz.ui.theme

import StringValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.polyquiz.R

enum class Theme(val displayName: StringValue) {
    LIGHT(StringValue.StringResource(R.string.light_theme)),
    DARK(StringValue.StringResource(R.string.dark_theme)),
    LUIGI(StringValue.StringResource(R.string.luigi_theme)),
    MARIO(StringValue.StringResource(R.string.mario_theme)),
    SONIC(StringValue.StringResource(R.string.sonic_theme)),
    PIKACHU(StringValue.StringResource(R.string.pikachu_theme))
}

// Ref : https://lh3.googleusercontent.com/2tz16tRWvWsNwxg22BmLGDpZ7Pp3SqAg6Zr10WUCbjeEJHFSBzkbb4gwkjIQ5m4rfYoSntR412o7bC7sr8Xi5-gpiciNiQyjssfNWQ=w1064-v0
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFACC5FC),
    secondary = Color(0xFF3700B3),
    background = Color(0xFF121212),
    surface = Color(0xFF1A1C22),
    error = Color(0xFFCF6679),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.Black,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF3F51B5),
    secondary = PurpleGrey40,
    background = Color.White,
    surface = Color.White,
    error = Color(0xFFB00020),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onError = Color.White,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

private val LuigiColorScheme = lightColorScheme(
    primary = Color(0xFF33A563),      // Luigi Green
    secondary = Color(0xFF2E7D32),    // Darker Green
    tertiary = Color(0xFF81C784),     // Lighter Green
    background = Color(0xFFE8F5E9),   // Very Light Green
    surface = Color(0xFFC8E6C9),      // Pale Green
    error = Color(0xFFB71C1C),        // Dark Red
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFF1B5E20), // Very Dark Green
    onSurface = Color(0xFF1B5E20),    // Very Dark Green
    onError = Color.White
)

private val MarioColorScheme = lightColorScheme(
    primary = Color(0xFFE53935),      // Mario Red
    secondary = Color(0xFFB71C1C),    // Darker Red
    tertiary = Color(0xFFEF9A9A),     // Light Pink/Red
    background = Color(0xFFFFEBEE),   // Very Light Red
    surface = Color.White,
    error = Color(0xFF4A148C),        // Purple
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFFB71C1C), // Dark Red
    onSurface = Color(0xFFB71C1C),    // Dark Red
    onError = Color.White
)

private val SonicColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),      // Sonic Blue
    secondary = Color(0xFF0D47A1),    // Darker Blue
    tertiary = Color(0xFF90CAF9),     // Light Blue
    background = Color(0xFFE3F2FD),   // Very Light Blue
    surface = Color.White,
    error = Color(0xFFD50000),        // Bright Red
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFF0D47A1), // Dark Blue
    onSurface = Color(0xFF0D47A1),    // Dark Blue
    onError = Color.White
)

private val PikachuColorScheme = lightColorScheme(
    primary = Color(0xFFFFC107),      // Pikachu Yellow
    secondary = Color(0xFFFFA000),    // Darker Yellow
    tertiary = Color(0xFFFFE082),     // Light Yellow
    background = Color(0xFFFFFDE7),   // Very Light Yellow
    surface = Color.White,
    error = Color(0xFF4E342E),        // Brown
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFF212121), // Very Dark Gray
    onSurface = Color(0xFF212121),    // Very Dark Gray
    onError = Color.White
)

@Composable
fun PolyQuizTheme(
    // Ref : https://developer.android.com/reference/kotlin/androidx/compose/foundation/package-summary#isSystemInDarkTheme()
    currentTheme: Theme,
    content: @Composable () -> Unit
) {
    val colorScheme = when (currentTheme) {
        Theme.DARK -> DarkColorScheme
        Theme.LIGHT -> LightColorScheme
        Theme.LUIGI -> LuigiColorScheme
        Theme.MARIO -> MarioColorScheme
        Theme.SONIC -> SonicColorScheme
        Theme.PIKACHU -> PikachuColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
