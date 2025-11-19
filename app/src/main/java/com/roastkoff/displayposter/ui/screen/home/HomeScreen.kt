package com.roastkoff.displayposter.ui.screen.home

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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.roastkoff.displayposter.repository.PlaylistItem
import com.roastkoff.displayposter.repository.PlaylistItemFit
import com.roastkoff.displayposter.repository.PlaylistItemType
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0D12))
    ) {
        PlayerSurface(
            items = uiState.value.items,
            defaultIntervalMs = uiState.value.defaultIntervalMs
        )

        FloatingActionButton(
            onClick = { },
            containerColor = Color(0xFF222A35),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) { Text("i", color = Color.White, fontSize = 18.sp) }

        if (uiState.value.infoOpen) {
            AlertDialog(
                onDismissRequest = { viewModel.toggleInfo(false) },
                confirmButton = {
                    TextButton(onClick = { viewModel.toggleInfo(false) }) { Text("OK") }
                },
                title = { Text("Display Info") },
                text = {
                    uiState.value.let { info ->
                        Column {
                            Text("Display: ${info.deviceName}")
                            Text("Tenant: ${info.tenantId ?: "-"}")
                            Text("Branch: ${info.branchId ?: "-"}")
                            Text("Version: ${info.version}")
                            Text("Last Sync: ${info.lastSync}")
                            Text("Items: ${info.items.size}")
                        }
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
    var index by remember(items) { mutableIntStateOf(0) }
    val current = items.getOrNull(index)

    LaunchedEffect(current, items) {
        if (current == null) return@LaunchedEffect
        if (current.type == PlaylistItemType.IMAGE) {
            delay(current.durationMs ?: defaultIntervalMs)
            index = if (items.isNotEmpty()) (index + 1) % items.size else 0
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when (current?.type) {
            PlaylistItemType.IMAGE -> {
                AsyncImage(
                    model = current.src,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = when (current.fit) {
                        PlaylistItemFit.COVER -> ContentScale.Crop
                        PlaylistItemFit.FILL -> ContentScale.FillBounds
                        else -> ContentScale.Fit
                    }
                )
            }

            PlaylistItemType.VIDEO -> {
                VideoPlayer(
                    uri = current.src,
                    mute = current.mute,
                    volume = current.volume.toFloat(),
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
    val context = LocalContext.current

    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    DisposableEffect(uri, mute, volume) {
        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.volume = if (mute) 0f else volume.coerceIn(0f, 1f)
        player.playWhenReady = true

        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) onEnded()
            }
        }
        player.addListener(listener)

        onDispose {
            player.removeListener(listener)
            player.stop()
            player.clearMediaItems()
            player.release()
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            PlayerView(context).apply {
                useController = false
                this.player = player
            }
        },
        update = { it.player = player }
    )
}


@Preview(device = "id:tv_1080p", showSystemUi = true)
@Composable
fun PreviewHome() {
    HomeScreen()
}
