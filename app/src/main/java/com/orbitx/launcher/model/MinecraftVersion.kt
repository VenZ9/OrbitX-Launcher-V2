package com.orbitx.launcher.model

import java.io.Serializable

/**
 * MinecraftVersion - Represents a Minecraft version
 * Contains all information needed to download and launch a specific version
 */
data class MinecraftVersion(
    val id: String,
    val type: String, // "release", "snapshot", "old_beta", "old_alpha"
    val url: String,
    val time: String,
    val releaseTime: String,
    val sha1: String,
    val complianceLevel: Int? = null,
    
    // Additional metadata
    val minecraftArguments: String? = null,
    val mainClass: String? = null,
    val minimumLauncherVersion: Int? = null,
    val libraries: List<Library> = emptyList(),
    val arguments: Arguments? = null,
    val assetIndex: AssetIndex? = null,
    val logging: LoggingConfig? = null,
    
    // Client information
    val client: ClientInfo? = null,
    val server: ServerInfo? = null,
    
    // Java compatibility
    val javaVersion: JavaVersion? = null,
    
    // Size information
    val size: Long? = null,
    val downloadCount: Long? = null
) : Serializable {
    
    data class Library(
        val name: String,
        val url: String? = null,
        val checksums: List<String>? = null,
        val serverReq: String? = null,
        val clientReq: String? = null,
        val extract: Extract? = null,
        val natives: Map<String, String>? = null,
        val rules: List<Rule>? = null,
        val download: Download? = null,
        val path: String? = null
    ) : Serializable {
        data class Extract(
            val exclude: List<String>? = null
        ) : Serializable
        
        data class Rule(
            val action: String, // "allow", "disallow"
            val os: Os? = null,
            val features: Map<String, Boolean>? = null
        ) : Serializable {
            data class Os(
                val name: String? = null,
                val version: String? = null,
                val arch: String? = null
            ) : Serializable
        }
        
        data class Download(
            val sha1: String? = null,
            val size: Long? = null,
            val url: String? = null,
            val path: String? = null
        ) : Serializable
    }
    
    data class Arguments(
        val game: List<String>? = null,
        val jvm: List<String>? = null
    ) : Serializable
    
    data class AssetIndex(
        val id: String,
        val sha1: String,
        val size: Long,
        val totalSize: Long,
        val url: String
    ) : Serializable
    
    data class LoggingConfig(
        val client: Client? = null,
        val server: Server? = null
    ) : Serializable {
        data class Client(
            val argument: String,
            val file: FileInfo,
            val type: String
        ) : Serializable
        
        data class Server(
            val argument: String,
            val file: FileInfo,
            val type: String
        ) : Serializable
        
        data class FileInfo(
            val id: String,
            val sha1: String,
            val size: Long,
            val url: String
        ) : Serializable
    }
    
    data class ClientInfo(
        val sha1: String,
        val size: Long,
        val url: String,
        val argument: String? = null
    ) : Serializable
    
    data class ServerInfo(
        val sha1: String,
        val size: Long,
        val url: String,
        val argument: String? = null
    ) : Serializable
    
    data class JavaVersion(
        val component: String,
        val majorVersion: Int
    ) : Serializable
    
    /**
     * Check if this version is installed
     */
    fun isInstalled(): Boolean {
        // Implementation would check if the version files exist
        return false
    }
    
    /**
     * Get display name for this version
     */
    fun getDisplayName(): String {
        return when (type) {
            "snapshot" -> "$id (Snapshot)"
            "old_beta" -> "$id (Beta)"
            "old_alpha" -> "$id (Alpha)"
            else -> id
        }
    }
    
    /**
     * Get version type display name
     */
    fun getTypeDisplayName(): String {
        return when (type) {
            "release" -> "Release"
            "snapshot" -> "Snapshot"
            "old_beta" -> "Beta"
            "old_alpha" -> "Alpha"
            else -> type
        }
    }
    
    /**
     * Check if this version requires a specific Java version
     */
    fun requiresJavaVersion(version: Int): Boolean {
        return javaVersion?.majorVersion?.let { it <= version } ?: false
    }
}

/**
 * VersionManifest - Contains the list of all available Minecraft versions
 */
data class VersionManifest(
    val latest: LatestVersions,
    val versions: List<MinecraftVersion>
) : Serializable {
    
    data class LatestVersions(
        val release: String,
        val snapshot: String
    ) : Serializable
}

/**
 * Mod - Represents a Minecraft mod
 */
data class Mod(
    val id: String,
    val name: String,
    val description: String,
    val version: String,
    val minecraftVersion: String,
    val author: String,
    val downloadUrl: String,
    val fileName: String,
    val fileSize: Long,
    val downloads: Long,
    val categories: List<String>,
    val dependencies: List<String>,
    val iconUrl: String? = null,
    val screenshots: List<String> = emptyList(),
    val rating: Float? = null,
    val lastUpdated: String,
    val createdAt: String,
    val websiteUrl: String? = null,
    val sourceUrl: String? = null,
    val license: String? = null
) : Serializable {
    
    /**
     * Get display name with version
     */
    fun getDisplayName(): String = "$name $version"
    
    /**
     * Get formatted file size
     */
    fun getFormattedSize(): String {
        return when {
            fileSize >= 1024 * 1024 * 1024 -> "${"%.2f".format(fileSize.toDouble() / (1024 * 1024 * 1024))} GB"
            fileSize >= 1024 * 1024 -> "${"%.2f".format(fileSize.toDouble() / (1024 * 1024))} MB"
            fileSize >= 1024 -> "${"%.2f".format(fileSize.toDouble() / 1024)} KB"
            else -> "$fileSize B"
        }
    }
}

/**
 * ResourcePack - Represents a Minecraft resource pack
 */
data class ResourcePack(
    val id: String,
    val name: String,
    val description: String,
    val version: String,
    val minecraftVersion: String,
    val author: String,
    val downloadUrl: String,
    val fileName: String,
    val fileSize: Long,
    val resolution: String,
    val downloads: Long,
    val iconUrl: String? = null,
    val screenshots: List<String> = emptyList(),
    val rating: Float? = null,
    val lastUpdated: String,
    val createdAt: String,
    val websiteUrl: String? = null,
    val license: String? = null
) : Serializable {
    
    /**
     * Get display name with version
     */
    fun getDisplayName(): String = "$name $version"
    
    /**
     * Get formatted file size
     */
    fun getFormattedSize(): String {
        return when {
            fileSize >= 1024 * 1024 * 1024 -> "${"%.2f".format(fileSize.toDouble() / (1024 * 1024 * 1024))} GB"
            fileSize >= 1024 * 1024 -> "${"%.2f".format(fileSize.toDouble() / (1024 * 1024))} MB"
            fileSize >= 1024 -> "${"%.2f".format(fileSize.toDouble() / 1024)} KB"
            else -> "$fileSize B"
        }
    }
}

/**
 * ShaderPack - Represents a Minecraft shader pack
 */
data class ShaderPack(
    val id: String,
    val name: String,
    val description: String,
    val version: String,
    val minecraftVersion: String,
    val author: String,
    val downloadUrl: String,
    val fileName: String,
    val fileSize: Long,
    val compatibility: List<String>,
    val downloads: Long,
    val iconUrl: String? = null,
    val screenshots: List<String> = emptyList(),
    val rating: Float? = null,
    val lastUpdated: String,
    val createdAt: String,
    val websiteUrl: String? = null,
    val license: String? = null
) : Serializable {
    
    /**
     * Get display name with version
     */
    fun getDisplayName(): String = "$name $version"
    
    /**
     * Get formatted file size
     */
    fun getFormattedSize(): String {
        return when {
            fileSize >= 1024 * 1024 * 1024 -> "${"%.2f".format(fileSize.toDouble() / (1024 * 1024 * 1024))} GB"
            fileSize >= 1024 * 1024 -> "${"%.2f".format(fileSize.toDouble() / (1024 * 1024))} MB"
            fileSize >= 1024 -> "${"%.2f".format(fileSize.toDouble() / 1024)} KB"
            else -> "$fileSize B"
        }
    }
    
    /**
     * Check if compatible with a specific Minecraft version
     */
    fun isCompatible(version: String): Boolean {
        return compatibility.contains(version) || compatibility.contains("all")
    }
}

/**
 * NewsItem - Represents a news item
 */
data class NewsItem(
    val id: String,
    val title: String,
    val content: String,
    val author: String,
    val publishedAt: String,
    val imageUrl: String? = null,
    val url: String? = null,
    val categories: List<String> = emptyList()
) : Serializable

/**
 * DownloadInfo - Information about a download
 */
data class DownloadInfo(
    val id: String,
    val url: String,
    val destination: String,
    val fileName: String,
    val totalSize: Long,
    val downloadedSize: Long = 0,
    val status: DownloadStatus = DownloadStatus.PENDING,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val speed: Long? = null,
    val errorMessage: String? = null
) : Serializable {
    
    enum class DownloadStatus {
        PENDING,
        DOWNLOADING,
        PAUSED,
        COMPLETED,
        FAILED,
        CANCELLED
    }
    
    /**
     * Get download progress percentage
     */
    fun getProgress(): Int {
        return if (totalSize > 0) {
            ((downloadedSize.toDouble() / totalSize.toDouble()) * 100).toInt()
        } else {
            0
        }
    }
    
    /**
     * Get formatted downloaded size
     */
    fun getFormattedDownloadedSize(): String {
        return when {
            downloadedSize >= 1024 * 1024 * 1024 -> "${"%.2f".format(downloadedSize.toDouble() / (1024 * 1024 * 1024))} GB"
            downloadedSize >= 1024 * 1024 -> "${"%.2f".format(downloadedSize.toDouble() / (1024 * 1024))} MB"
            downloadedSize >= 1024 -> "${"%.2f".format(downloadedSize.toDouble() / 1024)} KB"
            else -> "$downloadedSize B"
        }
    }
    
    /**
     * Get formatted total size
     */
    fun getFormattedTotalSize(): String {
        return when {
            totalSize >= 1024 * 1024 * 1024 -> "${"%.2f".format(totalSize.toDouble() / (1024 * 1024 * 1024))} GB"
            totalSize >= 1024 * 1024 -> "${"%.2f".format(totalSize.toDouble() / (1024 * 1024))} MB"
            totalSize >= 1024 -> "${"%.2f".format(totalSize.toDouble() / 1024)} KB"
            else -> "$totalSize B"
        }
    }
    
    /**
     * Get formatted speed
     */
    fun getFormattedSpeed(): String? {
        return speed?.let { s ->
            when {
                s >= 1024 * 1024 -> "${"%.2f".format(s.toDouble() / (1024 * 1024))} MB/s"
                s >= 1024 -> "${"%.2f".format(s.toDouble() / 1024)} KB/s"
                else -> "$s B/s"
            }
        }
    }
}
