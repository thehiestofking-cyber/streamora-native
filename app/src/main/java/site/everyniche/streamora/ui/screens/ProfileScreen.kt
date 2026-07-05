package site.everyniche.streamora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import site.everyniche.streamora.AppContainer
import site.everyniche.streamora.ui.theme.*

@Composable
fun ProfileScreen(container: AppContainer, onLogout: () -> Unit) {
    val user by container.auth.currentUserFlow.collectAsState(initial = null)
    var watchlistCount by remember { mutableIntStateOf(0) }
    var historyCount by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(user?.id) {
        watchlistCount = container.supabaseData.getWatchlist().size
        historyCount = container.supabaseData.getHistory(50).size
    }

    val name = user?.displayName ?: "Guest"
    val initial = name.firstOrNull()?.uppercaseChar() ?: 'G'

    Column(modifier = Modifier.fillMaxSize().background(BgDark)) {
        Column(modifier = Modifier.padding(top = 52.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Brush.linearGradient(listOf(AccentBlue, AccentBlueDark))),
                    contentAlignment = Alignment.Center,
                ) { Text(initial.toString(), color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold) }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(user?.email ?: "Not signed in", color = TextMuted, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0x0AFFFFFF)),
            ) {
                StatBox("Watchlist", watchlistCount.toString(), Modifier.weight(1f))
                StatBox("History", historyCount.toString(), Modifier.weight(1f))
            }
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp).weight(1f)) {
            SettingsSection(title = "Account") {
                SettingsRow("Notifications", "On")
                SettingsRow("Privacy & Security", null)
            }
            SettingsSection(title = "Preferences") {
                SettingsRow("Language", "English")
                SettingsRow("Theme", "Dark")
            }
            SettingsSection(title = "Support") {
                SettingsRow("Help Center", null)
                SettingsRow("Rate Streamora", null)
            }

            Button(
                onClick = { scope.launch { container.auth.signOut(); onLogout() } },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x1AEF4444), contentColor = Color(0xFFEF4444)),
            ) {
                Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sign Out", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(20.dp))
            Text("Streamora v1.0", color = Color(0xFF374151), fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextMuted, fontSize = 11.sp)
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Text(title.uppercase(), color = Color(0xFF4B5563), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp)
        Spacer(Modifier.height(10.dp))
        Column(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Color(0x08FFFFFF))) { content() }
    }
}

@Composable
private fun SettingsRow(label: String, value: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = Color(0xFFE5E7EB), fontSize = 14.sp)
        if (value != null) Text(value, color = TextMuted, fontSize = 13.sp)
    }
}
