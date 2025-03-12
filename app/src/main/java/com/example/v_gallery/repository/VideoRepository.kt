package com.example.v_gallery.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.example.v_gallery.model.Folder
import com.example.v_gallery.model.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Repository class responsible for accessing videos from the device storage
 */
class VideoRepository(private val context: Context) {

    private val projection = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.DATA,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.SIZE,
        MediaStore.Video.Media.DATE_ADDED
    )

    /**
     * Get all videos from the device storage
     */
    suspend fun getAllVideos(): List<Video> {
        return withContext(Dispatchers.IO) {
            val videos = mutableListOf<Video>()
            
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
            
            val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"
            
            try {
                context.contentResolver.query(
                    collection,
                    projection,
                    null,
                    null,
                    sortOrder
                )?.use { cursor ->
                    // Get column indices
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                    val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                    val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                    val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                    val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                    val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                    
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val name = cursor.getString(nameColumn)
                        val path = cursor.getString(dataColumn)
                        val duration = cursor.getLong(durationColumn)
                        val size = cursor.getLong(sizeColumn)
                        val dateAdded = cursor.getLong(dateAddedColumn)
                        
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                        
                        val folder = File(path).parent?.let { File(it).name } ?: "Unknown Folder"
                        
                        val video = Video(
                            id = id,
                            title = name,
                            uri = contentUri,
                            path = path,
                            duration = duration,
                            size = size,
                            dateAdded = dateAdded,
                            thumbnailUri = getThumbnailUri(id),
                            folderName = folder
                        )
                        videos.add(video)
                    }
                }
            } catch (e: Exception) {
                Log.e("VideoRepository", "Error fetching videos", e)
            }
            
            videos
        }
    }
    
    /**
     * Get all folders containing videos
     */
    suspend fun getAllFolders(): List<Folder> {
        val videos = getAllVideos()
        val foldersMap = mutableMapOf<String, MutableList<Video>>()
        
        // Group videos by folder
        videos.forEach { video ->
            val folderList = foldersMap.getOrPut(video.folderName) { mutableListOf() }
            folderList.add(video)
        }
        
        // Create folder objects
        return foldersMap.map { (folderName, folderVideos) ->
            val folderPath = folderVideos.firstOrNull()?.path?.let {
                File(it).parent ?: ""
            } ?: ""
            
            Folder(
                id = folderPath,
                name = folderName,
                path = folderPath,
                thumbnailUri = folderVideos.firstOrNull()?.thumbnailUri,
                videoCount = folderVideos.size
            )
        }
    }
    
    /**
     * Get videos in a specific folder
     */
    suspend fun getVideosInFolder(folderPath: String): List<Video> {
        return getAllVideos().filter { video ->
            val parent = File(video.path).parent
            parent == folderPath
        }
    }
    
    /**
     * Get thumbnail URI for a video
     * Using content URI approach which is more reliable across Android versions
     */
    private fun getThumbnailUri(videoId: Long): Uri {
        // Create content URI for the video's thumbnail
        return Uri.parse("content://media/external/video/media/$videoId/thumbnail")
    }
} 