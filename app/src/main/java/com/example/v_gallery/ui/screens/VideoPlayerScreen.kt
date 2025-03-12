package com.example.v_gallery.ui.screens

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_BUFFERING
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.common.Player.STATE_IDLE
import androidx.media3.common.Player.STATE_READY
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.v_gallery.model.Video
import com.example.v_gallery.ui.components.LoadingIndicator
import com.example.v_gallery.ui.theme.BrightOrange

/**
 * Screen for playing a video
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    video: Video,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var playbackState by remember { mutableStateOf(STATE_IDLE) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Create an ExoPlayer instance
        val exoPlayer = remember {
            ExoPlayer.Builder(context)
                .build().apply {
                    // Add a listener to monitor playback state
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(state: Int) {
                            playbackState = state
                        }
                        
                        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                            errorMessage = "Ошибка воспроизведения: ${error.message}"
                        }
                    })
                    
                    // Set media item
                    val mediaItem = MediaItem.fromUri(video.uri)
                    setMediaItem(mediaItem)
                    
                    // Prepare the player
                    prepare()
                    
                    // Start playback
                    playWhenReady = true
                    
                    // Set repeat mode
                    repeatMode = Player.REPEAT_MODE_OFF
                    
                    // Set video scaling mode
                    videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                }
        }
        
        // Handle errors using LaunchedEffect
        LaunchedEffect(errorMessage) {
            if (errorMessage != null) {
                // You could log the error or handle it further here
            }
        }
        
        // Clean up the ExoPlayer when leaving the screen
        DisposableEffect(key1 = exoPlayer) {
            onDispose {
                exoPlayer.release()
            }
        }
        
        // The player view or error/loading states
        if (errorMessage != null) {
            // Show error state
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Error",
                            tint = BrightOrange,
                            modifier = Modifier.padding(16.dp)
                        )
                        Text(
                            text = errorMessage ?: "Неизвестная ошибка воспроизведения",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        } else if (playbackState == STATE_BUFFERING) {
            // Show loading state
            LoadingIndicator()
        } else {
            // Show player view
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                        useController = true
                        controllerAutoShow = true
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Back button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = BrightOrange
            )
        }
    }
} 