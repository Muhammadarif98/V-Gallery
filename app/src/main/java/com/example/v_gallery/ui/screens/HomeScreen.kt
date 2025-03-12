package com.example.v_gallery.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.v_gallery.model.Folder
import com.example.v_gallery.model.Video
import com.example.v_gallery.ui.components.ErrorMessage
import com.example.v_gallery.ui.components.GalleryTopBar
import com.example.v_gallery.ui.theme.BrightOrange
import com.example.v_gallery.viewmodel.VideoViewModel

/**
 * Main home screen with tabs for All Videos and Folders
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: VideoViewModel,
    onVideoClick: (Video) -> Unit
) {
    val isGridView by viewModel.isGridView.collectAsState()
    val currentFolder by viewModel.currentFolder.collectAsState()
    val error by viewModel.error.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    val tabTitles = listOf("All Videos", "Folders")
    
    Scaffold(
        topBar = {
            Column {
                GalleryTopBar(
                    title = currentFolder?.name ?: "VGallery",
                    isGridView = isGridView,
                    onToggleViewMode = { viewModel.toggleViewMode() }
                )
                
                if (currentFolder == null) {
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = BrightOrange
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Show error message if there is an error
            if (error != null) {
                ErrorMessage(
                    message = error!!,
                    onDismiss = { viewModel.clearError() }
                )
            } else {
                // If we're viewing a folder's contents
                if (currentFolder != null) {
                    FolderContentsScreen(
                        viewModel = viewModel,
                        onVideoClick = onVideoClick,
                        onBackClick = { viewModel.showAllVideos() }
                    )
                } else {
                    // Show tabs content based on selected tab
                    when (selectedTabIndex) {
                        0 -> AllVideosScreen(
                            viewModel = viewModel,
                            onVideoClick = onVideoClick
                        )
                        1 -> FoldersScreen(
                            viewModel = viewModel,
                            onFolderClick = { folder ->
                                viewModel.selectFolder(folder)
                            }
                        )
                    }
                }
            }
        }
    }
} 