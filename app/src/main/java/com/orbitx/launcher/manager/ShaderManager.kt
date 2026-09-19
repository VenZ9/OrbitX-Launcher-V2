package com.orbitx.launcher.manager

import android.content.Context
import com.orbitx.launcher.core.OrbitXApplication
import com.orbitx.launcher.model.ShaderPack
import com.orbitx.launcher.utils.Logging
import java.io.File

/**
 * ShaderManager - Manages shader packs (installation, removal, listing, etc.)
 * Provides shader pack browser functionality similar to Zalith Launcher 2
 */
class ShaderManager(private val context: Context) {
    
    companion object {
        // Shader pack sources/APIs
        private const val MODRINTH_API = "https://api.modrinth.com/v2"
        private const val CURSEFORGE_API = "https://addons-ecs.forgesvc.net/api/v2"
        private const val SHADERPACKS_URL = "https://shaderpacks.com"
        private const val SHADERS_MODS_URL = "https://shadersmods.com"
        
        // Shader pack categories
        val SHADER_CATEGORIES = listOf(
            "Realistic",
            "Cartoon",
            "Fantasy",
            "Anime",
            "Stylized",
            "Low",
            "Medium",
            "High",
            "Extreme",
            "PBR",
            "Ray Tracing",
            "Path Tracing",
            "Volumetric",
            "Toon",
            "Cel Shading",
            "Pixel Art",
            "VHS",
            "Retro",
            "Horror",
            "Dream",
            "Nightmare"
        )
        
        // Sort options
        enum class SortOption {
            POPULAR,
            RECENT,
            RATING,
            NAME,
            DOWNLOADS
        }
        
        // Filter options
        data class ShaderFilter(
            val searchQuery: String = "",
            val categories: List<String> = emptyList(),
            val minecraftVersions: List<String> = emptyList(),
            val sortOption: SortOption = SortOption.POPULAR,
            val page: Int = 1,
            val pageSize: Int = 20
        )
        
        private var instance: ShaderManager? = null
        
        fun getInstance(context: Context): ShaderManager {
            if (instance == null) {
                instance = ShaderManager(context.applicationContext)
            }
            return instance!!
        }
    }
    
    private val app: OrbitXApplication = OrbitXApplication.getInstance()
    private val downloadManager: DownloadManager = DownloadManager.getInstance(context)
    
    /**
     * Get list of installed shader packs
     */
    fun getInstalledShaderPacks(): List<ShaderPack> {
        val shadersDir = app.getShadersDirectory()
        if (!shadersDir.exists()) {
            return emptyList()
        }
        
        val installedPacks = mutableListOf<ShaderPack>()
        shadersDir.listFiles()?.forEach { packFile ->
            if (packFile.extension == "zip") {
                try {
                    // Parse shader pack file to get pack info
                    val pack = parseShaderPackFile(packFile)
                    installedPacks.add(pack)
                } catch (e: Exception) {
                    Logging.w("ShaderManager", "Failed to parse shader pack: ${packFile.name}", e)
                }
            }
        }
        
        return installedPacks
    }
    
    /**
     * Parse shader pack file to extract pack information
     */
    private fun parseShaderPackFile(packFile: File): ShaderPack {
        // Implementation would parse the ZIP file to extract shader pack info
        // For now, create a basic shader pack object based on filename
        val name = packFile.nameWithoutExtension.replace("_", " ")
        val version = extractVersionFromFilename(packFile.nameWithoutExtension)
        
        return ShaderPack(
            id = packFile.name,
            name = name,
            description = "",
            version = version,
            minecraftVersion = "1.20.4",
            author = "Unknown",
            downloadUrl = "",
            fileName = packFile.name,
            fileSize = packFile.length(),
            compatibility = listOf("1.20.4", "1.20.3", "1.20.2"),
            downloads = 0,
            lastUpdated = "",
            createdAt = ""
        )
    }
    
    /**
     * Extract version from filename
     */
    private fun extractVersionFromFilename(filename: String): String {
        val versionPattern = Regex("[0-9]+\\.[0-9]+(\\[0-9]+)?(-[a-zA-Z0-9]+)?")
        val match = versionPattern.find(filename)
        return match?.value ?: "1.0.0"
    }
    
    /**
     * Get list of available shader packs from a specific source
     */
    suspend fun getAvailableShaderPacks(
        source: String = "modrinth",
        filter: ShaderFilter = ShaderFilter()
    ): List<ShaderPack> {
        return try {
            when (source.lowercase()) {
                "modrinth" -> getShaderPacksFromModrinth(filter)
                "curseforge" -> getShaderPacksFromCurseForge(filter)
                "shaderpacks" -> getShaderPacksFromShaderPacks(filter)
                "shadersmods" -> getShaderPacksFromShadersMods(filter)
                else -> getShaderPacksFromAllSources(filter)
            }
        } catch (e: Exception) {
            Logging.e("ShaderManager", "Failed to get available shader packs from $source", e)
            emptyList()
        }
    }
    
    /**
     * Get shader packs from Modrinth
     */
    private suspend fun getShaderPacksFromModrinth(filter: ShaderFilter): List<ShaderPack> {
        // Implementation would use Modrinth API
        // For now, return mock data
        return listOf(
            ShaderPack(
                id = "modrinth-sp-1",
                name = "BSL Shaders",
                description = "A high-quality shader pack with realistic lighting",
                version = "8.1.0",
                minecraftVersion = "1.20.4",
                author = "BSL Team",
                downloadUrl = "https://cdn.modrinth.com/data/4KJ98S3M/versions/8.1.0/BSL-v8.1.0.zip",
                fileName = "BSL-v8.1.0.zip",
                fileSize = 1024 * 1024 * 25, // 25 MB
                compatibility = listOf("1.20.4", "1.20.3", "1.20.2", "1.20.1", "1.20"),
                downloads = 1000000,
                iconUrl = "https://cdn.modrinth.com/data/4KJ98S3M/icon.png",
                rating = 4.9f,
                lastUpdated = "2024-01-15T10:30:00+00:00",
                createdAt = "2020-01-01T00:00:00+00:00"
            ),
            ShaderPack(
                id = "modrinth-sp-2",
                name = "Complementary Shaders",
                description = "A shader pack that complements the vanilla look",
                version = "5.0.0",
                minecraftVersion = "1.20.4",
                author = "Erix1337",
                downloadUrl = "https://cdn.modrinth.com/data/5KJ98S3M/versions/5.0.0/Complementary-v5.0.0.zip",
                fileName = "Complementary-v5.0.0.zip",
                fileSize = 1024 * 1024 * 20, // 20 MB
                compatibility = listOf("1.20.4", "1.20.3", "1.20.2", "1.20.1"),
                downloads = 800000,
                iconUrl = "https://cdn.modrinth.com/data/5KJ98S3M/icon.png",
                rating = 4.8f,
                lastUpdated = "2024-01-10T15:45:00+00:00",
                createdAt = "2021-03-01T00:00:00+00:00"
            ),
            ShaderPack(
                id = "modrinth-sp-3",
                name = "SEUS",
                description = "Sonic Ether's Unbelievable Shaders - High quality realistic shaders",
                version = "12.2",
                minecraftVersion = "1.20.4",
                author = "Sonic Ether",
                downloadUrl = "https://cdn.modrinth.com/data/6KJ98S3M/versions/12.2/SEUS-12.2.zip",
                fileName = "SEUS-12.2.zip",
                fileSize = 1024 * 1024 * 30, // 30 MB
                compatibility = listOf("1.20.4", "1.20.3", "1.20.2"),
                downloads = 1200000,
                iconUrl = "https://cdn.modrinth.com/data/6KJ98S3M/icon.png",
                rating = 4.7f,
                lastUpdated = "2024-01-05T12:00:00+00:00",
                createdAt = "2021-06-01T00:00:00+00:00"
            ),
            ShaderPack(
                id = "modrinth-sp-4",
                name = "Sildur's Shaders",
                description = "A collection of vibrant and enhanced shaders",
                version = "1.5.0",
                minecraftVersion = "1.20.4",
                author = "Sildur",
                downloadUrl = "https://cdn.modrinth.com/data/7KJ98S3M/versions/1.5.0/Sildur-v1.5.0.zip",
                fileName = "Sildur-v1.5.0.zip",
                fileSize = 1024 * 1024 * 18, // 18 MB
                compatibility = listOf("1.20.4", "1.20.3", "1.20.2", "1.20.1", "1.20"),
                downloads = 900000,
                iconUrl = "https://cdn.modrinth.com/data/7KJ98S3M/icon.png",
                rating = 4.6f,
                lastUpdated = "2024-01-08T09:15:00+00:00",
                createdAt = "2021-04-01T00:00:00+00:00"
            ),
            ShaderPack(
                id = "modrinth-sp-5",
                name = "MakeUp - Ultra Fast Shaders",
                description = "Ultra fast shaders with minimal performance impact",
                version = "1.4.0",
                minecraftVersion = "1.20.4",
                author = "MakeUp Team",
                downloadUrl = "https://cdn.modrinth.com/data/8KJ98S3M/versions/1.4.0/MakeUp-v1.4.0.zip",
                fileName = "MakeUp-v1.4.0.zip",
                fileSize = 1024 * 1024 * 10, // 10 MB
                compatibility = listOf("1.20.4", "1.20.3", "1.20.2", "1.20.1", "1.20"),
                downloads = 700000,
                iconUrl = "https://cdn.modrinth.com/data/8KJ98S3M/icon.png",
                rating = 4.8f,
                lastUpdated = "2024-01-12T14:30:00+00:00",
                createdAt = "2021-05-01T00:00:00+00:00"
            )
        )
    }
    
    /**
     * Get shader packs from CurseForge
     */
    private suspend fun getShaderPacksFromCurseForge(filter: ShaderFilter): List<ShaderPack> {
        // Implementation would use CurseForge API
        // For now, return mock data
        return listOf(
            ShaderPack(
                id = "curseforge-sp-1",
                name = "Continuum Shaders",
                description = "A high-quality shader pack with advanced lighting effects",
                version = "2.1.0",
                minecraftVersion = "1.20.4",
                author = "Continuum Team",
                downloadUrl = "https://edge.forgecdn.net/files/4800/789/Continuum-2.1.0.zip",
                fileName = "Continuum-2.1.0.zip",
                fileSize = 1024 * 1024 * 40, // 40 MB
                compatibility = listOf("1.20.4", "1.20.3", "1.20.2"),
                downloads = 1500000,
                iconUrl = "https://cf-assets.cursecdn.com/minecraft/shaders/248799.png",
                rating = 4.5f,
                lastUpdated = "2024-01-20T18:00:00+00:00",
                createdAt = "2018-01-01T00:00:00+00:00"
            ),
            ShaderPack(
                id = "curseforge-sp-2",
                name = "ProjectLUMA",
                description = "A shader pack with a focus on realistic lighting",
                version = "1.2.0",
                minecraftVersion = "1.20.4",
                author = "ProjectLUMA Team",
                downloadUrl = "https://edge.forgecdn.net/files/4801/123/ProjectLUMA-1.2.0.zip",
                fileName = "ProjectLUMA-1.2.0.zip",
                fileSize = 1024 * 1024 * 22, // 22 MB
                compatibility = listOf("1.20.4", "1.20.3", "1.20.2", "1.20.1"),
                downloads = 600000,
                iconUrl = "https://cf-assets.cursecdn.com/minecraft/shaders/322.png",
                rating = 4.7f,
                lastUpdated = "2024-01-18T16:45:00+00:00",
                createdAt = "2019-01-01T00:00:00+00:00"
            )
        )
    }
    
    /**
     * Get shader packs from ShaderPacks.com
     */
    private suspend fun getShaderPacksFromShaderPacks(filter: ShaderFilter): List<ShaderPack> {
        // Implementation would use ShaderPacks.com API
        // For now, return mock data
        return listOf(
            ShaderPack(
                id = "shaderpacks-1",
                name = "Chocapic13's Toon Shader",
                description = "A cel-shaded shader pack for a cartoon look",
                version = "1.20.4",
                minecraftVersion = "1.20.4",
                author = "Chocapic13",
                downloadUrl = "https://shaderpacks.com/download/Chocapic13-Toon-Shader-1.20.4.zip",
                fileName = "Chocapic13-Toon-Shader-1.20.4.zip",
                fileSize = 1024 * 1024 * 15, // 15 MB
                compatibility = listOf("1.20.4", "1.20.3", "1.20.2"),
                downloads = 500000,
                iconUrl = "https://shaderpacks.com/images/chocapic13-toon.png",
                rating = 4.8f,
                lastUpdated = "2024-01-16T12:30:00+00:00",
                createdAt = "2017-01-01T00:00:00+00:00"
            )
        )
    }
    
    /**
     * Get shader packs from ShadersMods.com
     */
    private suspend fun getShaderPacksFromShadersMods(filter: ShaderFilter): List<ShaderPack> {
        // Implementation would use ShadersMods.com API
        // For now, return mock data
        return listOf(
            ShaderPack(
                id = "shadersmods-1",
                name = "R. Reptile's Shaders",
                description = "A collection of high-quality shaders with various presets",
                version = "1.20.4",
                minecraftVersion = "1.20.4",
                author = "R. Reptile",
                downloadUrl = "https://shadersmods.com/download/R-Reptile-Shaders-1.20.4.zip",
                fileName = "R-Reptile-Shaders-1.20.4.zip",
                fileSize = 1024 * 1024 * 28, // 28 MB
                compatibility = listOf("1.20.4", "1.20.3", "1.20.2", "1.20.1"),
                downloads = 400000,
                iconUrl = "https://shadersmods.com/images/r-reptile.png",
                rating = 4.9f,
                lastUpdated = "2024-01-14T10:00:00+00:00",
                createdAt = "2016-01-01T00:00:00+00:00"
            )
        )
    }
    
    /**
     * Get shader packs from all sources
     */
    private suspend fun getShaderPacksFromAllSources(filter: ShaderFilter): List<ShaderPack> {
        val packs = mutableListOf<ShaderPack>()
        packs.addAll(getShaderPacksFromModrinth(filter))
        packs.addAll(getShaderPacksFromCurseForge(filter))
        packs.addAll(getShaderPacksFromShaderPacks(filter))
        packs.addAll(getShaderPacksFromShadersMods(filter))
        return packs
    }
    
    /**
     * Search for shader packs
     */
    suspend fun searchShaderPacks(query: String, source: String = "all"): List<ShaderPack> {
        val filter = ShaderFilter(
            searchQuery = query,
            page = 1,
            pageSize = 50
        )
        
        return when (source.lowercase()) {
            "modrinth" -> getShaderPacksFromModrinth(filter)
            "curseforge" -> getShaderPacksFromCurseForge(filter)
            "shaderpacks" -> getShaderPacksFromShaderPacks(filter)
            "shadersmods" -> getShaderPacksFromShadersMods(filter)
            else -> getShaderPacksFromAllSources(filter)
        }.filter { pack ->
            pack.name.contains(query, ignoreCase = true) ||
            pack.description.contains(query, ignoreCase = true) ||
            pack.author.contains(query, ignoreCase = true)
        }
    }
    
    /**
     * Get popular shader packs
     */
    suspend fun getPopularShaderPacks(limit: Int = 20): List<ShaderPack> {
        val filter = ShaderFilter(
            sortOption = SortOption.POPULAR,
            pageSize = limit
        )
        return getAvailableShaderPacks("all", filter)
    }
    
    /**
     * Get recent shader packs
     */
    suspend fun getRecentShaderPacks(limit: Int = 20): List<ShaderPack> {
        val filter = ShaderFilter(
            sortOption = SortOption.RECENT,
            pageSize = limit
        )
        return getAvailableShaderPacks("all", filter)
    }
    
    /**
     * Get top rated shader packs
     */
    suspend fun getTopRatedShaderPacks(limit: Int = 20): List<ShaderPack> {
        val filter = ShaderFilter(
            sortOption = SortOption.RATING,
            pageSize = limit
        )
        return getAvailableShaderPacks("all", filter)
    }
    
    /**
     * Get shader packs by category
     */
    suspend fun getShaderPacksByCategory(category: String, limit: Int = 20): List<ShaderPack> {
        val filter = ShaderFilter(
            categories = listOf(category),
            pageSize = limit
        )
        return getAvailableShaderPacks("all", filter)
    }
    
    /**
     * Get shader packs for a specific Minecraft version
     */
    suspend fun getShaderPacksForVersion(minecraftVersion: String, limit: Int = 20): List<ShaderPack> {
        val filter = ShaderFilter(
            minecraftVersions = listOf(minecraftVersion),
            pageSize = limit
        )
        return getAvailableShaderPacks("all", filter)
    }
    
    /**
     * Install a shader pack
     */
    fun installShaderPack(pack: ShaderPack, callback: (Boolean, String?) -> Unit) {
        try {
            val shadersDir = app.getShadersDirectory()
            if (!shadersDir.exists()) {
                shadersDir.mkdirs()
            }
            
            val destination = File(shadersDir, pack.fileName)
            
            // Check if already installed
            if (destination.exists()) {
                callback(true, "Shader pack already installed")
                return
            }
            
            // Download the shader pack
            val downloadId = downloadManager.startDownload(
                url = pack.downloadUrl,
                destination = destination,
                fileName = pack.fileName,
                totalSize = pack.fileSize,
                callback = object : DownloadManager.DownloadCallback {
                    override fun onComplete(downloadInfo: DownloadInfo) {
                        callback(true, "Shader pack installed successfully")
                    }
                    
                    override fun onError(downloadInfo: DownloadInfo, exception: Exception) {
                        callback(false, exception.message ?: "Download failed")
                    }
                }
            )
            
            Logging.d("ShaderManager", "Started shader pack download: ${pack.name} - $downloadId")
        } catch (e: Exception) {
            Logging.e("ShaderManager", "Failed to install shader pack: ${pack.name}", e)
            callback(false, e.message ?: "Installation failed")
        }
    }
    
    /**
     * Uninstall a shader pack
     */
    fun uninstallShaderPack(pack: ShaderPack, callback: (Boolean, String?) -> Unit) {
        try {
            val shadersDir = app.getShadersDirectory()
            val packFile = File(shadersDir, pack.fileName)
            
            if (packFile.exists()) {
                packFile.delete()
                callback(true, "Shader pack uninstalled successfully")
            } else {
                callback(false, "Shader pack not found")
            }
        } catch (e: Exception) {
            Logging.e("ShaderManager", "Failed to uninstall shader pack: ${pack.name}", e)
            callback(false, e.message ?: "Uninstallation failed")
        }
    }
    
    /**
     * Enable a shader pack
     */
    fun enableShaderPack(pack: ShaderPack, callback: (Boolean, String?) -> Unit) {
        // Implementation would add pack to enabled list
        // For now, just log it
        Logging.d("ShaderManager", "Enabled shader pack: ${pack.name}")
        callback(true, "Shader pack enabled")
    }
    
    /**
     * Disable a shader pack
     */
    fun disableShaderPack(pack: ShaderPack, callback: (Boolean, String?) -> Unit) {
        // Implementation would remove pack from enabled list
        // For now, just log it
        Logging.d("ShaderManager", "Disabled shader pack: ${pack.name}")
        callback(true, "Shader pack disabled")
    }
    
    /**
     * Check if a shader pack is installed
     */
    fun isShaderPackInstalled(pack: ShaderPack): Boolean {
        val shadersDir = app.getShadersDirectory()
        val packFile = File(shadersDir, pack.fileName)
        return packFile.exists()
    }
    
    /**
     * Get shader pack by ID
     */
    suspend fun getShaderPackById(packId: String, source: String = "all"): ShaderPack? {
        return getAvailableShaderPacks(source).find { it.id == packId }
    }
    
    /**
     * Get shader pack file
     */
    fun getShaderPackFile(pack: ShaderPack): File? {
        val shadersDir = app.getShadersDirectory()
        val packFile = File(shadersDir, pack.fileName)
        return if (packFile.exists()) packFile else null
    }
    
    /**
     * Check if a shader pack is compatible with a Minecraft version
     */
    fun isCompatible(pack: ShaderPack, minecraftVersion: String): Boolean {
        return pack.compatibility.contains(minecraftVersion) || 
               pack.compatibility.contains("all") ||
               pack.minecraftVersion == minecraftVersion
    }
}
