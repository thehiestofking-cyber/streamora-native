package site.everyniche.streamora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import site.everyniche.streamora.AppContainer
import site.everyniche.streamora.data.model.ContentItem
import site.everyniche.streamora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(container: AppContainer, onSelect: (ContentItem) -> Unit) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<ContentItem>>(emptyList()) }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(query) {
        val q = query.trim()
        if (q.length < 2) { results = emptyList(); suggestions = emptyList(); loading = false; return@LaunchedEffect }
        loading = true
        delay(320) // debounce
        val res = container.repository.search(q)
        val sug = if (res.isEmpty()) container.repository.suggest(q) else emptyList()
        results = res
        suggestions = sug
        loading = false
    }

    Column(modifier = Modifier.fillMaxSize().background(BgDark)) {
        Column(modifier = Modifier.padding(top = 52.dp, start = 20.dp, end = 20.dp, bottom = 8.dp)) {
            Text("Search", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Movies, shows, genres...", color = TextMuted) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextMuted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0x0FFFFFFF),
                    unfocusedContainerColor = Color(0x0FFFFFFF),
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = Color(0x14FFFFFF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                ),
            )
        }

        when {
            loading -> LoadingSpinner(modifier = Modifier.fillMaxWidth().padding(top = 40.dp))
            results.isNotEmpty() -> {
                Text(
                    "${results.size} results for \"$query\"",
                    color = TextMuted, fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(results, key = { it.stableId }) { item -> SearchResultCard(item, onClick = { onSelect(item) }) }
                }
            }
            suggestions.isNotEmpty() -> {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    suggestions.forEach { s ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { query = s }
                                .padding(vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(s, color = Color(0xFFE5E7EB), fontSize = 15.sp)
                        }
                    }
                }
            }
            query.length >= 2 -> {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text("No results for \"$query\"", color = TextMuted, fontSize = 14.sp)
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text("Search for movies, series and anime", color = TextMuted, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(item: ContentItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(BgCard)
            .clickable { onClick() },
    ) {
        AsyncImage(model = item.poster, contentDescription = item.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, BgDark.copy(alpha = 0.85f)))))
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)) {
            Text(item.title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            item.genres.firstOrNull()?.let { Text(it, color = TextSecondary, fontSize = 10.sp) }
        }
    }
}
