package com.example.v_gallery

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.v_gallery.model.Video
import com.example.v_gallery.ui.screens.HomeScreen
import com.example.v_gallery.ui.screens.VideoPlayerScreen
import com.example.v_gallery.ui.theme.VGalleryTheme
import com.example.v_gallery.viewmodel.VideoViewModel

class MainActivity : ComponentActivity() {
    
    private val viewModel: VideoViewModel by viewModels()
    
    // Состояние разрешений
    private val permissionsGranted = mutableStateOf(false)
    
    // Permission request launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        permissionsGranted.value = allGranted
        
        if (allGranted) {
            // Reload videos if permissions are granted
            viewModel.loadVideos()
        } else {
            // Показать предупреждение пользователю
            Toast.makeText(
                this,
                "Для доступа к видео в галерее необходимо разрешение на чтение медиафайлов",
                Toast.LENGTH_LONG
            ).show()
            
            // Установить состояние ошибки в ViewModel
            viewModel.setPermissionDeniedError()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Check permissions initially
        permissionsGranted.value = checkPermissionsGranted()
        
        // If permissions not granted, request them
        if (!permissionsGranted.value) {
            requestPermissions()
        }
        
        setContent {
            VGalleryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VideoGalleryApp(viewModel)
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        
        // Проверить разрешения при возобновлении активности
        val currentPermissionState = checkPermissionsGranted()
        
        // Если состояние разрешений изменилось с отказа на разрешение
        if (currentPermissionState && !permissionsGranted.value) {
            permissionsGranted.value = true
            viewModel.clearError()
            viewModel.loadVideos()
        }
    }
    
    private fun checkPermissionsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        // For Android 13+ use READ_MEDIA_VIDEO
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasReadMediaVideoPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED
            
            if (!hasReadMediaVideoPermission) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
            }
        } else {
            // For older versions, use READ_EXTERNAL_STORAGE
            val hasReadExternalStoragePermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            
            if (!hasReadExternalStoragePermission) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}

@Composable
fun VideoGalleryApp(viewModel: VideoViewModel) {
    var currentVideo by remember { mutableStateOf<Video?>(null) }
    
    if (currentVideo != null) {
        // Show the video player screen
        VideoPlayerScreen(
            video = currentVideo!!,
            onBackClick = { currentVideo = null }
        )
    } else {
        // Show the home screen
        HomeScreen(
            viewModel = viewModel,
            onVideoClick = { video -> currentVideo = video }
        )
    }
}