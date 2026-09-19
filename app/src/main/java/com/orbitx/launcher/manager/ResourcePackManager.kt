package com.orbitx.launcher.manager

import android.content.Context
import com.orbitx.launcher.core.OrbitXApplication
import com.orbitx.launcher.model.ResourcePack
import com.orbitx.launcher.utils.Logging
import java.io.File

/**
 * ResourcePackManager - Manages resource packs (installation, removal, listing, etc.)
 * Provides resource pack browser functionality similar to Zalith Launcher 2
 */
class ResourcePackManager(private val context: Context) {
    
    companion object {
        // Resource pack sources/APIs
        private const val MODRINTH_API = "https://api.modrinth.com/v2"
        private const val CURSEFORGE_API = "https://addons-ecs.forgesvc.net/api/v2"
        private const val PLANET_MINECRAFT_API = "https://api.planetminecraft.com/v1"
        private const val RESOURCE_PACKS_URL = "https://resourcepack.net"
        
        // Resource pack categories
        val RESOURCE_PACK_CATEGORIES = listOf(
            "Realistic",
            "Cartoon",
            "Fantasy",
            "Medieval",
            "Modern",
            "Sci-Fi",
            "Vanilla+",
            "Minimalist",
            "Pixel Art",
            "Anime",
            "Horror",
            "Christmas",
            "Halloween",
            "Easter",
            "Summer",
            "Winter",
            "16x",
            "32x",
            "64x",
            "128x",
            "256x",
            "512x",
            "1024x"
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
        data class ResourcePackFilter(
            val searchQuery: String = "",
            val categories: List<String> = emptyList(),
            val resolutions: List<String> = emptyList(),
            val minecraftVersions: List<String> = emptyList(),
            val sortOption: SortOption = SortOption.POPULAR,
            val page: Int = 1,
            val pageSize: Int = 20
        )
        
        private var instance: ResourcePackManager? = null
        
        fun getInstance(context: Context): ResourcePackManager {
            if (instance == null) {
                instance = ResourcePackManager(context.applicationContext)
            }
            return instance!!
        }
    }
    
    private val app: OrbitXApplication = OrbitXApplication.getInstance()
    private val downloadManager: DownloadManager = DownloadManager.getInstance(context)
    
    /**
     * Get list of installed resource packs
     */
    fun getInstalledResourcePacks(): List<ResourcePack> {
        val resourcePacksDir = app.getResourcePacksDirectory()
        if (!resourcePacksDir.exists()) {
            return emptyList()
        }
        
        val installedPacks = mutableListOf<ResourcePack>()
        resourcePacksDir.listFiles()?.forEach { packFile ->
            if (packFile.extension == "zip" && packFile.name != "pack.mcmeta") {
                try {
                    // Parse resource pack file to get pack info
                    val pack = parseResourcePackFile(packFile)
                    installedPacks.add(pack)
                } catch (e: Exception) {
                    Logging.w("ResourcePackManager", "Failed to parse resource pack: ${packFile.name}", e)
                }
            }
        }
        
        return installedPacks
    }
    
    /**
     * Parse resource pack file to extract pack information
     */
    private fun parseResourcePackFile(packFile: File): ResourcePack {
        // Implementation would parse the ZIP file to extract pack info
        // For now, create a basic resource pack object based on filename
        val name = packFile.nameWithoutExtension.replace("_", " ")
        val version = extractVersionFromFilename(packFile.nameWithoutExtension)
        val resolution = extractResolutionFromFilename(packFile.nameWithoutExtension)
        
        return ResourcePack(
            id = packFile.name,
            name = name,
            description = "",
            version = version,
            minecraftVersion = "1.20.4",
            author = "Unknown",
            downloadUrl = "",
            fileName = packFile.name,
            fileSize = packFile.length(),
            resolution = resolution,
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
     * Extract resolution from filename
     */
    private fun extractResolutionFromFilename(filename: String): String {
        val resolutionPattern = Regex("([0-9]+)x")
        val match = resolutionPattern.find(filename)
        return match?.groupValues?.get(1) ?: "16x"
    }
    
    /**
     * Get list of available resource packs from a specific source
     */
    suspend fun getAvailableResourcePacks(
        source: String = "modrinth",
        filter: ResourcePackFilter = ResourcePackFilter()
    ): List<ResourcePack> {
        return try {
            when (source.lowercase()) {
                "modrinth" -> getResourcePacksFromModrinth(filter)
                "curseforge" -> getResourcePacksFromCurseForge(filter)
                "planetminecraft" -> getResourcePacksFromPlanetMinecraft(filter)
                "resourcepacks" -> getResourcePacksFromResourcePacksNet(filter)
                else -> getResourcePacksFromAllSources(filter)
            }
        } catch (e: Exception) {
            Logging.e("ResourcePackManager", "Failed to get available resource packs from $source", e)
            emptyList()
        }
    }
    
    /**
     * Get resource packs from Modrinth
     */
    private suspend fun getResourcePacksFromModrinth(filter: ResourcePackFilter): List<ResourcePack> {
        // Implementation would use Modrinth API
        // For now, return mock data
        return listOf(
            ResourcePack(
                id = "modrinth-rp-1",
                name = "Better Vanilla",
                description = "A faithful improvement to vanilla textures",
                version = "1.20.4",
                minecraftVersion = "1.20.4",
                author = "Steelfeathers",
                downloadUrl = "https://cdn.modrinth.com/data/5SXR883T/versions/1.20.4/BetterVanilla-1.20.4.zip",
                fileName = "BetterVanilla-1.20.4.zip",
                fileSize = 1024 * 1024 * 20, // 20 MB
                resolution = "32x",
                downloads = 500000,
                iconUrl = "https://cdn.modrinth.com/data/5SXR883T/icon.png",
                rating = 4.8f,
                lastUpdated = "2024-01-15T10:30:00+00:00",
                createdAt = "2020-01-01T00:00:00+00:00"
            ),
            ResourcePack(
                id = "modrinth-rp-2",
                name = "Jickus",
                description = "A whimsical and cartoonish resource pack",
                version = "1.20.4",
                minecraftVersion = "1.20.4",
                author = "Jick26",
                downloadUrl = "https://cdn.modrinth.com/data/2rECK9y6/versions/1.20.4/Jickus-1.20.4.zip",
                fileName = "Jickus-1.20.4.zip",
                fileSize = 1024 * 1024 * 15, // 15 MB
                resolution = "16x",
                downloads = 300000,
                iconUrl = "https://cdn.modrinth.com/data/2rECK9y6/icon.png",
                rating = 4.7f,
                lastUpdated = "2024-01-10T15:45:00+00:00",
                createdAt = "2021-03-01T00:00:00+00:00"
            ),
            ResourcePack(
                id = "modrinth-rp-3",
                name = "Modern Craft",
                description = "A modern and clean resource pack",
                version = "1.20.4",
                minecraftVersion = "1.20.4",
                author = "ModernCraft Team",
                downloadUrl = "https://cdn.modrinth.com/data/3D4H6N5G/versions/1.20.4/ModernCraft-1.20.4.zip",
                fileName = "ModernCraft-1.20.4.zip",
                fileSize = 1024 * 1024 * 25, // 25 MB
                resolution = "64x",
                downloads = 400000,
                iconUrl = "https://cdn.modrinth.com/data/3D4H6N5G/icon.png",
                rating = 4.9f,
                lastUpdated = "2024-01-05T12:00:00+00:00",
                createdAt = "2021-06-01T00:00:00+00:00"
            ),
            ResourcePack(
                id = "modrinth-rp-4",
                name = "MedievalCraft",
                description = "A medieval-themed resource pack",
                version = "1.20.4",
                minecraftVersion = "1.20.4",
                author = "MedievalCraft Team",
                downloadUrl = "https://cdn.modrinth.com/data/4JX3Y7Z9/versions/1.20.4/MedievalCraft-1.20.4.zip",
                fileName = "MedievalCraft-1.20.4.zip",
                fileSize = 1024 * 1024 * 30, // 30 MB
                resolution = "128x",
                downloads = 250000,
                iconUrl = "https://cdn.modrinth.com/data/4JX3Y7Z9/icon.png",
                rating = 4.6f,
                lastUpdated = "2024-01-08T09:15:00+00:00",
                createdAt = "2021-04-01T00:00:00+00:00"
            ),
            ResourcePack(
                id = "modrinth-rp-5",
                name = "PureBDcraft",
                description = "A realistic and detailed resource pack",
                version = "1.20.4",
                minecraftVersion = "1.20.4",
                author = "BDcraft Team",
                downloadUrl = "https://cdn.modrinth.com/data/5L4K9R7X/versions/1.20.4/PureBDcraft-1.20.4.zip",
                fileName = "PureBDcraft-1.20.4.zip",
                fileSize = 1024 * 1024 * 50, // 50 MB
                resolution = "512x",
                downloads = 600000,
                iconUrl = "https://cdn.modrinth.com/data/5L4K9R7X/icon.png",
                rating = 4.5f,
                lastUpdated = "2024-01-12T14:30:00+00:00",
                createdAt = "2021-05-01T00:00:00+00:00"
            )
        )
    }
    
    /**
     * Get resource packs from CurseForge
     */
    private suspend fun getResourcePacksFromCurseForge(filter: ResourcePackFilter): List<ResourcePack> {
        // Implementation would use CurseForge API
        // For now, return mock data
        return listOf(
            ResourcePack(
                id = "curseforge-rp-1",
                name = "Sphax PureBDcraft",
                description = "A realistic and detailed resource pack",
                version = "1.20.4",
                minecraftVersion = "1.20.4",
                author = "Sphax",
                downloadUrl = "https://edge.forgecdn.net/files/4800/789/SphaxPureBDcraft-1.20.4.zip",
                fileName = "SphaxPureBDcraft-1.20.4.zip",
                fileSize = 1024 * 1024 * 45, // 45 MB
                resolution = "512x",
                downloads = 800000,
                iconUrl = "https://cf-assets.cursecdn.com/minecraft/texture-packs/2256.png",
                rating = 4.4f,
                lastUpdated = "2024-01-20T18:00:00+00:00",
                createdAt = "2012-01-01T00:00:00+00:00"
            ),
            ResourcePack(
                id = "curseforge-rp-2",
                name = "John Smith Legacy",
                description = "A classic resource pack with a timeless feel",
                version = "1.20.4",
                minecraftVersion = "1.20.4",
                author = "John Smith",
                downloadUrl = "https://edge.forgecdn.net/files/4801/123/JohnSmithLegacy-1.20.4.zip",
                fileName = "JohnSmithLegacy-1.20.4.zip",
                fileSize = 1024 * 1024 * 12, // 12 MB
                resolution = "32x",
                downloads = 700000,
                iconUrl = "https://cf-assets.cursecdn.com/minecraft/texture-packs/322.png",
                rating = 4.6f,
                lastUpdated = "2024-01-18T16:45:00+00:00",
                createdAt = "2011-01-01T00:00:00+00:00"
            )
        )
    }
    
    /**
     * Get resource packs from Planet Minecraft
     */
    private suspend fun getResourcePacksFromPlanetMinecraft(filter: ResourcePackFilter): List<ResourcePack> {
        // Implementation would use Planet Minecraft API
        // For now, return mock data
        return listOf(
            ResourcePack(
                id = "pmc-rp-1",
                name = "OzoCraft",
                description = "A cartoonish and colorful resource pack",
                version = "1.20.4",
                minecraftVersion = "1.20.4",
                author = "OzoCraft Team",
                downloadUrl = "https://files.planetminecraft.com/ozo-craft/OzoCraft-1.20.4.zip",
                fileName = "OzoCraft-1.20.4.zip",
                fileSize = 1024 * 1024 * 18, // 18 MB
                resolution = "32x",
                downloads = 450000,
                iconUrl = "https://static.planetminecraft.com/files/avatar/789012.png",
                rating = 4.7f,
                lastUpdated = "2024-01-16T12:30:00+00:00",
                createdAt = "2010-01-01T00:00:00+00:00"
            )
        )
    }
    
    /**
     * Get resource packs from ResourcePacks.net
     */
    private suspend fun getResourcePacksFromResourcePacksNet(filter: ResourcePackFilter): List<ResourcePack> {
        // Implementation would use ResourcePacks.net API
        // For now, return mock data
        return listOf(
            ResourcePack(
                id = "rpnet-1",
                name = "Vanilla Tweaks",
                description = "Tweaks to vanilla textures for a better experience",
                version = "1.20.4",
                minecraftVersion = "1.20.4",
                author = "Vanilla Tweaks Team",
                downloadUrl = "https://resourcepack.net/download/VanillaTweaks-1.20.4.zip",
                fileName = "VanillaTweaks-1.20.4.zip",
                fileSize = 1024 * 1024 * 8, // 8 MB
                resolution = "16x",
                downloads = 350000,
                iconUrl = "https://resourcepack.net/images/vanilla-tweaks.png",
                rating = 4.8f,
                lastUpdated = "2024-01-14T10:00:00+00:00",
                createdAt = "2019-01-01T00:00:00+00:00"
            )
        )
    }
    
    /**
     * Get resource packs from all sources
     */
    private suspend fun getResourcePacksFromAllSources(filter: ResourcePackFilter): List<ResourcePack> {
        val packs = mutableListOf<ResourcePack>()
        packs.addAll(getResourcePacksFromModrinth(filter))
        packs.addAll(getResourcePacksFromCurseForge(filter))
        packs.addAll(getResourcePacksFromPlanetMinecraft(filter))
        packs.addAll(getResourcePacksFromResourcePacksNet(filter))
        return packs
    }
    
    /**
     * Search for resource packs
     */
    suspend fun searchResourcePacks(query: String, source: String = "all"): List<ResourcePack> {
        val filter = ResourcePackFilter(
            searchQuery = query,
            page = 1,
            pageSize = 50
        )
        
        return when (source.lowercase()) {
            "modrinth" -> getResourcePacksFromModrinth(filter)
            "curseforge" -> getResourcePacksFromCurseForge(filter)
            "planetminecraft" -> getResourcePacksFromPlanetMinecraft(filter)
            "resourcepacks" -> getResourcePacksFromResourcePacksNet(filter)
            else -> getResourcePacksFromAllSources(filter)
        }.filter { pack ->
            pack.name.contains(query, ignoreCase = true) ||
            pack.description.contains(query, ignoreCase = true) ||
            pack.author.contains(query, ignoreCase = true)
        }
    }
    
    /**
     * Get popular resource packs
     */
    suspend fun getPopularResourcePacks(limit: Int = 20): List<ResourcePack> {
        val filter = ResourcePackFilter(
            sortOption = SortOption.POPULAR,
            pageSize = limit
        )
        return getAvailableResourcePacks("all", filter)
    }
    
    /**
     * Get recent resource packs
     */
    suspend fun getRecentResourcePacks(limit: Int = 20): List<ResourcePack> {
        val filter = ResourcePackFilter(
            sortOption = SortOption.RECENT,
            pageSize = limit
        )
        return getAvailableResourcePacks("all", filter)
    }
    
    /**
     * Get top rated resource packs
     */
    suspend fun getTopRatedResourcePacks(limit: Int = 20): List<ResourcePack> {
        val filter = ResourcePackFilter(
            sortOption = SortOption.RATING,
            pageSize = limit
        )
        return getAvailableResourcePacks("all", filter)
    }
    
    /**
     * Get resource packs by category
     */
    suspend fun getResourcePacksByCategory(category: String, limit: Int = 20): List<ResourcePack> {
        val filter = ResourcePackFilter(
            categories = listOf(category),
            pageSize = limit
        )
        return getAvailableResourcePacks("all", filter)
    }
    
    /**
     * Get resource packs by resolution
     */
    suspend fun getResourcePacksByResolution(resolution: String, limit: Int = 20): List<ResourcePack> {
        val filter = ResourcePackFilter(
            resolutions = listOf(resolution),
            pageSize = limit
        )
        return getAvailableResourcePacks("all", filter)
    }
    
    /**
     * Get resource packs for a specific Minecraft version
     */
    suspend fun getResourcePacksForVersion(minecraftVersion: String, limit: Int = 20): List<ResourcePack> {
        val filter = ResourcePackFilter(
            minecraftVersions = listOf(minecraftVersion),
            pageSize = limit
        )
        return getAvailableResourcePacks("all", filter)
    }
    
    /**
     * Install a resource pack
     */
    fun installResourcePack(pack: ResourcePack, callback: (Boolean, String?) -> Unit) {
        try {
            val packsDir = app.getResourcePacksDirectory()
            if (!packsDir.exists()) {
                packsDir.mkdirs()
            }
            
            val destination = File(packsDir, pack.fileName)
            
            // Check if already installed
            if (destination.exists()) {
                callback(true, "Resource pack already installed")
                return
            }
            
            // Download the resource pack
            val downloadId = downloadManager.startDownload(
                url = pack.downloadUrl,
                destination = destination,
                fileName = pack.fileName,
                totalSize = pack.fileSize,
                callback = object : DownloadManager.DownloadCallback {
                    override fun onComplete(downloadInfo: DownloadInfo) {
                        callback(true, "Resource pack installed successfully")
                    }
                    
                    override fun onError(downloadInfo: DownloadInfo, exception: Exception) {
                        callback(false, exception.message ?: "Download failed")
                    }
                }
            )
            
            Logging.d("ResourcePackManager", "Started resource pack download: ${pack.name} - $downloadId")
        } catch (e: Exception) {
            Logging.e("ResourcePackManager", "Failed to install resource pack: ${pack.name}", e)
            callback(false, e.message ?: "Installation failed")
        }
    }
    
    /**
     * Uninstall a resource pack
     */
    fun uninstallResourcePack(pack: ResourcePack, callback: (Boolean, String?) -> Unit) {
        try {
            val packsDir = app.getResourcePacksDirectory()
            val packFile = File(packsDir, pack.fileName)
            
            if (packFile.exists()) {
                packFile.delete()
                callback(true, "Resource pack uninstalled successfully")
            } else {
                callback(false, "Resource pack not found")
            }
        } catch (e: Exception) {
            Logging.e("ResourcePackManager", "Failed to uninstall resource pack: ${pack.name}", e)
            callback(false, e.message ?: "Uninstallation failed")
        }
    }
    
    /**
     * Enable a resource pack
     */
    fun enableResourcePack(pack: ResourcePack, callback: (Boolean, String?) -> Unit) {
        // Implementation would add pack to enabled list
        // For now, just log it
        Logging.d("ResourcePackManager", "Enabled resource pack: ${pack.name}")
        callback(true, "Resource pack enabled")
    }
    
    /**
     * Disable a resource pack
     */
    fun disableResourcePack(pack: ResourcePack, callback: (Boolean, String?) -> Unit) {
        // Implementation would remove pack from enabled list
        // For now, just log it
        Logging.d("ResourcePackManager", "Disabled resource pack: ${pack.name}")
        callback(true, "Resource pack disabled")
    }
    
    /**
     * Check if a resource pack is installed
     */
    fun isResourcePackInstalled(pack: ResourcePack): Boolean {
        val packsDir = app.getResourcePacksDirectory()
        val packFile = File(packsDir, pack.fileName)
        return packFile.exists()
    }
    
    /**
     * Get resource pack by ID
     */
    suspend fun getResourcePackById(packId: String, source: String = "all"): ResourcePack? {
        return getAvailableResourcePacks(source).find { it.id == packId }
    }
    
    /**
     * Get resource pack file
     */
    fun getResourcePackFile(pack: ResourcePack): File? {
        val packsDir = app.getResourcePacksDirectory()
        val packFile = File(packsDir, pack.fileName)
        return if (packFile.exists()) packFile else null
    }
}
