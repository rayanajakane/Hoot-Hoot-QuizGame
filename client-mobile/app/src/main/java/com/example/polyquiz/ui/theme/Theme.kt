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
    DARK(StringValue.StringResource(R.string.dark_theme))
    // TODO : Add more themes
}

// Ref : https://lh3.googleusercontent.com/2tz16tRWvWsNwxg22BmLGDpZ7Pp3SqAg6Zr10WUCbjeEJHFSBzkbb4gwkjIQ5m4rfYoSntR412o7bC7sr8Xi5-gpiciNiQyjssfNWQ=w1064-v0
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF3700B3),
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
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

@Composable
fun PolyQuizTheme(
    // Ref : https://developer.android.com/reference/kotlin/androidx/compose/foundation/package-summary#isSystemInDarkTheme()
    currentTheme: Theme,
    content: @Composable () -> Unit
) {
    val colorScheme = when (currentTheme) {
        Theme.DARK -> DarkColorScheme
        Theme.LIGHT -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
