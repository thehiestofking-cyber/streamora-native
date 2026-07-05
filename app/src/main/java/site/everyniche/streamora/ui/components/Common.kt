package site.everyniche.streamora.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import site.everyniche.streamora.data.model.ContentItem
import site.everyniche.streamora.ui.theme.*

@Composable
fun SectionHeader(title: String, badge: String? = null, onSeeAll: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .background(AccentBlue.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(badge, color = AccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        if (onSeeAll != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onSeeAll() },
            ) {
                Text("See all", color = AccentBlue, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
            }
        }
    }
}

enum class RailVariant { POSTER, WIDE, CONTINUE }

@Composable
fun ContentRail(
    title: String,
    items: List<ContentItem>,
    variant: RailVariant = RailVariant.POSTER,
    badge: String? = null,
    onSeeAll: (() -> Unit)? = null,
    onSelect: (ContentItem) -> Unit,
) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        SectionHeader(title, badge, onSeeAll)
        Spacer(Modifier.height(10.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            items(items, key = { it.stableId }) { item ->
                when (variant) {
                    RailVariant.POSTER -> PosterCard(item, onClick = { onSelect(item) })
                    RailVariant.WIDE -> WideCard(item, onClick = { onSelect(item) })
                    RailVariant.CONTINUE -> ContinueCard(item, onClick = { onSelect(item) })
                }
            }
        }
    }
}

@Composable
fun PosterCard(item: ContentItem, onClick: () -> Unit) {
    Column(modifier = Modifier.width(110.dp).clickable { onClick() }) {
        Box(
            modifier = Modifier
                .width(110.dp)
                .height(160.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(BgCard),
        ) {
            AsyncImage(
                model = item.poster,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color.Transparent, BgDark.copy(alpha = 0.55f)))
                )
            )
            if (item.isNew == true) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(AccentBlue, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("NEW", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            item.ratingText?.let { rating ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                ) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = AccentGold, modifier = Modifier.size(10.dp))
                    Spacer(Modifier.width(2.dp))
                    Text(rating, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(item.title, color = Color(0xFFE5E7EB), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        item.genres.firstOrNull()?.let {
            Text(it, color = TextMuted, fontSize = 10.sp, maxLines = 1)
        }
    }
}

@Composable
fun WideCard(item: ContentItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(200.dp)
            .height(115.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(BgCard)
            .clickable { onClick() },
    ) {
        AsyncImage(
            model = item.backdrop ?: item.poster,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, BgDark.copy(alpha = 0.85f)))))
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)) {
            Text(item.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.genres.take(2).joinToString(" · "), color = TextSecondary, fontSize = 10.sp, maxLines = 1)
        }
    }
}

@Composable
fun ContinueCard(item: ContentItem, onClick: () -> Unit, progress: Int = 0) {
    Column(modifier = Modifier.width(200.dp).clickable { onClick() }) {
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(115.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(BgCard),
        ) {
            AsyncImage(
                model = item.backdrop ?: item.poster,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, BgDark.copy(alpha = 0.9f)))))
            Column(modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(10.dp)) {
                Text(item.title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.weight(1f).height(3.dp).clip(RoundedCornerShape(2.dp)),
                        color = AccentBlue,
                        trackColor = Color.White.copy(alpha = 0.2f),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("$progress%", color = TextSecondary, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
fun LoadingSpinner(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AccentBlue, strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
    }
}
