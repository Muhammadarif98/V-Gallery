package com.example.v_gallery.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.v_gallery.model.Folder
import com.example.v_gallery.model.Video
import com.example.v_gallery.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel class to manage video gallery state and operations
 */
class VideoViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = VideoRepository(application.applicationContext)
    
    // State holders for videos, folders and loading state
    private val _videos = MutableStateFlow<List<Video>>(emptyList())
    val videos: StateFlow<List<Video>> = _videos.asStateFlow()
    
    private val _folders = MutableStateFlow<List<Folder>>(emptyList())
    val folders: StateFlow<List<Folder>> = _folders.asStateFlow()
    
    private val _currentVideos = MutableStateFlow<List<Video>>(emptyList())
    val currentVideos: StateFlow<List<Video>> = _currentVideos.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _currentFolder = MutableStateFlow<Folder?>(null)
    val currentFolder: StateFlow<Folder?> = _currentFolder.asStateFlow()
    
    // UI state for the current view mode (all videos or folder view)
    private val _isGridView = MutableStateFlow(true)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()
    
    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadVideos()
    }
    
    /**
     * Load all videos from storage
     */
    fun loadVideos() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val allVideos = repository.getAllVideos()
                _videos.value = allVideos
                _currentVideos.value = allVideos
                
                // Load folders after videos are loaded
                val allFolders = repository.getAllFolders()
                _folders.value = allFolders
            } catch (e: Exception) {
                // Log and set error state
                Log.e("VideoViewModel", "Error loading videos", e)
                _error.value = "Не удалось загрузить видео: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Toggle between grid and list view
     */
    fun toggleViewMode() {
        _isGridView.value = !_isGridView.value
    }
    
    /**
     * Select a folder to view its videos
     */
    fun selectFolder(folder: Folder) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val folderVideos = repository.getVideosInFolder(folder.path)
                _currentVideos.value = folderVideos
                _currentFolder.value = folder
            } catch (e: Exception) {
                Log.e("VideoViewModel", "Error loading folder videos", e)
                _error.value = "Не удалось загрузить видео из папки: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Return to the all videos view
     */
    fun showAllVideos() {
        _currentVideos.value = _videos.value
        _currentFolder.value = null
        _error.value = null
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Set error state for permission denied
     */
    fun setPermissionDeniedError() {
        _error.value = "Для работы приложения необходим доступ к видео в галерее. Пожалуйста, предоставьте разрешения в настройках устройства."
    }
} 