package com.example.v_gallery.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.v_gallery.model.Video
import com.example.v_gallery.ui.components.LoadingIndicator
import com.example.v_gallery.ui.components.VideoGridItem
import com.example.v_gallery.viewmodel.VideoViewModel

/**
 * Screen displaying all videos in a grid
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AllVideosScreen(
    viewModel: VideoViewModel,
    onVideoClick: (Video) -> Unit
) {
    val videos by viewModel.videos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isGridView by viewModel.isGridView.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            LoadingIndicator()
        } else if (videos.isEmpty()) {
            // Show empty state
            Text(
                text = "No videos found",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .align(Alignment.Center),
                textAlign = TextAlign.Center
            )
        } else {
            // Show grid of videos
            LazyVerticalGrid(
                columns = GridCells.Fixed(if (isGridView) 2 else 1),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(
                    items = videos,
                    key = { it.id }
                ) { video ->
                    VideoGridItem(
                        video = video,
                        onClick = onVideoClick
                    )
                }
            }
        }
    }
} 