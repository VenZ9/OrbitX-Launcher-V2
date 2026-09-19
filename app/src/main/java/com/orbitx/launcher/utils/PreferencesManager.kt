package com.orbitx.launcher.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * PreferencesManager - Manages application preferences and settings
 * Uses SharedPreferences for storage
 */
class PreferencesManager(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "OrbitXPreferences"
        private const val KEY_THEME = "theme"
        private const val KEY_JAVA_PATH = "java_path"
        private const val KEY_JAVA_ARGS = "java_args"
        private const val KEY_MEMORY_ALLOCATION = "memory_allocation"
        private const val KEY_GAME_DIRECTORY = "game_directory"
        private const val KEY_LAST_VERSION = "last_version"
        private const val KEY_LAST_USERNAME = "last_username"
        private const val KEY_ACCOUNTS = "accounts"
        private const val KEY_PROFILES = "profiles"
        private const val KEY_MODS_ENABLED = "mods_enabled"
        private const val KEY_RESOURCE_PACKS_ENABLED = "resource_packs_enabled"
        private const val KEY_SHADERS_ENABLED = "shaders_enabled"
        private const val KEY_FULLSCREEN = "fullscreen"
        private const val KEY_VSYNC = "vsync"
        private const val KEY_FPS_COUNTER = "fps_counter"
        private const val KEY_RENDER_DISTANCE = "render_distance"
        private const val KEY_GRAPHICS_QUALITY = "graphics_quality"
        private const val KEY_MASTER_VOLUME = "master_volume"
        private const val KEY_MUSIC_VOLUME = "music_volume"
        private const val KEY_SOUND_VOLUME = "sound_volume"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_WELCOME_COMPLETED = "welcome_completed"
        private const val KEY_UPDATE_CHECK_ENABLED = "update_check_enabled"
        private const val KEY_LAST_UPDATE_CHECK = "last_update_check"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_DOWNLOAD_WIFI_ONLY = "download_wifi_only"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_TOUCH_CONTROLS_ENABLED = "touch_controls_enabled"
        private const val KEY_TOUCH_CONTROLS_CONFIG = "touch_controls_config"
        
        // Additional keys for SettingsFragment
        private const val KEY_SENSITIVITY = "sensitivity"
        private const val KEY_SHOW_FPS = "show_fps"
        private const val KEY_PROFILE_NAME = "profile_name"
        private const val KEY_SELECTED_VERSION = "selected_version"
        private const val KEY_JAVA_VERSION = "java_version"
        
        // Default values
        private const val DEFAULT_THEME = "system"
        private const val DEFAULT_JAVA_ARGS = "-Xmx1G -Xms512M"
        private const val DEFAULT_MEMORY_ALLOCATION = 1024
        private const val DEFAULT_FULLSCREEN = true
        private const val DEFAULT_VSYNC = true
        private const val DEFAULT_FPS_COUNTER = false
        private const val DEFAULT_RENDER_DISTANCE = 16
        private const val DEFAULT_GRAPHICS_QUALITY = "fast"
        private const val DEFAULT_MASTER_VOLUME = 100
        private const val DEFAULT_MUSIC_VOLUME = 100
        private const val DEFAULT_SOUND_VOLUME = 100
        private const val DEFAULT_UPDATE_CHECK_ENABLED = true
        private const val DEFAULT_NOTIFICATIONS_ENABLED = true
        private const val DEFAULT_DOWNLOAD_WIFI_ONLY = false
        private const val DEFAULT_LANGUAGE = "en"
        private const val DEFAULT_TOUCH_CONTROLS_ENABLED = true
        private const val DEFAULT_SENSITIVITY = 50
        private const val DEFAULT_PROFILE_NAME = "Default Profile"
        private const val DEFAULT_SELECTED_VERSION = "1.20.4"
        private const val DEFAULT_JAVA_VERSION = "OpenJDK 17"
    }
    
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    private val gson: Gson by lazy {
        Gson()
    }
    
    // Theme preferences
    fun getTheme(): String = sharedPreferences.getString(KEY_THEME, DEFAULT_THEME) ?: DEFAULT_THEME
    fun setTheme(theme: String) = sharedPreferences.edit().putString(KEY_THEME, theme).apply()
    
    // Java preferences
    fun getJavaPath(): String? = sharedPreferences.getString(KEY_JAVA_PATH, null)
    fun setJavaPath(path: String) = sharedPreferences.edit().putString(KEY_JAVA_PATH, path).apply()
    
    fun getJavaArgs(): String = sharedPreferences.getString(KEY_JAVA_ARGS, DEFAULT_JAVA_ARGS) ?: DEFAULT_JAVA_ARGS
    fun setJavaArgs(args: String) = sharedPreferences.edit().putString(KEY_JAVA_ARGS, args).apply()
    
    fun getMemoryAllocation(): Int = sharedPreferences.getInt(KEY_MEMORY_ALLOCATION, DEFAULT_MEMORY_ALLOCATION)
    fun setMemoryAllocation(memory: Int) = sharedPreferences.edit().putInt(KEY_MEMORY_ALLOCATION, memory).apply()
    
    // Game directory
    fun getGameDirectory(): String? = sharedPreferences.getString(KEY_GAME_DIRECTORY, null)
    fun setGameDirectory(directory: String) = sharedPreferences.edit().putString(KEY_GAME_DIRECTORY, directory).apply()
    
    // Version preferences
    fun getLastVersion(): String? = sharedPreferences.getString(KEY_LAST_VERSION, null)
    fun setLastVersion(version: String) = sharedPreferences.edit().putString(KEY_LAST_VERSION, version).apply()
    
    // Account preferences
    fun getLastUsername(): String? = sharedPreferences.getString(KEY_LAST_USERNAME, null)
    fun setLastUsername(username: String) = sharedPreferences.edit().putString(KEY_LAST_USERNAME, username).apply()
    
    // Accounts management
    fun getAccounts(): List<Account> {
        val json = sharedPreferences.getString(KEY_ACCOUNTS, null)
        return if (json != null) {
            try {
                gson.fromJson(json, object : TypeToken<List<Account>>() {}.type)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    fun addAccount(account: Account) {
        val accounts = getAccounts().toMutableList()
        accounts.add(account)
        sharedPreferences.edit().putString(KEY_ACCOUNTS, gson.toJson(accounts)).apply()
    }
    
    fun updateAccount(account: Account) {
        val accounts = getAccounts().toMutableList()
        val index = accounts.indexOfFirst { it.username == account.username }
        if (index != -1) {
            accounts[index] = account
            sharedPreferences.edit().putString(KEY_ACCOUNTS, gson.toJson(accounts)).apply()
        }
    }
    
    fun removeAccount(username: String) {
        val accounts = getAccounts().toMutableList()
        accounts.removeAll { it.username == username }
        sharedPreferences.edit().putString(KEY_ACCOUNTS, gson.toJson(accounts)).apply()
    }
    
    // Profiles management
    fun getProfiles(): List<Profile> {
        val json = sharedPreferences.getString(KEY_PROFILES, null)
        return if (json != null) {
            try {
                gson.fromJson(json, object : TypeToken<List<Profile>>() {}.type)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    fun addProfile(profile: Profile) {
        val profiles = getProfiles().toMutableList()
        profiles.add(profile)
        sharedPreferences.edit().putString(KEY_PROFILES, gson.toJson(profiles)).apply()
    }
    
    fun updateProfile(profile: Profile) {
        val profiles = getProfiles().toMutableList()
        val index = profiles.indexOfFirst { it.name == profile.name }
        if (index != -1) {
            profiles[index] = profile
            sharedPreferences.edit().putString(KEY_PROFILES, gson.toJson(profiles)).apply()
        }
    }
    
    fun removeProfile(name: String) {
        val profiles = getProfiles().toMutableList()
        profiles.removeAll { it.name == name }
        sharedPreferences.edit().putString(KEY_PROFILES, gson.toJson(profiles)).apply()
    }
    
    // Mods preferences
    fun areModsEnabled(): Boolean = sharedPreferences.getBoolean(KEY_MODS_ENABLED, true)
    fun setModsEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean(KEY_MODS_ENABLED, enabled).apply()
    
    // Resource packs preferences
    fun areResourcePacksEnabled(): Boolean = sharedPreferences.getBoolean(KEY_RESOURCE_PACKS_ENABLED, true)
    fun setResourcePacksEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean(KEY_RESOURCE_PACKS_ENABLED, enabled).apply()
    
    // Shaders preferences
    fun areShadersEnabled(): Boolean = sharedPreferences.getBoolean(KEY_SHADERS_ENABLED, false)
    fun setShadersEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean(KEY_SHADERS_ENABLED, enabled).apply()
    
    // Graphics preferences
    fun isFullscreen(): Boolean = sharedPreferences.getBoolean(KEY_FULLSCREEN, DEFAULT_FULLSCREEN)
    fun setFullscreen(fullscreen: Boolean) = sharedPreferences.edit().putBoolean(KEY_FULLSCREEN, fullscreen).apply()
    
    fun isVsyncEnabled(): Boolean = sharedPreferences.getBoolean(KEY_VSYNC, DEFAULT_VSYNC)
    fun setVsyncEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean(KEY_VSYNC, enabled).apply()
    
    fun isFpsCounterEnabled(): Boolean = sharedPreferences.getBoolean(KEY_FPS_COUNTER, DEFAULT_FPS_COUNTER)
    fun setFpsCounterEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean(KEY_FPS_COUNTER, enabled).apply()
    
    fun getRenderDistance(): Int = sharedPreferences.getInt(KEY_RENDER_DISTANCE, DEFAULT_RENDER_DISTANCE)
    fun setRenderDistance(distance: Int) = sharedPreferences.edit().putInt(KEY_RENDER_DISTANCE, distance).apply()
    
    fun getGraphicsQuality(): String = sharedPreferences.getString(KEY_GRAPHICS_QUALITY, DEFAULT_GRAPHICS_QUALITY) ?: DEFAULT_GRAPHICS_QUALITY
    fun setGraphicsQuality(quality: String) = sharedPreferences.edit().putString(KEY_GRAPHICS_QUALITY, quality).apply()
    
    // Audio preferences
    fun getMasterVolume(): Int = sharedPreferences.getInt(KEY_MASTER_VOLUME, DEFAULT_MASTER_VOLUME)
    fun setMasterVolume(volume: Int) = sharedPreferences.edit().putInt(KEY_MASTER_VOLUME, volume).apply()
    
    fun getMusicVolume(): Int = sharedPreferences.getInt(KEY_MUSIC_VOLUME, DEFAULT_MUSIC_VOLUME)
    fun setMusicVolume(volume: Int) = sharedPreferences.edit().putInt(KEY_MUSIC_VOLUME, volume).apply()
    
    fun getSoundVolume(): Int = sharedPreferences.getInt(KEY_SOUND_VOLUME, DEFAULT_SOUND_VOLUME)
    fun setSoundVolume(volume: Int) = sharedPreferences.edit().putInt(KEY_SOUND_VOLUME, volume).apply()
    
    // First launch
    fun isFirstLaunch(): Boolean = sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
    fun setFirstLaunch(firstLaunch: Boolean) = sharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH, firstLaunch).apply()
    
    // Welcome completed
    fun isWelcomeCompleted(): Boolean = sharedPreferences.getBoolean(KEY_WELCOME_COMPLETED, false)
    fun setWelcomeCompleted(completed: Boolean) = sharedPreferences.edit().putBoolean(KEY_WELCOME_COMPLETED, completed).apply()
    
    // Update check
    fun isUpdateCheckEnabled(): Boolean = sharedPreferences.getBoolean(KEY_UPDATE_CHECK_ENABLED, DEFAULT_UPDATE_CHECK_ENABLED)
    fun setUpdateCheckEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean(KEY_UPDATE_CHECK_ENABLED, enabled).apply()
    
    fun getLastUpdateCheck(): Long = sharedPreferences.getLong(KEY_LAST_UPDATE_CHECK, 0)
    fun setLastUpdateCheck(timestamp: Long) = sharedPreferences.edit().putLong(KEY_LAST_UPDATE_CHECK, timestamp).apply()
    
    // Notifications
    fun areNotificationsEnabled(): Boolean = sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, DEFAULT_NOTIFICATIONS_ENABLED)
    fun setNotificationsEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    
    // Download preferences
    fun isDownloadWifiOnly(): Boolean = sharedPreferences.getBoolean(KEY_DOWNLOAD_WIFI_ONLY, DEFAULT_DOWNLOAD_WIFI_ONLY)
    fun setDownloadWifiOnly(wifiOnly: Boolean) = sharedPreferences.edit().putBoolean(KEY_DOWNLOAD_WIFI_ONLY, wifiOnly).apply()
    
    // Language
    fun getLanguage(): String = sharedPreferences.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    fun setLanguage(language: String) = sharedPreferences.edit().putString(KEY_LANGUAGE, language).apply()
    
    // Touch controls
    fun areTouchControlsEnabled(): Boolean = sharedPreferences.getBoolean(KEY_TOUCH_CONTROLS_ENABLED, DEFAULT_TOUCH_CONTROLS_ENABLED)
    fun setTouchControlsEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean(KEY_TOUCH_CONTROLS_ENABLED, enabled).apply()
    
    fun getTouchControlsConfig(): TouchControlsConfig? {
        val json = sharedPreferences.getString(KEY_TOUCH_CONTROLS_CONFIG, null)
        return if (json != null) {
            try {
                gson.fromJson(json, TouchControlsConfig::class.java)
            } catch (e: Exception) {
                TouchControlsConfig.getDefault()
            }
        } else {
            TouchControlsConfig.getDefault()
        }
    }
    
    fun setTouchControlsConfig(config: TouchControlsConfig) = sharedPreferences.edit().putString(KEY_TOUCH_CONTROLS_CONFIG, gson.toJson(config)).apply()
    
    // Additional getters and setters for SettingsFragment
    fun getSensitivity(): Int = sharedPreferences.getInt(KEY_SENSITIVITY, DEFAULT_SENSITIVITY)
    fun setSensitivity(sensitivity: Int) = sharedPreferences.edit().putInt(KEY_SENSITIVITY, sensitivity).apply()
    
    fun getProfileName(): String = sharedPreferences.getString(KEY_PROFILE_NAME, DEFAULT_PROFILE_NAME) ?: DEFAULT_PROFILE_NAME
    fun setProfileName(name: String) = sharedPreferences.edit().putString(KEY_PROFILE_NAME, name).apply()
    
    fun getSelectedVersion(): String = sharedPreferences.getString(KEY_SELECTED_VERSION, DEFAULT_SELECTED_VERSION) ?: DEFAULT_SELECTED_VERSION
    fun setSelectedVersion(version: String) = sharedPreferences.edit().putString(KEY_SELECTED_VERSION, version).apply()
    
    fun getJavaVersion(): String = sharedPreferences.getString(KEY_JAVA_VERSION, DEFAULT_JAVA_VERSION) ?: DEFAULT_JAVA_VERSION
    fun setJavaVersion(version: String) = sharedPreferences.edit().putString(KEY_JAVA_VERSION, version).apply()
    
    // Clear all preferences
    fun clearAll() = sharedPreferences.edit().clear().apply()
    
    // Data classes for accounts and profiles
    data class Account(
        val username: String,
        val type: String, // "microsoft", "mojang", "offline"
        val accessToken: String? = null,
        val refreshToken: String? = null,
        val expiresAt: Long? = null,
        val uuid: String? = null,
        val displayName: String? = null,
        val skinUrl: String? = null,
        val capeUrl: String? = null
    )
    
    data class Profile(
        val name: String,
        val minecraftVersion: String,
        val javaPath: String? = null,
        val javaArgs: String = DEFAULT_JAVA_ARGS,
        val gameDirectory: String? = null,
        val mods: List<String> = emptyList(),
        val resourcePacks: List<String> = emptyList(),
        val shaders: List<String> = emptyList(),
        val jvmArgs: List<String> = emptyList(),
        val lastPlayed: Long = 0,
        val playTime: Long = 0
    )
    
    data class TouchControlsConfig(
        val joystick: ControlConfig = ControlConfig(),
        val jumpButton: ControlConfig = ControlConfig(),
        val sneakButton: ControlConfig = ControlConfig(),
        val attackButton: ControlConfig = ControlConfig(),
        val useButton: ControlConfig = ControlConfig(),
        val inventoryButton: ControlConfig = ControlConfig(),
        val dropButton: ControlConfig = ControlConfig(),
        val sprintButton: ControlConfig = ControlConfig(),
        val flyButton: ControlConfig = ControlConfig(),
        val hotbar: ControlConfig = ControlConfig(),
        val chatButton: ControlConfig = ControlConfig(),
        val commandButton: ControlConfig = ControlConfig(),
        val pauseButton: ControlConfig = ControlConfig(),
        val settingsButton: ControlConfig = ControlConfig()
    ) {
        companion object {
            fun getDefault(): TouchControlsConfig {
                return TouchControlsConfig()
            }
        }
    }
    
    data class ControlConfig(
        val x: Float = 0f,
        val y: Float = 0f,
        val width: Float = 60f,
        val height: Float = 60f,
        val opacity: Float = 1f,
        val scale: Float = 1f,
        val visible: Boolean = true,
        val icon: String = "",
        val label: String = ""
    )
}
