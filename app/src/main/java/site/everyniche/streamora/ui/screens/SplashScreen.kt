package site.everyniche.streamora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import site.everyniche.streamora.ui.theme.AccentBlue
import site.everyniche.streamora.ui.theme.AccentBlueDark
import site.everyniche.streamora.ui.theme.BgDark

@Composable
fun SplashScreen(onComplete: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1200)
        onComplete()
    }
    Box(modifier = Modifier.fillMaxSize().background(BgDark), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(AccentBlue, AccentBlueDark))),
                contentAlignment = Alignment.Center,
            ) {
                Text("▶", color = Color.White, fontSize = 28.sp)
            }
            Spacer(Modifier.height(16.dp))
            Row {
                Text("STREAM", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("ORA", color = AccentBlue, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
