package com.roastkoff.displayposter.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    var showControls by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(showControls) {
        if (showControls) {
            delay(5000)
            showControls = false
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0D12))
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent {
                showControls = true
                false
            }
    ) {
        PlayerSurface(
            items = uiState.value.items,
            defaultIntervalMs = uiState.value.defaultIntervalMs
        )

        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            FloatingActionButton(
                onClick = { viewModel.toggleInfo(true) },
                containerColor = Color(0xFF222A35),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .focusable()
                    .onFocusChanged {
                        if (it.isFocused) showControls = true
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Display Info",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (uiState.value.infoOpen) {
            InfoDialog(
                uiState = uiState.value,
                onDismiss = { viewModel.toggleInfo(false) }
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
                        PlaylistItemFit.CONTAIN -> ContentScale.Fit
                        else -> {
                            ContentScale.Fit
                        }
                    }
                )
            }

            PlaylistItemType.VIDEO -> {
                VideoPlayer(
                    uri = current.src,
                    mute = current.mute,
                    volume = current.volume,  // 👈 ส่ง Double ไปตรงๆ
                    onEnded = {
                        index = if (items.isNotEmpty()) (index + 1) % items.size else 0
                    }
                )
            }

            null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "กำลังรอ Playlist...",
                        color = Color(0xFF666666),
                        fontSize = 24.sp
                    )
                }
            }

            PlaylistItemType.UNKNOWN -> {

            }
        }
    }
}

@Composable
fun VideoPlayer(
    uri: String,
    mute: Boolean,
    volume: Double,
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
        player.volume =
            if (mute) 0f else volume.toFloat().coerceIn(0f, 1f)  // 👈 แปลง Double → Float
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

@Composable
fun InfoDialog(
    uiState: HomeUiState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("ปิด", fontSize = 16.sp)
            }
        },
        title = {
            Text(
                "ข้อมูลจอแสดงผล",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoRow(label = "ชื่ออุปกรณ์", value = uiState.deviceName.ifEmpty { "-" })
                InfoRow(label = "Tenant ID", value = uiState.tenantId ?: "-")
                InfoRow(
                    label = "กลุ่ม",
                    value = uiState.deviceName ?: uiState.groupId ?: "-"
                )
                InfoRow(label = "เวอร์ชัน", value = uiState.version.toString())
                InfoRow(label = "ซิงค์ล่าสุด", value = uiState.lastSync)
                InfoRow(label = "จำนวนรายการ", value = "${uiState.items.size} รายการ")

                if (uiState.items.isEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "⚠️ ยังไม่มี Playlist ที่ active",
                        color = Color(0xFFFFB74D),
                        fontSize = 14.sp
                    )
                }
            }
        },
        containerColor = Color(0xFF1E1E1E),
        titleContentColor = Color.White,
        textContentColor = Color(0xFFE0E0E0)
    )
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            fontSize = 14.sp,
            color = Color(0xFFB0B0B0),
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Preview(device = "id:tv_1080p", showSystemUi = true)
@Composable
fun PreviewHome() {
    HomeScreen()
}
