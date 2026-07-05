package site.everyniche.streamora.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BgDark = Color(0xFF090B10)
val BgCard = Color(0xFF111827)
val AccentBlue = Color(0xFF3B82F6)
val AccentBlueDark = Color(0xFF1D4ED8)
val AccentGold = Color(0xFFFBBF24)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF9CA3AF)
val TextMuted = Color(0xFF6B7280)
val BorderColor = Color(0x14FFFFFF)

private val StreamoraColorScheme = darkColorScheme(
    primary = AccentBlue,
    secondary = AccentBlueDark,
    background = BgDark,
    surface = BgCard,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

@Composable
fun StreamoraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StreamoraColorScheme,
        content = content,
    )
}
