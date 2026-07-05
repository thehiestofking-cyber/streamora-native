package site.everyniche.streamora.ui.screens

import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import site.everyniche.streamora.AppContainer
import site.everyniche.streamora.data.model.ContentItem
import site.everyniche.streamora.data.model.DownloadOption
import site.everyniche.streamora.ui.theme.AccentBlue

private enum class PlayerStatus { LOADING, READY, ERROR }
private enum class SheetKind { NONE, QUALITY, SPEED }
private val SPEEDS = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)

// Headers the upstream CDN requires (mirrors the backend's DOWNLOAD_REQUEST_HEADERS).
// On a real device these travel with the app's own network egress, which is a
// residential/mobile IP — this is exactly what avoids the datacenter block that
// cloud-hosted backends hit.
private val STREAM_HEADERS = mapOf(
    "Referer" to "https://videodownloader.site/",
    "Origin" to "https://videodownloader.site",
    "User-Agent" to "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Mobile Safari/537.36",
)

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun PlayerScreen(container: AppContainer, item: ContentItem, onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var status by remember { mutableStateOf(PlayerStatus.LOADING) }
    var errorMsg by remember { mutableStateOf("") }
    var options by remember { mutableStateOf<List<DownloadOption>>(emptyList()) }
    var currentUrl by remember { mutableStateOf<String?>(null) }

    var isPlaying by remember { mutableStateOf(true) }
    var showControls by remember { mutableStateOf(true) }
    var sheet by remember { mutableStateOf(SheetKind.NONE) }
    var speed by remember { mutableFloatStateOf(1f) }
    var positionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var bufferedMs by remember { mutableLongStateOf(0L) }

    val httpFactory = remember {
        DefaultHttpDataSource.Factory()
            .setDefaultRequestProperties(STREAM_HEADERS)
            .setConnectTimeoutMs(20_000)
            .setReadTimeoutMs(20_000)
            .setAllowCrossProtocolRedirects(true)
    }
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(DefaultDataSource.Factory(context, httpFactory)))
            .build()
    }

    // Fetch stream URL from the backend.
    LaunchedEffect(item.subjectId, item.detailPath) {
        val subjectId = item.subjectId
        val detailPath = item.detailPath
        if (subjectId == null || detailPath == null) {
            status = PlayerStatus.ERROR
            errorMsg = "This title has no playable source."
            return@LaunchedEffect
        }
        val resp = container.repository.getDownloads(subjectId, detailPath)
        val downloads = resp?.downloads.orEmpty().sortedByDescending { it.resolution ?: 0 }
        if (resp == null || resp.limited == true || downloads.isEmpty()) {
            status = PlayerStatus.ERROR
            errorMsg = "No playable stream found for this title."
            return@LaunchedEffect
        }
        options = downloads
        currentUrl = downloads.first().url
        status = PlayerStatus.READY
    }

    // Load into ExoPlayer whenever the URL changes.
    LaunchedEffect(currentUrl) {
        val url = currentUrl ?: return@LaunchedEffect
        val mediaItem = MediaItem.fromUri(url)
        val wasPlaying = exoPlayer.isPlaying
        val resumeAt = exoPlayer.currentPosition
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        if (resumeAt > 0) exoPlayer.seekTo(resumeAt)
        exoPlayer.playWhenReady = true
        exoPlayer.playbackParameters = androidx.media3.common.PlaybackParameters(speed)
    }

    // Player listener.
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                status = PlayerStatus.ERROR
                errorMsg = "Playback error: ${error.errorCodeName}"
            }
        }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }
    }

    // Progress polling + save history.
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            if (exoPlayer.duration > 0) {
                positionMs = exoPlayer.currentPosition
                durationMs = exoPlayer.duration
                bufferedMs = exoPlayer.bufferedPosition
            }
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(15000)
            if (durationMs > 0) {
                container.supabaseData.recordHistory(item, positionMs / 1000.0, durationMs / 1000.0)
            }
        }
    }

    // Release player + save final position on leave.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) exoPlayer.pause()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (durationMs > 0) {
                scope.launch { container.supabaseData.recordHistory(item, positionMs / 1000.0, durationMs / 1000.0) }
            }
            exoPlayer.release()
        }
    }

    // Auto-hide controls while playing.
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(3500)
            showControls = false
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black)
            .clickable(enabled = status == PlayerStatus.READY) { showControls = !showControls },
    ) {
        if (status == PlayerStatus.READY) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    PlayerView(context).apply {
                        player = exoPlayer
                        useController = false
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    }
                },
            )
        } else {
            AsyncImage(
                model = item.backdrop, contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                alpha = 0.35f,
            )
        }

        when (status) {
            PlayerStatus.LOADING -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AccentBlue)
                    Spacer(Modifier.height(12.dp))
                    Text("Loading stream…", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                }
            }
            PlayerStatus.ERROR -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Text("Can't play right now", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text(errorMsg, color = Color(0xFF9CA3AF), fontSize = 13.sp)
                    Spacer(Modifier.height(18.dp))
                    Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)) { Text("Go back") }
                }
            }
            PlayerStatus.READY -> {
                if (showControls) {
                    PlayerControls(
                        title = item.title,
                        isPlaying = isPlaying,
                        positionMs = positionMs,
                        durationMs = durationMs,
                        bufferedMs = bufferedMs,
                        speed = speed,
                        onBack = onBack,
                        onTogglePlay = { if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play() },
                        onSeekBy = { deltaMs -> exoPlayer.seekTo((exoPlayer.currentPosition + deltaMs).coerceIn(0, exoPlayer.duration.coerceAtLeast(0))) },
                        onSeekTo = { ratio -> if (exoPlayer.duration > 0) exoPlayer.seekTo((ratio * exoPlayer.duration).toLong()) },
                        onOpenQuality = { sheet = SheetKind.QUALITY },
                        onOpenSpeed = { sheet = SheetKind.SPEED },
                    )
                }
            }
        }

        if (sheet != SheetKind.NONE) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable { sheet = SheetKind.NONE },
                contentAlignment = Alignment.BottomCenter,
            ) {
                PlayerBottomSheet(
                    kind = sheet,
                    options = options,
                    currentUrl = currentUrl,
                    speed = speed,
                    onDismiss = { sheet = SheetKind.NONE },
                    onSelectQuality = { opt -> currentUrl = opt.url; sheet = SheetKind.NONE },
                    onSelectSpeed = { s -> speed = s; exoPlayer.playbackParameters = androidx.media3.common.PlaybackParameters(s); sheet = SheetKind.NONE },
                )
            }
        }
    }
}

@Composable
private fun PlayerControls(
    title: String,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    bufferedMs: Long,
    speed: Float,
    onBack: () -> Unit,
    onTogglePlay: () -> Unit,
    onSeekBy: (Long) -> Unit,
    onSeekTo: (Float) -> Unit,
    onOpenQuality: () -> Unit,
    onOpenSpeed: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircleIconButton(Icons.Filled.ArrowBack) { onBack() }
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
            CircleIconButton(Icons.Filled.Settings) { onOpenQuality() }
        }

        Spacer(Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onSeekBy(-10000) }) { Icon(Icons.Filled.Replay10, contentDescription = "Back 10s", tint = Color.White, modifier = Modifier.size(38.dp)) }
            Spacer(Modifier.width(28.dp))
            Box(
                modifier = Modifier.size(66.dp).clip(RoundedCornerShape(50)).background(AccentBlue.copy(alpha = 0.9f)).clickable { onTogglePlay() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = "Play/Pause", tint = Color.White, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.width(28.dp))
            IconButton(onClick = { onSeekBy(10000) }) { Icon(Icons.Filled.Forward10, contentDescription = "Forward 10s", tint = Color.White, modifier = Modifier.size(38.dp)) }
        }

        Spacer(Modifier.weight(1f))

        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            val pct = if (durationMs > 0) positionMs.toFloat() / durationMs else 0f
            val bufPct = if (durationMs > 0) bufferedMs.toFloat() / durationMs else 0f
            Box(modifier = Modifier.fillMaxWidth().height(20.dp)) {
                Box(modifier = Modifier.fillMaxWidth().height(4.dp).align(Alignment.Center).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.2f)))
                Box(modifier = Modifier.fillMaxWidth(bufPct).height(4.dp).align(Alignment.CenterStart).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.4f)))
                Box(modifier = Modifier.fillMaxWidth(pct).height(4.dp).align(Alignment.CenterStart).clip(RoundedCornerShape(2.dp)).background(AccentBlue))
            }
            Slider(
                value = pct.coerceIn(0f, 1f),
                onValueChange = { onSeekTo(it) },
                modifier = Modifier.fillMaxWidth().offset(y = (-8).dp),
                colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.Transparent, inactiveTrackColor = Color.Transparent),
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(fmtTime(positionMs), color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp)
                Text(fmtTime(durationMs), color = Color.White.copy(alpha = 0.45f), fontSize = 11.sp)
            }
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onOpenSpeed) {
                    Icon(Icons.Filled.Speed, contentDescription = "Speed", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (speed == 1f) "Speed" else "${speed}x", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun CircleIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(Color.White.copy(alpha = 0.1f)).clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) { Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp)) }
}

@Composable
private fun PlayerBottomSheet(
    kind: SheetKind,
    options: List<DownloadOption>,
    currentUrl: String?,
    speed: Float,
    onDismiss: () -> Unit,
    onSelectQuality: (DownloadOption) -> Unit,
    onSelectSpeed: (Float) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F1420), RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
            .padding(bottom = 24.dp),
    ) {
        Spacer(Modifier.height(10.dp))
        Box(modifier = Modifier.width(40.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.2f)))
        Spacer(Modifier.height(14.dp))
        Text(
            if (kind == SheetKind.QUALITY) "Quality" else "Playback speed",
            color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(8.dp))
        if (kind == SheetKind.QUALITY) {
            if (options.isEmpty()) {
                Text("No quality options.", color = Color(0xFF6B7280), fontSize = 13.sp, modifier = Modifier.padding(20.dp))
            }
            options.forEach { opt ->
                val active = opt.url == currentUrl
                SheetRow(
                    label = if (opt.resolution != null) "${opt.resolution}p" else "Auto",
                    active = active,
                    onClick = { onSelectQuality(opt) },
                )
            }
        } else {
            SPEEDS.forEach { s ->
                SheetRow(label = if (s == 1f) "Normal (1x)" else "${s}x", active = s == speed, onClick = { onSelectSpeed(s) })
            }
        }
    }
}

@Composable
private fun SheetRow(label: String, active: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (active) AccentBlue.copy(alpha = 0.08f) else Color.Transparent)
            .padding(horizontal = 20.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = if (active) Color(0xFF60A5FA) else Color(0xFFE5E7EB), fontSize = 14.sp, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
        if (active) Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(AccentBlue))
    }
}

private fun fmtTime(ms: Long): String {
    val totalSec = (ms / 1000).toInt().coerceAtLeast(0)
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}
