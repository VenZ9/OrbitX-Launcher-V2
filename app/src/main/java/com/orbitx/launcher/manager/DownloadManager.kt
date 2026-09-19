package com.orbitx.launcher.manager

import android.app.DownloadManager as AndroidDownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.orbitx.launcher.core.OrbitXApplication
import com.orbitx.launcher.model.DownloadInfo
import com.orbitx.launcher.utils.Logging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * DownloadManager - Manages downloads for mods, resource packs, shaders, and Minecraft versions
 */
class DownloadManager(private val context: Context) {
    
    companion object {
        private const val TIMEOUT_SECONDS = 30L
        private const val BUFFER_SIZE = 8192
        private const val MAX_RETRIES = 3
        
        private var instance: DownloadManager? = null
        
        fun getInstance(context: Context): DownloadManager {
            if (instance == null) {
                instance = DownloadManager(context.applicationContext)
            }
            return instance!!
        }
    }
    
    private val app: OrbitXApplication = OrbitXApplication.getInstance()
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
    
    private val downloadQueue = mutableMapOf<String, DownloadInfo>()
    private val activeDownloads = mutableMapOf<String, DownloadTask>()
    
    /**
     * Start a download
     */
    fun startDownload(
        url: String,
        destination: File,
        fileName: String,
        totalSize: Long? = null,
        callback: DownloadCallback? = null
    ): String {
        val downloadId = generateDownloadId()
        
        val downloadInfo = DownloadInfo(
            id = downloadId,
            url = url,
            destination = destination.absolutePath,
            fileName = fileName,
            totalSize = totalSize ?: 0,
            status = DownloadInfo.DownloadStatus.PENDING
        )
        
        downloadQueue[downloadId] = downloadInfo
        
        // Start the download
        val task = DownloadTask(downloadId, url, destination, fileName, totalSize, callback)
        activeDownloads[downloadId] = task
        task.start()
        
        Logging.d("DownloadManager", "Started download: $downloadId - $fileName")
        
        return downloadId
    }
    
    /**
     * Pause a download
     */
    fun pauseDownload(downloadId: String): Boolean {
        activeDownloads[downloadId]?.let { task ->
            task.pause()
            downloadQueue[downloadId]?.let { info ->
                val pausedInfo = info.copy(status = DownloadInfo.DownloadStatus.PAUSED)
                downloadQueue[downloadId] = pausedInfo
            }
            Logging.d("DownloadManager", "Paused download: $downloadId")
            return true
        } ?: run {
            Logging.w("DownloadManager", "Download not found: $downloadId")
            return false
        }
    }
    
    /**
     * Resume a download
     */
    fun resumeDownload(downloadId: String): Boolean {
        downloadQueue[downloadId]?.let { info ->
            if (info.status == DownloadInfo.DownloadStatus.PAUSED) {
                val destination = File(info.destination)
                val task = DownloadTask(
                    downloadId,
                    info.url,
                    destination,
                    info.fileName,
                    info.totalSize,
                    null
                )
                activeDownloads[downloadId] = task
                task.start()
                
                val resumedInfo = info.copy(status = DownloadInfo.DownloadStatus.DOWNLOADING)
                downloadQueue[downloadId] = resumedInfo
                
                Logging.d("DownloadManager", "Resumed download: $downloadId")
                return true
            }
        } ?: run {
            Logging.w("DownloadManager", "Download not found or not paused: $downloadId")
            return false
        }
        
        return false
    }
    
    /**
     * Cancel a download
     */
    fun cancelDownload(downloadId: String): Boolean {
        activeDownloads[downloadId]?.let { task ->
            task.cancel()
            activeDownloads.remove(downloadId)
            downloadQueue[downloadId]?.let { info ->
                val cancelledInfo = info.copy(status = DownloadInfo.DownloadStatus.CANCELLED)
                downloadQueue[downloadId] = cancelledInfo
            }
            Logging.d("DownloadManager", "Cancelled download: $downloadId")
            return true
        } ?: run {
            Logging.w("DownloadManager", "Download not found: $downloadId")
            return false
        }
    }
    
    /**
     * Get download info by ID
     */
    fun getDownloadInfo(downloadId: String): DownloadInfo? {
        return downloadQueue[downloadId] ?: activeDownloads[downloadId]?.getInfo()
    }
    
    /**
     * Get all active downloads
     */
    fun getActiveDownloads(): List<DownloadInfo> {
        return activeDownloads.values.mapNotNull { it.getInfo() }
    }
    
    /**
     * Get all queued downloads
     */
    fun getQueuedDownloads(): List<DownloadInfo> {
        return downloadQueue.values.filter { 
            it.status == DownloadInfo.DownloadStatus.PENDING || 
            it.status == DownloadInfo.DownloadStatus.PAUSED
        }
    }
    
    /**
     * Get all completed downloads
     */
    fun getCompletedDownloads(): List<DownloadInfo> {
        return downloadQueue.values.filter { it.status == DownloadInfo.DownloadStatus.COMPLETED }
    }
    
    /**
     * Get all failed downloads
     */
    fun getFailedDownloads(): List<DownloadInfo> {
        return downloadQueue.values.filter { it.status == DownloadInfo.DownloadStatus.FAILED }
    }
    
    /**
     * Clear completed downloads from queue
     */
    fun clearCompletedDownloads() {
        downloadQueue.entries.removeAll { (_, info) ->
            info.status == DownloadInfo.DownloadStatus.COMPLETED
        }
    }
    
    /**
     * Clear failed downloads from queue
     */
    fun clearFailedDownloads() {
        downloadQueue.entries.removeAll { (_, info) ->
            info.status == DownloadInfo.DownloadStatus.FAILED
        }
    }
    
    /**
     * Clear all downloads from queue
     */
    fun clearAllDownloads() {
        downloadQueue.clear()
        activeDownloads.values.forEach { it.cancel() }
        activeDownloads.clear()
    }
    
    /**
     * Download a file
     */
    suspend fun downloadFile(
        url: String,
        destination: File,
        progressCallback: ((Long, Long) -> Unit)? = null
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Ensure parent directory exists
                destination.parentFile?.mkdirs()
                
                // Create request
                val request = Request.Builder()
                    .url(url)
                    .build()
                
                // Execute request
                val response = okHttpClient.newCall(request).execute()
                
                if (!response.isSuccessful) {
                    throw IOException("Unexpected code: ${response.code}")
                }
                
                val contentLength = response.body?.contentLength() ?: 0L
                var downloaded: Long = 0L
                
                // Write to file
                response.body?.let { body ->
                    body.byteStream().use { input ->
                        FileOutputStream(destination).use { output ->
                            val buffer = ByteArray(BUFFER_SIZE)
                            var bytesRead: Int
                            
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                                downloaded += bytesRead.toLong()
                                progressCallback?.invoke(downloaded, contentLength)
                            }
                        }
                    }
                }
                
                true
            } catch (e: Exception) {
                Logging.e("DownloadManager", "Failed to download file: $url", e)
                destination.delete()
                false
            }
        }
    }
    
    /**
     * Download using Android's DownloadManager (for large files)
     */
    fun downloadWithSystemManager(
        url: String,
        fileName: String,
        destinationDir: String = Environment.DIRECTORY_DOWNLOADS,
        mimeType: String = "application/octet-stream"
    ): Long {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as AndroidDownloadManager
        
        val request = AndroidDownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setDescription("Downloading $fileName")
            .setNotificationVisibility(AndroidDownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(destinationDir, fileName)
            .setMimeType(mimeType)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
        
        return downloadManager.enqueue(request)
    }
    
    /**
     * Generate a unique download ID
     */
    private fun generateDownloadId(): String {
        return "download_${System.currentTimeMillis()}_${Math.random().toString(36).substring(2, 8)}"
    }
    
    /**
     * Download task for managing individual downloads
     */
    private inner class DownloadTask(
        private val downloadId: String,
        private val url: String,
        private val destination: File,
        private val fileName: String,
        private val totalSize: Long?,
        private val callback: DownloadCallback?
    ) {
        private var isPaused = false
        private var isCancelled = false
        private var retries = 0
        private var downloadedSize: Long = 0
        private var startTime: Long = 0
        
        fun start() {
            isPaused = false
            isCancelled = false
            downloadedSize = 0
            startTime = System.currentTimeMillis()
            
            // Start download in background thread
            Thread {
                try {
                    downloadWithRetries()
                } catch (e: Exception) {
                    handleError(e)
                }
            }.start()
        }
        
        fun pause() {
            isPaused = true
        }
        
        fun cancel() {
            isCancelled = true
            isPaused = false
        }
        
        fun getInfo(): DownloadInfo? {
            return downloadQueue[downloadId]?.copy(
                downloadedSize = downloadedSize,
                status = when {
                    isCancelled -> DownloadInfo.DownloadStatus.CANCELLED
                    isPaused -> DownloadInfo.DownloadStatus.PAUSED
                    else -> DownloadInfo.DownloadStatus.DOWNLOADING
                }
            )
        }
        
        private suspend fun downloadWithRetries() {
            while (retries < MAX_RETRIES && !isCancelled && !isPaused) {
                try {
                    val result = downloadFile(
                        url,
                        destination,
                        { downloaded, total ->
                            downloadedSize = downloaded
                            updateProgress(downloaded, total)
                        }
                    )
                    
                    if (result) {
                        handleSuccess()
                        return
                    } else {
                        retries++
                        Thread.sleep(1000 * retries.toLong()) // Exponential backoff
                    }
                } catch (e: Exception) {
                    retries++
                    if (retries >= MAX_RETRIES || isCancelled) {
                        handleError(e)
                        return
                    }
                    Thread.sleep(1000 * retries.toLong())
                }
            }
            
            if (isCancelled) {
                handleCancelled()
            }
        }
        
        private fun updateProgress(downloaded: Long, total: Long) {
            val speed = if (startTime > 0 && downloaded > 0) {
                val duration = System.currentTimeMillis() - startTime
                if (duration > 0) {
                    (downloaded * 1000 / duration)
                } else {
                    0L
                }
            } else {
                0L
            }
            
            val info = DownloadInfo(
                id = downloadId,
                url = url,
                destination = destination.absolutePath,
                fileName = fileName,
                totalSize = total,
                downloadedSize = downloaded,
                status = DownloadInfo.DownloadStatus.DOWNLOADING,
                startTime = startTime,
                speed = speed
            )
            
            downloadQueue[downloadId] = info
            callback?.onProgress(info)
        }
        
        private fun handleSuccess() {
            val endTime = System.currentTimeMillis()
            val speed = if (endTime > startTime && downloadedSize > 0) {
                (downloadedSize * 1000 / (endTime - startTime))
            } else {
                0L
            }
            
            val info = DownloadInfo(
                id = downloadId,
                url = url,
                destination = destination.absolutePath,
                fileName = fileName,
                totalSize = totalSize ?: 0,
                downloadedSize = downloadedSize,
                status = DownloadInfo.DownloadStatus.COMPLETED,
                startTime = startTime,
                endTime = endTime,
                speed = speed
            )
            
            downloadQueue[downloadId] = info
            activeDownloads.remove(downloadId)
            
            callback?.onComplete(info)
            Logging.d("DownloadManager", "Download completed: $downloadId - $fileName")
        }
        
        private fun handleError(e: Exception) {
            val info = DownloadInfo(
                id = downloadId,
                url = url,
                destination = destination.absolutePath,
                fileName = fileName,
                totalSize = totalSize ?: 0,
                downloadedSize = downloadedSize,
                status = DownloadInfo.DownloadStatus.FAILED,
                startTime = startTime,
                endTime = System.currentTimeMillis(),
                errorMessage = e.message ?: "Unknown error"
            )
            
            downloadQueue[downloadId] = info
            activeDownloads.remove(downloadId)
            
            callback?.onError(info, e)
            Logging.e("DownloadManager", "Download failed: $downloadId - $fileName", e)
        }
        
        private fun handleCancelled() {
            val info = DownloadInfo(
                id = downloadId,
                url = url,
                destination = destination.absolutePath,
                fileName = fileName,
                totalSize = totalSize ?: 0,
                downloadedSize = downloadedSize,
                status = DownloadInfo.DownloadStatus.CANCELLED,
                startTime = startTime,
                endTime = System.currentTimeMillis()
            )
            
            downloadQueue[downloadId] = info
            activeDownloads.remove(downloadId)
            
            callback?.onCancelled(info)
            Logging.d("DownloadManager", "Download cancelled: $downloadId - $fileName")
        }
    }
    
    /**
     * Callback interface for download progress
     */
    interface DownloadCallback {
        fun onProgress(downloadInfo: DownloadInfo) {}
        fun onComplete(downloadInfo: DownloadInfo) {}
        fun onError(downloadInfo: DownloadInfo, exception: Exception) {}
        fun onCancelled(downloadInfo: DownloadInfo) {}
    }
}
