package com.roastkoff.displayposter.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.roastkoff.displayposter.repository.PlaylistItem
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val ui = viewModel.ui
    val bg = Color(0xFF0B0D12)
    Box(
        Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        // Player: แสดงรูป/วิดีโอวนตามรายการ
        PlayerSurface(items = ui.items, defaultIntervalMs = ui.defaultIntervalMs)

        // ปุ่ม Info
        FloatingActionButton(
            onClick = { },
            containerColor = Color(0xFF222A35),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) { Text("i", color = Color.White, fontSize = 18.sp) }

        if (ui.infoOpen) {
            AlertDialog(
                onDismissRequest = { viewModel.toggleInfo(false) },
                confirmButton = {
                    TextButton(onClick = { viewModel.toggleInfo(false) }) { Text("OK") }
                },
                title = { Text("Display Info") },
                text = {
                    Column {
                        Text("Display: ${ui.deviceName}")
                        Text("Tenant: ${ui.tenantId ?: "-"}")
                        Text("Branch: ${ui.branchId ?: "-"}")
                        Text("Version: ${ui.version}")
                        Text("Last Sync: ${ui.lastSync}")
                        Text("Items: ${ui.items.size}")
                    }
                }
            )
        }
    }
}

@Composable
fun PlayerSurface(
    items: List<PlaylistItem>,
    defaultIntervalMs: Long
) {
    var index by remember(items) { mutableStateOf(0) }
    val current = items.getOrNull(index)

    LaunchedEffect(current, items) {
        if (current == null) return@LaunchedEffect
        if (current.type == "image") {
            delay(current.durationMs ?: defaultIntervalMs)
            index = if (items.isNotEmpty()) (index + 1) % items.size else 0
        }
        // วิดีโอ: จะขยับ index ใน onVideoEnded()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when (current?.type) {
            "image" -> {
                AsyncImage(
                    model = current.src,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = when (current.fit) {
                        "cover" -> ContentScale.Crop
                        "fill" -> ContentScale.FillBounds
                        else -> ContentScale.Fit
                    }
                )
            }

            "video" -> {
                VideoPlayer(
                    uri = current.src,
                    mute = current.mute ?: false,
                    volume = (current.volume ?: 1.0).toFloat(),
                    onEnded = {
                        index = if (items.isNotEmpty()) (index + 1) % items.size else 0
                    }
                )
            }

            else -> {
                // ไม่มี item → จอดำ
            }
        }
    }
}

@Composable
fun VideoPlayer(
    uri: String,
    mute: Boolean,
    volume: Float,
    onEnded: () -> Unit
) {
    val ctx = LocalContext.current
    val exo = remember {
        ExoPlayer.Builder(ctx).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }
    DisposableEffect(uri, mute, volume) {
        val item = MediaItem.fromUri(uri)
        exo.setMediaItem(item)
        exo.prepare()
        exo.volume = if (mute) 0f else volume.coerceIn(0f, 1f)
        exo.playWhenReady = true
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) onEnded()
            }
        }
        exo.addListener(listener)
        onDispose {
            exo.removeListener(listener)
            exo.stop(); exo.clearMediaItems()
        }
    }
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            PlayerView(it).apply {
                useController = false; player = exo
            }
        }
    )
}


@Preview(device = "id:tv_1080p", showSystemUi = true)
@Composable
fun PreviewHome() {
    HomeScreen()
}
