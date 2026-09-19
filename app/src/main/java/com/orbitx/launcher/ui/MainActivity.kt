package com.orbitx.launcher.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.orbitx.launcher.R
import com.orbitx.launcher.databinding.ActivityMainBinding
import com.orbitx.launcher.ui.browser.ModBrowserFragment
import com.orbitx.launcher.ui.browser.ResourcePackBrowserFragment
import com.orbitx.launcher.ui.browser.ShaderBrowserFragment
import com.orbitx.launcher.ui.game.LaunchFragment
import com.orbitx.launcher.ui.home.HomeFragment
import com.orbitx.launcher.ui.settings.SettingsFragment
import com.orbitx.launcher.utils.Logging

/**
 * MainActivity - The main activity for OrbitX Launcher
 * Handles navigation between different fragments
 */
class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
        private const val TAG_HOME = "home"
        private const val TAG_LAUNCH = "launch"
        private const val TAG_MODS = "mods"
        private const val TAG_RESOURCE_PACKS = "resource_packs"
        private const val TAG_SHADERS = "shaders"
        private const val TAG_SETTINGS = "settings"
        
        private var currentFragmentTag: String? = null
    }
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Logging.d(TAG, "MainActivity created")
        
        // Initialize view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize view model
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        
        // Setup UI
        setupUI()
        
        // Check for first launch
        checkFirstLaunch()
    }
    
    /**
     * Setup UI components
     */
    private fun setupUI() {
        // Setup bottom navigation
        setupBottomNavigation()
        
        // Setup toolbar
        setupToolbar()
        
        // Load initial fragment
        if (savedInstanceState == null) {
            loadFragment(HomeFragment.newInstance(), TAG_HOME)
        }
    }
    
    /**
     * Setup bottom navigation view
     */
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment.newInstance(), TAG_HOME)
                    true
                }
                R.id.nav_launch -> {
                    loadFragment(LaunchFragment.newInstance(), TAG_LAUNCH)
                    true
                }
                R.id.nav_mods -> {
                    loadFragment(ModBrowserFragment.newInstance(), TAG_MODS)
                    true
                }
                R.id.nav_resource_packs -> {
                    loadFragment(ResourcePackBrowserFragment.newInstance(), TAG_RESOURCE_PACKS)
                    true
                }
                R.id.nav_shaders -> {
                    loadFragment(ShaderBrowserFragment.newInstance(), TAG_SHADERS)
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(SettingsFragment.newInstance(), TAG_SETTINGS)
                    true
                }
                else -> false
            }
        }
    }
    
    /**
     * Setup toolbar
     */
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let { actionBar ->
            actionBar.setDisplayShowTitleEnabled(false)
        }
        
        // Set title based on current fragment
        updateTitle(TAG_HOME)
    }
    
    /**
     * Load a fragment
     */
    private fun loadFragment(fragment: Fragment, tag: String) {
        if (currentFragmentTag == tag && supportFragmentManager.fragments.size > 0) {
            return
        }
        
        currentFragmentTag = tag
        
        supportFragmentManager.commit(true) {
            setCustomAnimations(
                R.anim.fade_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.fade_out
            )
            replace(R.id.fragment_container, fragment, tag)
            addToBackStack(tag)
        }
        
        updateTitle(tag)
        updateBottomNavigation(tag)
    }
    
    /**
     * Update title based on current fragment
     */
    private fun updateTitle(tag: String) {
        val title = when (tag) {
            TAG_HOME -> getString(R.string.app_name)
            TAG_LAUNCH -> getString(R.string.launch)
            TAG_MODS -> getString(R.string.mods)
            TAG_RESOURCE_PACKS -> getString(R.string.resource_packs)
            TAG_SHADERS -> getString(R.string.shaders)
            TAG_SETTINGS -> getString(R.string.settings)
            else -> getString(R.string.app_name)
        }
        
        binding.toolbarTitle.text = title
    }
    
    /**
     * Update bottom navigation selection
     */
    private fun updateBottomNavigation(tag: String) {
        val itemId = when (tag) {
            TAG_HOME -> R.id.nav_home
            TAG_LAUNCH -> R.id.nav_launch
            TAG_MODS -> R.id.nav_mods
            TAG_RESOURCE_PACKS -> R.id.nav_resource_packs
            TAG_SHADERS -> R.id.nav_shaders
            TAG_SETTINGS -> R.id.nav_settings
            else -> R.id.nav_home
        }
        
        binding.bottomNavigation.selectedItemId = itemId
    }
    
    /**
     * Check if this is the first launch
     */
    private fun checkFirstLaunch() {
        val preferencesManager = com.orbitx.launcher.core.OrbitXApplication.getInstance().getPreferencesManager()
        
        if (preferencesManager.isFirstLaunch()) {
            preferencesManager.setFirstLaunch(false)
            
            // Show welcome screen or tutorial
            // For now, just show a toast
            Toast.makeText(this, R.string.welcome, Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * Show progress
     */
    fun showProgress(show: Boolean) {
        if (show) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }
    
    /**
     * Show error message
     */
    fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    /**
     * Show success message
     */
    fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Navigate to a specific fragment
     */
    fun navigateTo(tag: String) {
        when (tag) {
            TAG_HOME -> loadFragment(HomeFragment.newInstance(), TAG_HOME)
            TAG_LAUNCH -> loadFragment(LaunchFragment.newInstance(), TAG_LAUNCH)
            TAG_MODS -> loadFragment(ModBrowserFragment.newInstance(), TAG_MODS)
            TAG_RESOURCE_PACKS -> loadFragment(ResourcePackBrowserFragment.newInstance(), TAG_RESOURCE_PACKS)
            TAG_SHADERS -> loadFragment(ShaderBrowserFragment.newInstance(), TAG_SHADERS)
            TAG_SETTINGS -> loadFragment(SettingsFragment.newInstance(), TAG_SETTINGS)
        }
    }
    
    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager
        
        // Check if we can go back in the current fragment
        if (fragmentManager.backStackEntryCount > 1) {
            val currentFragment = fragmentManager.findFragmentByTag(currentFragmentTag)
            if (currentFragment is BaseFragment && currentFragment.onBackPressed()) {
                return
            }
        }
        
        // If we're not on the home screen, go to home
        if (currentFragmentTag != TAG_HOME) {
            loadFragment(HomeFragment.newInstance(), TAG_HOME)
            return
        }
        
        // Default back behavior
        super.onBackPressed()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Logging.d(TAG, "MainActivity destroyed")
    }
    
    /**
     * Extension function for FragmentManager to commit with animation
     */
    private fun FragmentManager.commit(allowStateLoss: Boolean, action: () -> Unit) {
        if (allowStateLoss) {
            beginTransaction()
                .apply { action() }
                .commitAllowingStateLoss()
        } else {
            beginTransaction()
                .apply { action() }
                .commit()
        }
    }
}

/**
 * MainViewModel - ViewModel for MainActivity
 */
class MainViewModel : androidx.lifecycle.ViewModel() {
    // ViewModel for shared data between fragments
    // Can be extended as needed
}

/**
 * BaseFragment - Base class for all fragments in the app
 */
abstract class BaseFragment : androidx.fragment.app.Fragment() {
    
    open fun onBackPressed(): Boolean {
        return false
    }
    
    protected fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    
    protected fun showLongToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}
