package com.example.v_gallery.model

import android.net.Uri

/**
 * Data class representing a video in the gallery
 */
data class Video(
    val id: Long,
    val title: String,
    val uri: Uri,
    val path: String,
    val duration: Long,
    val size: Long,
    val dateAdded: Long,
    val thumbnailUri: Uri?,
    val folderName: String
)

/**
 * Data class representing a folder containing videos
 */
data class Folder(
    val id: String,
    val name: String,
    val path: String,
    val thumbnailUri: Uri?,
    val videoCount: Int
) 