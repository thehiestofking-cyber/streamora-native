package site.everyniche.streamora.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import site.everyniche.streamora.AppContainer
import site.everyniche.streamora.data.model.ContentItem
import site.everyniche.streamora.ui.ViewModelFactory
import site.everyniche.streamora.ui.components.*
import site.everyniche.streamora.ui.theme.*

private val CATEGORIES = listOf("For You", "Movies", "Series", "Anime")
private val GENRES = listOf("Action", "Sci-Fi", "Horror", "Thriller", "Drama", "Mystery")

@Composable
fun HomeScreen(
    container: AppContainer,
    onSelect: (ContentItem) -> Unit,
    onSearch: () -> Unit,
    onBrowse: (String, Map<String, String>) -> Unit,
) {
    val vm: HomeViewModel = viewModel(factory = ViewModelFactory(container))
    val state by vm.state.collectAsState()
    var heroIdx by remember { mutableIntStateOf(0) }
    val hero = state.data?.hero.orEmpty()

    LaunchedEffect(hero.size) {
        if (hero.size < 2) return@LaunchedEffect
        while (true) {
            delay(4500)
            heroIdx = (heroIdx + 1) % hero.size
        }
    }
    LaunchedEffect(state.activeCategory) { heroIdx = 0 }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        if (state.loading && state.data == null) {
            LoadingSpinner(modifier = Modifier.fillMaxSize())
        } else if (hero.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Couldn't reach the server.", color = TextSecondary, fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))
                Button(onClick = { vm.loadCategory(state.activeCategory) }) { Text("Retry") }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    HeroSection(
                        item = hero[heroIdx % hero.size],
                        heroCount = hero.size,
                        heroIdx = heroIdx,
                        onDotClick = { heroIdx = it },
                        onSearch = onSearch,
                        onSelect = onSelect,
                    )
                }
                item {
                    CategoryTabs(active = state.activeCategory, onSelect = { vm.loadCategory(it) })
                    Spacer(Modifier.height(4.dp))
                }
                if (state.continueWatching.isNotEmpty()) {
                    item {
                        ContentRail(
                            title = "Continue Watching",
                            items = state.continueWatching,
                            variant = RailVariant.CONTINUE,
                            onSelect = onSelect,
                        )
                    }
                }
                state.data?.let { data ->
                    item {
                        ContentRail(
                            title = "Trending Now", items = data.trending, badge = "HOT",
                            onSeeAll = { onBrowse("Trending Now", mapOf("type" to "movie", "sort" to "Hottest")) },
                            onSelect = onSelect,
                        )
                    }
                    item {
                        ContentRail(
                            title = "New Releases", items = data.newReleases.take(8), badge = "NEW",
                            onSeeAll = { onBrowse("New Releases", mapOf("type" to "movie", "sort" to "Latest")) },
                            onSelect = onSelect,
                        )
                    }
                    item {
                        ContentRail(
                            title = "Top Rated", items = data.topRated, variant = RailVariant.WIDE,
                            onSeeAll = { onBrowse("Top Rated", mapOf("type" to "movie", "sort" to "Rating")) },
                            onSelect = onSelect,
                        )
                    }
                }
                item {
                    GenreGrid(onGenreClick = { g -> onBrowse(g, mapOf("type" to "movie", "genre" to g, "sort" to "Hottest")) })
                }
                item { Spacer(Modifier.height(120.dp)) }
            }
        }
    }
}

@Composable
private fun HeroSection(
    item: ContentItem,
    heroCount: Int,
    heroIdx: Int,
    onDotClick: (Int) -> Unit,
    onSearch: () -> Unit,
    onSelect: (ContentItem) -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth().height(480.dp)) {
        AnimatedContent(targetState = item.stableId, label = "hero") { _ ->
            AsyncImage(
                model = item.backdrop ?: item.poster,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(Color(0x4D090B10), Color(0x1A090B10), Color(0xF2090B10)),
                    startY = 0f,
                )
            )
        )

        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 52.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(26.dp).clip(RoundedCornerShape(8.dp))
                        .background(Brush.linearGradient(listOf(AccentBlue, AccentBlueDark))),
                    contentAlignment = Alignment.Center,
                ) { Text("▶", color = Color.White, fontSize = 11.sp) }
                Spacer(Modifier.width(8.dp))
                Text("STREAM", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("ORA", color = AccentBlue, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Row {
                RoundIconButton(icon = Icons.Filled.Search, onClick = onSearch)
                Spacer(Modifier.width(10.dp))
                RoundIconButton(icon = Icons.Filled.NotificationsNone, onClick = {})
            }
        }

        // Bottom metadata
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
            Row {
                item.genres.take(2).forEach { g ->
                    Box(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .background(AccentBlue.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) { Text(g, color = Color(0xFF93C5FD), fontSize = 10.sp, fontWeight = FontWeight.SemiBold) }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                item.title, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold,
                maxLines = 2, overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(8.dp))
            item.description?.let {
                Text(it, color = TextSecondary, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(16.dp))
            Row {
                Button(
                    onClick = { onSelect(item) },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                ) { Text("Play Now", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
            }
            if (heroCount > 1) {
                Spacer(Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    repeat(heroCount) { i ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .clickable { onDotClick(i) }
                                .height(5.dp)
                                .width(if (i == heroIdx) 20.dp else 5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (i == heroIdx) AccentBlue else Color(0x4DFFFFFF)),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoundIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x1AFFFFFF))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) { Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp)) }
}

@Composable
private fun CategoryTabs(active: String, onSelect: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(CATEGORIES) { cat ->
            val isActive = cat == active
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isActive) AccentBlue.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { onSelect(cat) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    cat,
                    color = if (isActive) Color(0xFF60A5FA) else TextMuted,
                    fontSize = 13.sp,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun GenreGrid(onGenreClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
        Text("Browse by Genre", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        val colors = listOf(Color(0xFFEF4444), Color(0xFF3B82F6), Color(0xFF8B5CF6), Color(0xFFF59E0B), Color(0xFF10B981), Color(0xFFEC4899))
        val rows = GENRES.chunked(3)
        rows.forEach { rowGenres ->
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowGenres.forEachIndexed { i, g ->
                    val color = colors[GENRES.indexOf(g) % colors.size]
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(color.copy(alpha = 0.18f))
                            .clickable { onGenreClick(g) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(g, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
