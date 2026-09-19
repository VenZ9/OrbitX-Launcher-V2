package com.orbitx.launcher.manager

import android.content.Context
import com.orbitx.launcher.core.OrbitXApplication
import com.orbitx.launcher.model.Mod
import com.orbitx.launcher.utils.Logging
import java.io.File

/**
 * ModManager - Manages mods (installation, removal, listing, etc.)
 * Provides mod browser functionality similar to Zalith Launcher 2
 */
class ModManager(private val context: Context) {
    
    companion object {
        // Mod sources/APIs
        private const val MODRINTH_API = "https://api.modrinth.com/v2"
        private const val CURSEFORGE_API = "https://addons-ecs.forgesvc.net/api/v2"
        private const val PLANET_MINECRAFT_API = "https://api.planetminecraft.com/v1"
        
        // Mod categories
        val MOD_CATEGORIES = listOf(
            "Adventure and RPG",
            "Biomes and Dimensions",
            "Blocks and Items",
            "Building and Decoration",
            "Combat",
            "Commands and Utilities",
            "Cosmetic",
            "Crafting and Recipes",
            "Creativity",
            "Difficulty and Progression",
            "Entities and Mobs",
            "Equipment and Tools",
            "Food and Hunger",
            "Game Mechanics",
            "Library",
            "Magic",
            "Maps and Information",
            "Mini-Games",
            "Miscellaneous",
            "Multiplayer",
            "Optimization",
            "Redstone",
            "Social and Multiplayer",
            "Storage",
            "Technology",
            "Transportation",
            "World Generation"
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
        data class ModFilter(
            val searchQuery: String = "",
            val categories: List<String> = emptyList(),
            val minecraftVersions: List<String> = emptyList(),
            val sortOption: SortOption = SortOption.POPULAR,
            val page: Int = 1,
            val pageSize: Int = 20
        )
        
        private var instance: ModManager? = null
        
        fun getInstance(context: Context): ModManager {
            if (instance == null) {
                instance = ModManager(context.applicationContext)
            }
            return instance!!
        }
    }
    
    private val app: OrbitXApplication = OrbitXApplication.getInstance()
    private val downloadManager: DownloadManager = DownloadManager.getInstance(context)
    
    /**
     * Get list of installed mods
     */
    fun getInstalledMods(): List<Mod> {
        val modsDir = app.getModsDirectory()
        if (!modsDir.exists()) {
            return emptyList()
        }
        
        val installedMods = mutableListOf<Mod>()
        modsDir.listFiles()?.forEach { modFile ->
            if (modFile.extension == "jar" && modFile.name != "mods.toml") {
                try {
                    // Parse mod file to get mod info
                    val mod = parseModFile(modFile)
                    installedMods.add(mod)
                } catch (e: Exception) {
                    Logging.w("ModManager", "Failed to parse mod file: ${modFile.name}", e)
                }
            }
        }
        
        return installedMods
    }
    
    /**
     * Parse mod file to extract mod information
     */
    private fun parseModFile(modFile: File): Mod {
        // Implementation would parse the JAR file to extract mod info
        // For now, create a basic mod object based on filename
        val name = modFile.nameWithoutExtension.replace("_", " ")
        val version = extractVersionFromFilename(modFile.nameWithoutExtension)
        
        return Mod(
            id = modFile.name,
            name = name,
            description = "",
            version = version,
            minecraftVersion = "1.20.4",
            author = "Unknown",
            downloadUrl = "",
            fileName = modFile.name,
            fileSize = modFile.length(),
            downloads = 0,
            categories = emptyList(),
            dependencies = emptyList(),
            lastUpdated = "",
            createdAt = ""
        )
    }
    
    /**
     * Extract version from filename
     */
    private fun extractVersionFromFilename(filename: String): String {
        // Simple version extraction from filename
        val versionPattern = Regex("[0-9]+\\.[0-9]+(\\[0-9]+)?(-[a-zA-Z0-9]+)?")
        val match = versionPattern.find(filename)
        return match?.value ?: "1.0.0"
    }
    
    /**
     * Get list of available mods from a specific source
     */
    suspend fun getAvailableMods(
        source: String = "modrinth",
        filter: ModFilter = ModFilter()
    ): List<Mod> {
        return try {
            when (source.lowercase()) {
                "modrinth" -> getModsFromModrinth(filter)
                "curseforge" -> getModsFromCurseForge(filter)
                "planetminecraft" -> getModsFromPlanetMinecraft(filter)
                else -> getModsFromAllSources(filter)
            }
        } catch (e: Exception) {
            Logging.e("ModManager", "Failed to get available mods from $source", e)
            emptyList()
        }
    }
    
    /**
     * Get mods from Modrinth
     */
    private suspend fun getModsFromModrinth(filter: ModFilter): List<Mod> {
        // Implementation would use Modrinth API
        // For now, return mock data
        return listOf(
            Mod(
                id = "modrinth-1",
                name = "Fabric API",
                description = "The essential library for Fabric mods",
                version = "0.91.0",
                minecraftVersion = "1.20.4",
                author = "FabricMC",
                downloadUrl = "https://cdn.modrinth.com/data/P7dR8mSH/versions/0.91.0+1.20.4/fabric-api-0.91.0+1.20.4.jar",
                fileName = "fabric-api-0.91.0+1.20.4.jar",
                fileSize = 1024 * 1024 * 5, // 5 MB
                downloads = 1000000,
                categories = listOf("Library"),
                dependencies = emptyList(),
                iconUrl = "https://cdn.modrinth.com/data/P7dR8mSH/icon.png",
                rating = 4.8f,
                lastUpdated = "2024-01-15T10:30:00+00:00",
                createdAt = "2021-01-01T00:00:00+00:00"
            ),
            Mod(
                id = "modrinth-2",
                name = "Sodium",
                description = "Modern rendering engine for Minecraft",
                version = "0.5.0",
                minecraftVersion = "1.20.4",
                author = "CaffeineMC",
                downloadUrl = "https://cdn.modrinth.com/data/AANobbMI/versions/0.5.0+mc1.20.4/sodium-fabric-mc1.20.4-0.5.0.jar",
                fileName = "sodium-fabric-mc1.20.4-0.5.0.jar",
                fileSize = 1024 * 1024 * 8, // 8 MB
                downloads = 2000000,
                categories = listOf("Optimization"),
                dependencies = listOf("fabric-api"),
                iconUrl = "https://cdn.modrinth.com/data/AANobbMI/icon.png",
                rating = 4.9f,
                lastUpdated = "2024-01-10T15:45:00+00:00",
                createdAt = "2021-03-01T00:00:00+00:00"
            ),
            Mod(
                id = "modrinth-3",
                name = "Iris Shaders",
                description = "Shader pack support for Fabric",
                version = "1.6.0",
                minecraftVersion = "1.20.4",
                author = "Iris Development Team",
                downloadUrl = "https://cdn.modrinth.com/data/YL5hW8CU/versions/1.6.0+1.20.4/iris-mc1.20.4-1.6.0.jar",
                fileName = "iris-mc1.20.4-1.6.0.jar",
                fileSize = 1024 * 1024 * 10, // 10 MB
                downloads = 1500000,
                categories = listOf("Graphics"),
                dependencies = listOf("fabric-api", "sodium"),
                iconUrl = "https://cdn.modrinth.com/data/YL5hW8CU/icon.png",
                rating = 4.7f,
                lastUpdated = "2024-01-05T12:00:00+00:00",
                createdAt = "2021-06-01T00:00:00+00:00"
            ),
            Mod(
                id = "modrinth-4",
                name = "Lithium",
                description = "General purpose optimization mod",
                version = "0.11.0",
                minecraftVersion = "1.20.4",
                author = "CaffeineMC",
                downloadUrl = "https://cdn.modrinth.com/data/gvQqBUqZ/versions/0.11.0+mc1.20.4/lithium-fabric-mc1.20.4-0.11.0.jar",
                fileName = "lithium-fabric-mc1.20.4-0.11.0.jar",
                fileSize = 1024 * 1024 * 3, // 3 MB
                downloads = 1800000,
                categories = listOf("Optimization"),
                dependencies = listOf("fabric-api"),
                iconUrl = "https://cdn.modrinth.com/data/gvQqBUqZ/icon.png",
                rating = 4.8f,
                lastUpdated = "2024-01-08T09:15:00+00:00",
                createdAt = "2021-04-01T00:00:00+00:00"
            ),
            Mod(
                id = "modrinth-5",
                name = "Phosphor",
                description = "Lighting engine optimization",
                version = "0.8.0",
                minecraftVersion = "1.20.4",
                author = "CaffeineMC",
                downloadUrl = "https://cdn.modrinth.com/data/4c5sXdEi/versions/0.8.0+mc1.20.4/phosphor-fabric-mc1.20.4-0.8.0.jar",
                fileName = "phosphor-fabric-mc1.20.4-0.8.0.jar",
                fileSize = 1024 * 1024 * 2, // 2 MB
                downloads = 1200000,
                categories = listOf("Optimization"),
                dependencies = listOf("fabric-api"),
                iconUrl = "https://cdn.modrinth.com/data/4c5sXdEi/icon.png",
                rating = 4.6f,
                lastUpdated = "2024-01-12T14:30:00+00:00",
                createdAt = "2021-05-01T00:00:00+00:00"
            )
        )
    }
    
    /**
     * Get mods from CurseForge
     */
    private suspend fun getModsFromCurseForge(filter: ModFilter): List<Mod> {
        // Implementation would use CurseForge API
        // For now, return mock data
        return listOf(
            Mod(
                id = "curseforge-1",
                name = "OptiFine",
                description = "Minecraft optimization and shader support",
                version = "1.20.4 HD U I3",
                minecraftVersion = "1.20.4",
                author = "sp614x",
                downloadUrl = "https://edge.forgecdn.net/files/4800/123/OptiFine_1.20.4_HD_U_I3.jar",
                fileName = "OptiFine_1.20.4_HD_U_I3.jar",
                fileSize = 1024 * 1024 * 15, // 15 MB
                downloads = 5000000,
                categories = listOf("Optimization", "Graphics"),
                dependencies = emptyList(),
                iconUrl = "https://cf-assets.cursecdn.com/minecraft/mc-mods/248799.png",
                rating = 4.5f,
                lastUpdated = "2024-01-20T18:00:00+00:00",
                createdAt = "2012-01-01T00:00:00+00:00"
            ),
            Mod(
                id = "curseforge-2",
                name = "JourneyMap",
                description = "Real-time mapping in-game",
                version = "5.9.0",
                minecraftVersion = "1.20.4",
                author = "techbrew",
                downloadUrl = "https://edge.forgecdn.net/files/4801/456/JourneyMap-1.20.4-5.9.0-forge.jar",
                fileName = "JourneyMap-1.20.4-5.9.0-forge.jar",
                fileSize = 1024 * 1024 * 10, // 10 MB
                downloads = 3000000,
                categories = listOf("Maps and Information"),
                dependencies = emptyList(),
                iconUrl = "https://cf-assets.cursecdn.com/minecraft/mc-mods/32274.png",
                rating = 4.7f,
                lastUpdated = "2024-01-18T16:45:00+00:00",
                createdAt = "2011-01-01T00:00:00+00:00"
            )
        )
    }
    
    /**
     * Get mods from Planet Minecraft
     */
    private suspend fun getModsFromPlanetMinecraft(filter: ModFilter): List<Mod> {
        // Implementation would use Planet Minecraft API
        // For now, return mock data
        return listOf(
            Mod(
                id = "pmc-1",
                name = "TooManyItems",
                description = "In-game inventory management",
                version = "1.20.4",
                minecraftVersion = "1.20.4",
                author = "Marglyph",
                downloadUrl = "https://files.planetminecraft.com/too-many-items/TooManyItems-1.20.4.jar",
                fileName = "TooManyItems-1.20.4.jar",
                fileSize = 1024 * 1024 * 2, // 2 MB
                downloads = 500000,
                categories = listOf("Utilities"),
                dependencies = emptyList(),
                iconUrl = "https://static.planetminecraft.com/files/avatar/123456.png",
                rating = 4.3f,
                lastUpdated = "2024-01-16T12:30:00+00:00",
                createdAt = "2010-01-01T00:00:00+00:00"
            )
        )
    }
    
    /**
     * Get mods from all sources
     */
    private suspend fun getModsFromAllSources(filter: ModFilter): List<Mod> {
        val mods = mutableListOf<Mod>()
        mods.addAll(getModsFromModrinth(filter))
        mods.addAll(getModsFromCurseForge(filter))
        mods.addAll(getModsFromPlanetMinecraft(filter))
        return mods
    }
    
    /**
     * Search for mods
     */
    suspend fun searchMods(query: String, source: String = "all"): List<Mod> {
        val filter = ModFilter(
            searchQuery = query,
            page = 1,
            pageSize = 50
        )
        
        return when (source.lowercase()) {
            "modrinth" -> getModsFromModrinth(filter)
            "curseforge" -> getModsFromCurseForge(filter)
            "planetminecraft" -> getModsFromPlanetMinecraft(filter)
            else -> getModsFromAllSources(filter)
        }.filter { mod ->
            mod.name.contains(query, ignoreCase = true) ||
            mod.description.contains(query, ignoreCase = true) ||
            mod.author.contains(query, ignoreCase = true)
        }
    }
    
    /**
     * Get popular mods
     */
    suspend fun getPopularMods(limit: Int = 20): List<Mod> {
        val filter = ModFilter(
            sortOption = SortOption.POPULAR,
            pageSize = limit
        )
        return getAvailableMods("all", filter)
    }
    
    /**
     * Get recent mods
     */
    suspend fun getRecentMods(limit: Int = 20): List<Mod> {
        val filter = ModFilter(
            sortOption = SortOption.RECENT,
            pageSize = limit
        )
        return getAvailableMods("all", filter)
    }
    
    /**
     * Get top rated mods
     */
    suspend fun getTopRatedMods(limit: Int = 20): List<Mod> {
        val filter = ModFilter(
            sortOption = SortOption.RATING,
            pageSize = limit
        )
        return getAvailableMods("all", filter)
    }
    
    /**
     * Get mods by category
     */
    suspend fun getModsByCategory(category: String, limit: Int = 20): List<Mod> {
        val filter = ModFilter(
            categories = listOf(category),
            pageSize = limit
        )
        return getAvailableMods("all", filter)
    }
    
    /**
     * Get mods for a specific Minecraft version
     */
    suspend fun getModsForVersion(minecraftVersion: String, limit: Int = 20): List<Mod> {
        val filter = ModFilter(
            minecraftVersions = listOf(minecraftVersion),
            pageSize = limit
        )
        return getAvailableMods("all", filter)
    }
    
    /**
     * Install a mod
     */
    fun installMod(mod: Mod, callback: (Boolean, String?) -> Unit) {
        try {
            val modsDir = app.getModsDirectory()
            if (!modsDir.exists()) {
                modsDir.mkdirs()
            }
            
            val destination = File(modsDir, mod.fileName)
            
            // Check if already installed
            if (destination.exists()) {
                callback(true, "Mod already installed")
                return
            }
            
            // Download the mod
            val downloadId = downloadManager.startDownload(
                url = mod.downloadUrl,
                destination = destination,
                fileName = mod.fileName,
                totalSize = mod.fileSize,
                callback = object : DownloadManager.DownloadCallback {
                    override fun onComplete(downloadInfo: DownloadInfo) {
                        callback(true, "Mod installed successfully")
                    }
                    
                    override fun onError(downloadInfo: DownloadInfo, exception: Exception) {
                        callback(false, exception.message ?: "Download failed")
                    }
                }
            )
            
            Logging.d("ModManager", "Started mod download: ${mod.name} - $downloadId")
        } catch (e: Exception) {
            Logging.e("ModManager", "Failed to install mod: ${mod.name}", e)
            callback(false, e.message ?: "Installation failed")
        }
    }
    
    /**
     * Uninstall a mod
     */
    fun uninstallMod(mod: Mod, callback: (Boolean, String?) -> Unit) {
        try {
            val modsDir = app.getModsDirectory()
            val modFile = File(modsDir, mod.fileName)
            
            if (modFile.exists()) {
                modFile.delete()
                callback(true, "Mod uninstalled successfully")
            } else {
                callback(false, "Mod not found")
            }
        } catch (e: Exception) {
            Logging.e("ModManager", "Failed to uninstall mod: ${mod.name}", e)
            callback(false, e.message ?: "Uninstallation failed")
        }
    }
    
    /**
     * Enable a mod
     */
    fun enableMod(mod: Mod, callback: (Boolean, String?) -> Unit) {
        // Implementation would move mod to enabled directory
        // For now, just log it
        Logging.d("ModManager", "Enabled mod: ${mod.name}")
        callback(true, "Mod enabled")
    }
    
    /**
     * Disable a mod
     */
    fun disableMod(mod: Mod, callback: (Boolean, String?) -> Unit) {
        // Implementation would move mod to disabled directory
        // For now, just log it
        Logging.d("ModManager", "Disabled mod: ${mod.name}")
        callback(true, "Mod disabled")
    }
    
    /**
     * Check if a mod is installed
     */
    fun isModInstalled(mod: Mod): Boolean {
        val modsDir = app.getModsDirectory()
        val modFile = File(modsDir, mod.fileName)
        return modFile.exists()
    }
    
    /**
     * Get mod by ID
     */
    suspend fun getModById(modId: String, source: String = "all"): Mod? {
        return getAvailableMods(source).find { it.id == modId }
    }
    
    /**
     * Get mod file
     */
    fun getModFile(mod: Mod): File? {
        val modsDir = app.getModsDirectory()
        val modFile = File(modsDir, mod.fileName)
        return if (modFile.exists()) modFile else null
    }
}
