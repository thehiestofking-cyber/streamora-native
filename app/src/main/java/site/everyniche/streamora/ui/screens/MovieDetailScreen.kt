package site.everyniche.streamora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import site.everyniche.streamora.AppContainer
import site.everyniche.streamora.data.model.CastMember
import site.everyniche.streamora.data.model.ContentItem
import site.everyniche.streamora.ui.theme.*

@Composable
fun MovieDetailScreen(
    container: AppContainer,
    baseItem: ContentItem,
    onBack: () -> Unit,
    onPlay: (ContentItem) -> Unit,
) {
    var item by remember(baseItem.stableId) { mutableStateOf(baseItem) }
    var cast by remember(baseItem.stableId) { mutableStateOf<List<CastMember>>(emptyList()) }
    var inList by remember(baseItem.stableId) { mutableStateOf(false) }
    var tab by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val scroll = rememberScrollState()

    LaunchedEffect(baseItem.stableId) {
        item = baseItem
        baseItem.detailPath?.let { path ->
            container.repository.getDetails(path)?.let { details ->
                cast = details.stars.orEmpty()
                item = item.copy(description = details.subject?.description ?: item.description)
            }
        }
        baseItem.subjectId?.let { sid ->
            inList = container.supabaseData.isInWatchlist(sid)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(scroll)) {
            Box(modifier = Modifier.fillMaxWidth().height(360.dp)) {
                AsyncImage(
                    model = item.backdrop ?: item.poster, contentDescription = item.title,
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color(0x33090B10), Color(0x80090B10), BgDark))
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 52.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    RoundBtn(Icons.Filled.ArrowBack) { onBack() }
                    RoundBtn(Icons.Filled.Share) {}
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(58.dp)
                        .clip(RoundedCornerShape(50))
                        .background(AccentBlue.copy(alpha = 0.9f))
                        .clickable { onPlay(item) },
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(26.dp)) }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp).offset(y = (-10).dp)) {
                Row {
                    item.genres.forEach { g ->
                        Box(
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .background(AccentBlue.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) { Text(g, color = Color(0xFF60A5FA), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(item.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    item.ratingText?.let { MetaChip(Icons.Filled.Star, "$it IMDb", AccentGold) }
                    item.year?.let { MetaChip(Icons.Filled.CalendarToday, it.toString(), TextSecondary) }
                    item.durationText?.let { MetaChip(Icons.Filled.AccessTime, it, TextSecondary) }
                }
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onPlay(item) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Watch Now", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (inList) AccentBlue.copy(alpha = 0.2f) else Color(0x0FFFFFFF))
                            .clickable {
                                scope.launch {
                                    val sid = item.subjectId ?: return@launch
                                    inList = !inList
                                    val result = if (inList) {
                                        container.supabaseData.addToWatchlist(item)
                                    } else {
                                        container.supabaseData.removeFromWatchlist(sid)
                                    }
                                    result.onFailure { inList = !inList }
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            if (inList) Icons.Filled.Check else Icons.Filled.Add,
                            contentDescription = "Watchlist",
                            tint = if (inList) Color(0xFF60A5FA) else TextSecondary,
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                TabRow(
                    selectedTabIndex = tab,
                    containerColor = Color.Transparent,
                    contentColor = AccentBlue,
                    divider = {},
                ) {
                    listOf("Overview", "Cast").forEachIndexed { i, label ->
                        Tab(
                            selected = tab == i, onClick = { tab = i },
                            text = { Text(label, fontSize = 13.sp, color = if (tab == i) AccentBlue else TextMuted) },
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                if (tab == 0) {
                    Text(item.description ?: "No description available.", color = TextSecondary, fontSize = 14.sp, lineHeight = 22.sp)
                } else {
                    if (cast.isEmpty()) {
                        Text("Cast information not available.", color = TextMuted, fontSize = 13.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            cast.forEach { c ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = c.avatar, contentDescription = c.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(BgCard),
                                    )
                                    Spacer(Modifier.width(14.dp))
                                    Column {
                                        Text(c.name ?: "", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                        c.character?.let { Text(it, color = TextMuted, fontSize = 12.sp) }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(120.dp))
            }
        }
    }
}

@Composable
private fun RoundBtn(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x66000000))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) { Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp)) }
}

@Composable
private fun MetaChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
