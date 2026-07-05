package site.everyniche.streamora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import site.everyniche.streamora.AppContainer
import site.everyniche.streamora.data.model.ContentItem
import site.everyniche.streamora.ui.theme.*

@Composable
fun BrowseScreen(
    container: AppContainer,
    title: String,
    params: Map<String, String>,
    onBack: () -> Unit,
    onSelect: (ContentItem) -> Unit,
) {
    var items by remember { mutableStateOf<List<ContentItem>>(emptyList()) }
    var page by remember { mutableIntStateOf(1) }
    var loading by remember { mutableStateOf(true) }
    var done by remember { mutableStateOf(false) }
    val seen = remember { mutableSetOf<String>() }
    val gridState = rememberLazyGridState()

    suspend fun loadNext() {
        if (done || loading) return
        loading = true
        val next = container.repository.browse(params, page)
        if (next.isEmpty()) {
            done = true
        } else {
            val fresh = next.filter { seen.add(it.stableId) }
            items = items + fresh
            page += 1
            if (fresh.isEmpty()) done = true
        }
        loading = false
    }

    LaunchedEffect(Unit) { loadNext() }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= items.size - 6 && !done && !loading
        }
    }
    LaunchedEffect(shouldLoadMore) { if (shouldLoadMore) loadNext() }

    Column(modifier = Modifier.fillMaxSize().background(BgDark)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 52.dp, start = 16.dp, end = 16.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x0FFFFFFF))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp)) }
            Spacer(Modifier.width(14.dp))
            Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        if (items.isEmpty() && loading) {
            LoadingSpinner(modifier = Modifier.fillMaxWidth().padding(top = 60.dp))
        } else if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(60.dp), contentAlignment = Alignment.Center) {
                Text("Nothing here yet.", color = TextMuted, fontSize = 14.sp)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                state = gridState,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(items, key = { it.stableId }) { item -> BrowseCard(item, onClick = { onSelect(item) }) }
                if (loading) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(3) }) {
                        LoadingSpinner(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun BrowseCard(item: ContentItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(2f / 3f)
            .clip(RoundedCornerShape(12.dp))
            .background(BgCard)
            .clickable { onClick() },
    ) {
        AsyncImage(model = item.poster, contentDescription = item.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, BgDark.copy(alpha = 0.8f)))))
        item.ratingText?.let { rating ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(5.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
            ) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = AccentGold, modifier = Modifier.size(9.dp))
                Spacer(Modifier.width(2.dp))
                Text(rating, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(
            item.title, color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Medium,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.BottomStart).padding(6.dp),
        )
    }
}
