package com.example.v_gallery.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.v_gallery.model.Video
import com.example.v_gallery.ui.components.LoadingIndicator
import com.example.v_gallery.ui.components.VideoGridItem
import com.example.v_gallery.ui.theme.BrightOrange
import com.example.v_gallery.viewmodel.VideoViewModel

/**
 * Screen displaying videos within a selected folder
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderContentsScreen(
    viewModel: VideoViewModel,
    onVideoClick: (Video) -> Unit,
    onBackClick: () -> Unit
) {
    val currentVideos by viewModel.currentVideos.collectAsState()
    val currentFolder by viewModel.currentFolder.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isGridView by viewModel.isGridView.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Folder header with back button
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = BrightOrange
                    )
                }
                
                Text(
                    text = currentFolder?.name ?: "Videos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Videos content
        Box(modifier = Modifier.weight(1f)) {
            if (isLoading) {
                LoadingIndicator()
            } else if (currentVideos.isEmpty()) {
                // Show empty state
                Text(
                    text = "No videos in this folder",
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
                        items = currentVideos,
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
} 