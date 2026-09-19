package com.orbitx.launcher.core

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.orbitx.launcher.utils.PreferencesManager
import com.orbitx.launcher.utils.Logging
import java.io.File

/**
 * OrbitX Launcher Application class
 * Initializes core components and manages application state
 */
class OrbitXApplication : Application() {
    
    companion object {
        private lateinit var instance: OrbitXApplication
        fun getInstance(): OrbitXApplication = instance
        fun getAppContext(): Context = instance.applicationContext
    }
    
    // Application directories
    private lateinit var appDir: File
    private lateinit var gameDir: File
    private lateinit var cacheDir: File
    private lateinit var tempDir: File
    private lateinit var logsDir: File
    
    // Managers
    private lateinit var preferencesManager: PreferencesManager
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize logging
        Logging.initialize(this)
        
        // Initialize directories
        initializeDirectories()
        
        // Initialize preferences
        preferencesManager = PreferencesManager(this)
        
        // Set theme based on preferences
        updateTheme()
        
        // Log application start
        Logging.d("OrbitXApplication", "Application started")
    }
    
    /**
     * Initialize application directories
     */
    private fun initializeDirectories() {
        appDir = File(filesDir.parentFile, "orbitx")
        gameDir = File(appDir, "game")
        cacheDir = File(appDir, "cache")
        tempDir = File(appDir, "temp")
        logsDir = File(appDir, "logs")
        
        // Create directories if they don't exist
        listOf(appDir, gameDir, cacheDir, tempDir, logsDir).forEach { dir ->
            if (!dir.exists()) {
                dir.mkdirs()
            }
        }
        
        Logging.d("OrbitXApplication", "Directories initialized: ${appDir.absolutePath}")
    }
    
    /**
     * Update application theme based on preferences
     */
    private fun updateTheme() {
        val theme = preferencesManager.getTheme()
        when (theme) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
    
    /**
     * Get application directory
     */
    fun getAppDirectory(): File = appDir
    
    /**
     * Get game directory
     */
    fun getGameDirectory(): File = gameDir
    
    /**
     * Get cache directory
     */
    fun getCacheDirectory(): File = cacheDir
    
    /**
     * Get temporary directory
     */
    fun getTempDirectory(): File = tempDir
    
    /**
     * Get logs directory
     */
    fun getLogsDirectory(): File = logsDir
    
    /**
     * Get preferences manager
     */
    fun getPreferencesManager(): PreferencesManager = preferencesManager
    
    /**
     * Clean up temporary files
     */
    fun cleanupTempFiles() {
        tempDir.listFiles()?.forEach { file ->
            file.delete()
        }
    }
    
    /**
     * Get Java installation directory
     */
    fun getJavaDirectory(): File = File(appDir, "java")
    
    /**
     * Get Minecraft versions directory
     */
    fun getVersionsDirectory(): File = File(gameDir, "versions")
    
    /**
     * Get Minecraft libraries directory
     */
    fun getLibrariesDirectory(): File = File(gameDir, "libraries")
    
    /**
     * Get Minecraft assets directory
     */
    fun getAssetsDirectory(): File = File(gameDir, "assets")
    
    /**
     * Get mods directory
     */
    fun getModsDirectory(): File = File(gameDir, "mods")
    
    /**
     * Get resource packs directory
     */
    fun getResourcePacksDirectory(): File = File(gameDir, "resourcepacks")
    
    /**
     * Get shaders directory
     */
    fun getShadersDirectory(): File = File(gameDir, "shaderpacks")
    
    /**
     * Get saves directory
     */
    fun getSavesDirectory(): File = File(gameDir, "saves")
    
    /**
     * Get screenshots directory
     */
    fun getScreenshotsDirectory(): File = File(gameDir, "screenshots")
    
    /**
     * Get configs directory
     */
    fun getConfigsDirectory(): File = File(gameDir, "config")
    
    /**
     * Get logs directory
     */
    fun getLogsDirectoryPath(): String = logsDir.absolutePath
    
    /**
     * Get application version name
     */
    fun getVersionName(): String {
        return try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: Exception) {
            "1.0.0"
        }
    }
    
    /**
     * Get application version code
     */
    fun getVersionCode(): Int {
        return try {
            packageManager.getPackageInfo(packageName, 0).versionCode
        } catch (e: Exception) {
            1
        }
    }
}
