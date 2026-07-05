package site.everyniche.streamora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import site.everyniche.streamora.ui.theme.AccentBlue
import site.everyniche.streamora.ui.theme.BgDark
import site.everyniche.streamora.ui.theme.TextMuted

private data class OnboardPage(val emoji: String, val title: String, val body: String)

private val PAGES = listOf(
    OnboardPage("🎬", "Endless movies & series", "Discover trending titles across every genre, updated daily."),
    OnboardPage("⚡", "Stream instantly", "Play anything in seconds, right from your device."),
    OnboardPage("❤️", "Made for you", "Build your watchlist and pick up right where you left off."),
)

@Composable
fun OnboardingScreenSimple(onComplete: () -> Unit) {
    var page by remember { mutableIntStateOf(0) }
    val current = PAGES[page]

    Column(
        modifier = Modifier.fillMaxSize().background(BgDark).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(current.emoji, fontSize = 56.sp)
        Spacer(Modifier.height(24.dp))
        Text(current.title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text(current.body, color = TextMuted, fontSize = 14.sp)
        Spacer(Modifier.height(36.dp))
        Row {
            PAGES.forEachIndexed { i, _ ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(6.dp)
                        .width(if (i == page) 22.dp else 6.dp)
                        .background(if (i == page) AccentBlue else Color(0x33FFFFFF), RoundedCornerShape(3.dp)),
                )
            }
        }
        Spacer(Modifier.height(36.dp))
        Button(
            onClick = { if (page < PAGES.lastIndex) page++ else onComplete() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
        ) {
            Text(if (page < PAGES.lastIndex) "Next" else "Get Started", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
