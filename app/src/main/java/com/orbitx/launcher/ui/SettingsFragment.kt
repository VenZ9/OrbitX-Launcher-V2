package com.orbitx.launcher.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.orbitx.launcher.databinding.FragmentSettingsBinding
import com.orbitx.launcher.ui.BaseFragment
import com.orbitx.launcher.utils.Logging
import com.orbitx.launcher.utils.PreferencesManager

/**
 * SettingsFragment - Fragment for app settings
 * Includes Java settings, game settings, controls, and credits
 */
class SettingsFragment : BaseFragment() {
    
    companion object {
        private const val TAG = "SettingsFragment"
        
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
    
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var viewModel: SettingsViewModel
    private lateinit var prefs: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logging.d(TAG, "SettingsFragment created")
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize view model
        viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        
        // Initialize preferences
        prefs = PreferencesManager.getInstance(requireContext())
        
        // Setup UI
        setupUI()
        
        // Load settings
        loadSettings()
    }
    
    /**
     * Setup UI components
     */
    private fun setupUI() {
        setupToolbar()
        setupJavaSettings()
        setupGameSettings()
        setupControlsSettings()
        setupAboutSection()
    }
    
    /**
     * Setup toolbar
     */
    private fun setupToolbar() {
        binding.toolbar.title = getString(com.orbitx.launcher.R.string.settings)
    }
    
    /**
     * Setup Java settings
     */
    private fun setupJavaSettings() {
        binding.buttonSelectJava.setOnClickListener {
            showJavaSelector()
        }
        
        binding.buttonEditMemory.setOnClickListener {
            showMemoryEditor()
        }
    }
    
    /**
     * Setup game settings
     */
    private fun setupGameSettings() {
        binding.buttonSelectGameDir.setOnClickListener {
            showGameDirectorySelector()
        }
        
        binding.switchFullscreen.setOnCheckedChangeListener { _, isChecked ->
            prefs.putBoolean(PreferencesManager.KEY_FULLSCREEN, isChecked)
        }
        
        binding.switchFpsCounter.setOnCheckedChangeListener { _, isChecked ->
            prefs.putBoolean(PreferencesManager.KEY_SHOW_FPS, isChecked)
        }
    }
    
    /**
     * Setup controls settings
     */
    private fun setupControlsSettings() {
        binding.buttonCustomizeControls.setOnClickListener {
            showControlsCustomizer()
        }
        
        binding.buttonEditSensitivity.setOnClickListener {
            showSensitivityEditor()
        }
    }
    
    /**
     * Setup about section
     */
    private fun setupAboutSection() {
        // Update app version
        try {
            val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            val versionName = packageInfo.versionName
            val versionCode = packageInfo.versionCode
            binding.textViewAppVersion.text = "OrbitX Launcher v$versionName (Build: $versionCode)"
        } catch (e: Exception) {
            Logging.e(TAG, "Failed to get package info", e)
            binding.textViewAppVersion.text = "OrbitX Launcher v1.0.0"
        }
    }
    
    /**
     * Load settings
     */
    private fun loadSettings() {
        // Load Java settings
        val javaPath = prefs.getString(PreferencesManager.KEY_JAVA_PATH, "OpenJDK 17")
        binding.textViewJavaPath.text = javaPath
        
        val memoryAllocation = prefs.getString(PreferencesManager.KEY_MEMORY_ALLOCATION, "2GB")
        binding.textViewMemoryAllocation.text = memoryAllocation
        
        // Load game settings
        val gameDirectory = prefs.getString(PreferencesManager.KEY_GAME_DIRECTORY, "/orbitx/game")
        binding.textViewGameDirectory.text = gameDirectory
        
        val fullscreen = prefs.getBoolean(PreferencesManager.KEY_FULLSCREEN, true)
        binding.switchFullscreen.isChecked = fullscreen
        
        val showFps = prefs.getBoolean(PreferencesManager.KEY_SHOW_FPS, false)
        binding.switchFpsCounter.isChecked = showFps
        
        // Load controls settings
        val sensitivity = prefs.getInt(PreferencesManager.KEY_SENSITIVITY, 50)
        binding.textViewSensitivityValue.text = "$sensitivity%"
    }
    
    /**
     * Show Java selector
     */
    private fun showJavaSelector() {
        showToast("Java selector")
    }
    
    /**
     * Show memory editor
     */
    private fun showMemoryEditor() {
        showToast("Memory editor")
    }
    
    /**
     * Show game directory selector
     */
    private fun showGameDirectorySelector() {
        showToast("Game directory selector")
    }
    
    /**
     * Show controls customizer
     */
    private fun showControlsCustomizer() {
        showToast("Controls customizer")
    }
    
    /**
     * Show sensitivity editor
     */
    private fun showSensitivityEditor() {
        showToast("Sensitivity editor")
    }
    
    override fun onResume() {
        super.onResume()
        Logging.d(TAG, "SettingsFragment resumed")
        loadSettings()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        Logging.d(TAG, "SettingsFragment view destroyed")
    }
}

/**
 * SettingsViewModel - ViewModel for SettingsFragment
 */
class SettingsViewModel : androidx.lifecycle.ViewModel() {
    // ViewModel for settings data
}
