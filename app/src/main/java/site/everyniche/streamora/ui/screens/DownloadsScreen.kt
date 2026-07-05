package site.everyniche.streamora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import site.everyniche.streamora.AppContainer
import site.everyniche.streamora.data.model.ContentItem
import site.everyniche.streamora.ui.components.LoadingSpinner
import site.everyniche.streamora.ui.theme.*

/**
 * Real offline downloads require native filesystem write + a background
 * download manager, which is a native-app feature that will be implemented
 * as a follow-up. For now this screen honestly shows the user's saved
 * watchlist so they can jump straight to a title.
 */
@Composable
fun DownloadsScreen(container: AppContainer, onSelect: (ContentItem) -> Unit) {
    var items by remember { mutableStateOf<List<ContentItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        items = container.supabaseData.getWatchlist()
        loading = false
    }

    Column(modifier = Modifier.fillMaxSize().background(BgDark)) {
        Column(modifier = Modifier.padding(top = 52.dp, start = 20.dp, end = 20.dp, bottom = 16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Downloads", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                if (items.isNotEmpty()) {
                    Text(
                        "${items.size} saved", color = AccentBlue, fontSize = 12.sp,
                        modifier = Modifier.background(AccentBlue.copy(alpha = 0.12f), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth().background(AccentBlue.copy(alpha = 0.08f), RoundedCornerShape(14.dp)).padding(14.dp),
            ) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Offline downloads are coming soon.", color = Color(0xFFDBEAFE), fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                    Text("Below is your saved list — tap to jump right in.", color = Color(0xFF93C5FD), fontSize = 11.5.sp)
                }
            }
        }

        when {
            loading -> LoadingSpinner(modifier = Modifier.fillMaxWidth().padding(top = 40.dp))
            items.isEmpty() -> EmptyDownloads()
            else -> LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp), modifier = Modifier.fillMaxSize()) {
                items(items, key = { it.stableId }) { item -> SavedRow(item, onClick = { onSelect(item) }) }
                item { Spacer(Modifier.height(120.dp)) }
            }
        }
    }
}

@Composable
private fun SavedRow(item: ContentItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x08FFFFFF))
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = item.poster, contentDescription = item.title, contentScale = ContentScale.Crop,
            modifier = Modifier.size(width = 72.dp, height = 54.dp).clip(RoundedCornerShape(10.dp)).background(BgCard),
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(if (item.isTv) "Series" else "Movie", color = TextMuted, fontSize = 11.sp)
        }
        Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF4B5563), modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun EmptyDownloads() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(72.dp).clip(RoundedCornerShape(22.dp)).background(AccentBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Filled.Download, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(30.dp)) }
        Spacer(Modifier.height(18.dp))
        Text("Your list is empty", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            "Tap \"+\" on any title to save it here.",
            color = TextMuted, fontSize = 13.sp,
        )
    }
}
