package com.orbitx.launcher.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.orbitx.launcher.core.OrbitXApplication
import com.orbitx.launcher.databinding.FragmentHomeBinding
import com.orbitx.launcher.manager.MinecraftManager
import com.orbitx.launcher.model.MinecraftVersion
import com.orbitx.launcher.ui.BaseFragment
import com.orbitx.launcher.ui.MainActivity
import com.orbitx.launcher.utils.Logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * HomeFragment - Main home screen for OrbitX Launcher
 * Shows Minecraft versions, news, and quick actions
 */
class HomeFragment : BaseFragment() {
    
    companion object {
        private const val TAG = "HomeFragment"
        
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }
    
    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var minecraftManager: MinecraftManager
    private lateinit var versionAdapter: VersionAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logging.d(TAG, "HomeFragment created")
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize view model
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        
        // Initialize managers
        val app = OrbitXApplication.getInstance()
        minecraftManager = MinecraftManager.getInstance(requireContext())
        
        // Setup UI
        setupUI()
        
        // Load data
        loadData()
    }
    
    /**
     * Setup UI components
     */
    private fun setupUI() {
        setupRecyclerView()
        setupRefreshLayout()
        setupClickListeners()
    }
    
    /**
     * Setup RecyclerView for Minecraft versions
     */
    private fun setupRecyclerView() {
        versionAdapter = VersionAdapter(
            onVersionClick = { version ->
                onVersionClicked(version)
            },
            onInstallClick = { version ->
                onInstallClicked(version)
            },
            onLaunchClick = { version ->
                onLaunchClicked(version)
            },
            onUninstallClick = { version ->
                onUninstallClicked(version)
            }
        )
        
        binding.recyclerViewVersions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = versionAdapter
            setHasFixedSize(true)
        }
    }
    
    /**
     * Setup SwipeRefreshLayout
     */
    private fun setupRefreshLayout() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadData()
        }
        
        binding.swipeRefreshLayout.setColorSchemeColors(
            com.orbitx.launcher.R.color.orbitx_primary,
            com.orbitx.launcher.R.color.orbitx_secondary
        )
    }
    
    /**
     * Setup click listeners
     */
    private fun setupClickListeners() {
        binding.buttonInstallJava.setOnClickListener {
            onInstallJavaClicked()
        }
        
        binding.buttonNewProfile.setOnClickListener {
            onNewProfileClicked()
        }
        
        binding.cardNews.setOnClickListener {
            onNewsClicked()
        }
        
        binding.cardQuickActions.setOnClickListener {
            onQuickActionsClicked()
        }
    }
    
    /**
     * Load data
     */
    private fun loadData() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                binding.swipeRefreshLayout.isRefreshing = true
                
                // Load installed versions
                val installedVersions = minecraftManager.getInstalledVersions()
                
                // Load available versions
                val availableVersions = minecraftManager.getAvailableVersions()
                
                // Update UI
                updateVersions(installedVersions, availableVersions)
                
                // Check Java installation
                checkJavaInstallation()
                
            } catch (e: Exception) {
                Logging.e(TAG, "Failed to load data", e)
                showToast(getString(com.orbitx.launcher.R.string.error))
            } finally {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }
    
    /**
     * Update versions in the UI
     */
    private fun updateVersions(installed: List<MinecraftVersion>, available: List<MinecraftVersion>) {
        // Combine installed and available versions
        val allVersions = mutableListOf<VersionItem>()
        
        // Add installed versions
        installed.forEach { version ->
            allVersions.add(
                VersionItem(
                    version = version,
                    isInstalled = true,
                    isSelected = false
                )
            )
        }
        
        // Add available versions that are not installed
        available.filter { v -> installed.none { it.id == v.id } }.forEach { version ->
            allVersions.add(
                VersionItem(
                    version = version,
                    isInstalled = false,
                    isSelected = false
                )
            )
        }
        
        versionAdapter.submitList(allVersions)
        
        // Update empty state
        if (allVersions.isEmpty()) {
            binding.textViewEmptyState.visibility = View.VISIBLE
            binding.recyclerViewVersions.visibility = View.GONE
        } else {
            binding.textViewEmptyState.visibility = View.GONE
            binding.recyclerViewVersions.visibility = View.VISIBLE
        }
    }
    
    /**
     * Check Java installation
     */
    private fun checkJavaInstallation() {
        val app = OrbitXApplication.getInstance()
        val preferencesManager = app.getPreferencesManager()
        
        val javaPath = preferencesManager.getJavaPath()
        
        if (javaPath.isNullOrEmpty()) {
            binding.textViewJavaStatus.text = getString(com.orbitx.launcher.R.string.java_required)
            binding.textViewJavaStatus.setTextColor(
                resources.getColor(com.orbitx.launcher.R.color.orbitx_error, null)
            )
            binding.buttonInstallJava.visibility = View.VISIBLE
        } else {
            binding.textViewJavaStatus.text = getString(com.orbitx.launcher.R.string.java_version) + ": " + javaPath
            binding.textViewJavaStatus.setTextColor(
                resources.getColor(com.orbitx.launcher.R.color.orbitx_success, null)
            )
            binding.buttonInstallJava.visibility = View.GONE
        }
    }
    
    /**
     * Handle version click
     */
    private fun onVersionClicked(version: MinecraftVersion) {
        // Show version details or options
        showVersionOptions(version)
    }
    
    /**
     * Handle install click
     */
    private fun onInstallClicked(version: MinecraftVersion) {
        CoroutineScope(Dispatchers.Main).launch {
            minecraftManager.installVersion(version.id) { success, message ->
                if (success) {
                    showToast(message ?: getString(com.orbitx.launcher.R.string.success_installed))
                    loadData()
                } else {
                    showToast(message ?: getString(com.orbitx.launcher.R.string.failed))
                }
            }
        }
    }
    
    /**
     * Handle launch click
     */
    private fun onLaunchClicked(version: MinecraftVersion) {
        // Navigate to launch screen with selected version
        val activity = requireActivity() as? MainActivity
        activity?.navigateTo(MainActivity.TAG_LAUNCH)
    }
    
    /**
     * Handle uninstall click
     */
    private fun onUninstallClicked(version: MinecraftVersion) {
        minecraftManager.uninstallVersion(version.id) { success, message ->
            if (success) {
                showToast(message ?: getString(com.orbitx.launcher.R.string.success_removed))
                loadData()
            } else {
                showToast(message ?: getString(com.orbitx.launcher.R.string.failed))
            }
        }
    }
    
    /**
     * Show version options dialog
     */
    private fun showVersionOptions(version: MinecraftVersion) {
        // Implementation would show a dialog with version options
        // For now, just show a toast
        showToast("Options for ${version.id}")
    }
    
    /**
     * Handle install Java click
     */
    private fun onInstallJavaClicked() {
        // Show Java installation guide or download options
        showToast(getString(com.orbitx.launcher.R.string.java_required))
    }
    
    /**
     * Handle new profile click
     */
    private fun onNewProfileClicked() {
        // Navigate to profile creation screen
        showToast("Create new profile")
    }
    
    /**
     * Handle news click
     */
    private fun onNewsClicked() {
        // Navigate to news screen
        showToast("News")
    }
    
    /**
     * Handle quick actions click
     */
    private fun onQuickActionsClicked() {
        // Show quick actions dialog
        showToast("Quick actions")
    }
    
    override fun onResume() {
        super.onResume()
        Logging.d(TAG, "HomeFragment resumed")
        loadData()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        Logging.d(TAG, "HomeFragment view destroyed")
    }
    
    /**
     * Data class for version items in the list
     */
    data class VersionItem(
        val version: MinecraftVersion,
        val isInstalled: Boolean,
        val isSelected: Boolean
    )
    
    /**
     * Adapter for version list
     */
    inner class VersionAdapter(
        private val onVersionClick: (MinecraftVersion) -> Unit,
        private val onInstallClick: (MinecraftVersion) -> Unit,
        private val onLaunchClick: (MinecraftVersion) -> Unit,
        private val onUninstallClick: (MinecraftVersion) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<VersionViewHolder>() {
        
        private val items = mutableListOf<VersionItem>()
        
        fun submitList(newItems: List<VersionItem>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VersionViewHolder {
            val binding = com.orbitx.launcher.databinding.ItemVersionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return VersionViewHolder(binding)
        }
        
        override fun onBindViewHolder(holder: VersionViewHolder, position: Int) {
            val item = items[position]
            holder.bind(item, onVersionClick, onInstallClick, onLaunchClick, onUninstallClick)
        }
        
        override fun getItemCount(): Int = items.size
    }
    
    /**
     * ViewHolder for version items
     */
    inner class VersionViewHolder(private val binding: com.orbitx.launcher.databinding.ItemVersionBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        
        fun bind(
            item: VersionItem,
            onVersionClick: (MinecraftVersion) -> Unit,
            onInstallClick: (MinecraftVersion) -> Unit,
            onLaunchClick: (MinecraftVersion) -> Unit,
            onUninstallClick: (MinecraftVersion) -> Unit
        ) {
            val version = item.version
            
            binding.textViewVersionName.text = version.getDisplayName()
            binding.textViewVersionType.text = version.getTypeDisplayName()
            binding.textViewVersionDate.text = version.releaseTime
            
            if (item.isInstalled) {
                binding.buttonInstall.text = getString(com.orbitx.launcher.R.string.launch)
                binding.buttonInstall.setOnClickListener { onLaunchClick(version) }
                binding.buttonUninstall.visibility = View.VISIBLE
                binding.buttonUninstall.setOnClickListener { onUninstallClick(version) }
            } else {
                binding.buttonInstall.text = getString(com.orbitx.launcher.R.string.install)
                binding.buttonInstall.setOnClickListener { onInstallClick(version) }
                binding.buttonUninstall.visibility = View.GONE
            }
            
            binding.root.setOnClickListener { onVersionClick(version) }
        }
    }
}

/**
 * HomeViewModel - ViewModel for HomeFragment
 */
class HomeViewModel : androidx.lifecycle.ViewModel() {
    // ViewModel for home screen data
    // Can be extended as needed
}
