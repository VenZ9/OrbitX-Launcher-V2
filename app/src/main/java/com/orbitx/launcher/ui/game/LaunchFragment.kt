package com.orbitx.launcher.ui.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.orbitx.launcher.core.OrbitXApplication
import com.orbitx.launcher.databinding.FragmentLaunchBinding
import com.orbitx.launcher.manager.MinecraftManager
import com.orbitx.launcher.model.MinecraftVersion
import com.orbitx.launcher.ui.BaseFragment
import com.orbitx.launcher.utils.Logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * LaunchFragment - Fragment for launching Minecraft
 * Handles profile selection, version selection, and game launching
 */
class LaunchFragment : BaseFragment() {
    
    companion object {
        private const val TAG = "LaunchFragment"
        
        fun newInstance(): LaunchFragment {
            return LaunchFragment()
        }
    }
    
    private lateinit var binding: FragmentLaunchBinding
    private lateinit var viewModel: LaunchViewModel
    private lateinit var minecraftManager: MinecraftManager
    
    private var installedVersions = emptyList<MinecraftVersion>()
    private var recentVersions = emptyList<MinecraftVersion>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logging.d(TAG, "LaunchFragment created")
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLaunchBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize view model
        viewModel = ViewModelProvider(this)[LaunchViewModel::class.java]
        
        // Initialize managers
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
        setupToolbar()
        setupProfileCard()
        setupLaunchButton()
        setupQuickActions()
        setupRecentVersions()
    }
    
    /**
     * Setup toolbar
     */
    private fun setupToolbar() {
        binding.toolbar.title = getString(com.orbitx.launcher.R.string.launch)
    }
    
    /**
     * Setup profile card
     */
    private fun setupProfileCard() {
        binding.buttonEditProfile.setOnClickListener {
            showProfileEditor()
        }
    }
    
    /**
     * Setup launch button
     */
    private fun setupLaunchButton() {
        binding.buttonLaunch.setOnClickListener {
            launchGame()
        }
        
        binding.fabLaunch.setOnClickListener {
            launchGame()
        }
    }
    
    /**
     * Setup quick actions
     */
    private fun setupQuickActions() {
        // Find all action cards and set click listeners
        val createProfileCard = binding.gridQuickActions.getChildAt(0)
        val importCard = binding.gridQuickActions.getChildAt(1)
        val exportCard = binding.gridQuickActions.getChildAt(2)
        val deleteCard = binding.gridQuickActions.getChildAt(3)
        
        createProfileCard?.setOnClickListener { showCreateProfileDialog() }
        importCard?.setOnClickListener { showImportProfileDialog() }
        exportCard?.setOnClickListener { showExportProfileDialog() }
        deleteCard?.setOnClickListener { showDeleteProfileDialog() }
    }
    
    /**
     * Setup recent versions RecyclerView
     */
    private fun setupRecentVersions() {
        val adapter = RecentVersionsAdapter(
            onVersionClick = { version ->
                selectVersion(version)
            },
            onVersionInstall = { version ->
                installVersion(version)
            }
        )
        
        binding.recyclerViewRecentVersions.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = adapter
            setHasFixedSize(true)
        }
    }
    
    /**
     * Load data
     */
    private fun loadData() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Load installed versions
                installedVersions = minecraftManager.getInstalledVersions()
                
                // Load recent versions (last 5 installed)
                recentVersions = installedVersions.takeLast(5).reversed()
                
                // Update UI
                updateRecentVersions()
                updateProfileInfo()
                
            } catch (e: Exception) {
                Logging.e(TAG, "Failed to load data", e)
                showToast(getString(com.orbitx.launcher.R.string.error))
            }
        }
    }
    
    /**
     * Update recent versions in the UI
     */
    private fun updateRecentVersions() {
        val adapter = binding.recyclerViewRecentVersions.adapter as? RecentVersionsAdapter
        adapter?.submitList(recentVersions)
        
        // Update empty state
        if (recentVersions.isEmpty()) {
            binding.textViewRecentVersionsLabel.visibility = View.GONE
            binding.recyclerViewRecentVersions.visibility = View.GONE
        } else {
            binding.textViewRecentVersionsLabel.visibility = View.VISIBLE
            binding.recyclerViewRecentVersions.visibility = View.VISIBLE
        }
    }
    
    /**
     * Update profile info
     */
    private fun updateProfileInfo() {
        // Get current profile from preferences
        val prefs = com.orbitx.launcher.utils.PreferencesManager.getInstance(requireContext())
        val profileName = prefs.getString(com.orbitx.launcher.utils.PreferencesManager.KEY_PROFILE_NAME, "Default Profile")
        val selectedVersion = prefs.getString(com.orbitx.launcher.utils.PreferencesManager.KEY_SELECTED_VERSION, "1.20.4")
        val javaVersion = prefs.getString(com.orbitx.launcher.utils.PreferencesManager.KEY_JAVA_VERSION, "OpenJDK 17")
        val memoryAllocation = prefs.getString(com.orbitx.launcher.utils.PreferencesManager.KEY_MEMORY_ALLOCATION, "2GB")
        
        binding.textViewProfileName.text = profileName
        binding.textViewProfileVersion.text = "Minecraft $selectedVersion"
        
        // Update profile details
        val profileDetails = binding.cardProfile.findViewById<View>(com.orbitx.launcher.R.id.text_view_profile_details) as? android.widget.LinearLayout
        profileDetails?.let { details ->
            val javaText = details.getChildAt(0) as? android.widget.TextView
            val memoryText = details.getChildAt(2) as? android.widget.TextView
            javaText?.text = "Java: $javaVersion"
            memoryText?.text = "Memory: $memoryAllocation"
        }
    }
    
    /**
     * Launch the game
     */
    private fun launchGame() {
        // Get current profile settings
        val prefs = com.orbitx.launcher.utils.PreferencesManager.getInstance(requireContext())
        val selectedVersion = prefs.getString(com.orbitx.launcher.utils.PreferencesManager.KEY_SELECTED_VERSION, "1.20.4")
        
        if (selectedVersion.isNullOrEmpty()) {
            showToast(getString(com.orbitx.launcher.R.string.error_no_version))
            return
        }
        
        // Check if Java is available
        if (!minecraftManager.isJavaAvailable()) {
            showToast(getString(com.orbitx.launcher.R.string.error_no_java))
            return
        }
        
        // Launch the game
        showToast(getString(com.orbitx.launcher.R.string.preparing))
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                minecraftManager.launchMinecraft(selectedVersion) { success, message ->
                    if (success) {
                        showToast(message ?: getString(com.orbitx.launcher.R.string.launching))
                    } else {
                        showToast(message ?: getString(com.orbitx.launcher.R.string.error_launch_failed))
                    }
                }
            } catch (e: Exception) {
                Logging.e(TAG, "Failed to launch game", e)
                showToast(getString(com.orbitx.launcher.R.string.error_launch_failed))
            }
        }
    }
    
    /**
     * Select a version
     */
    private fun selectVersion(version: MinecraftVersion) {
        // Save selected version
        val prefs = com.orbitx.launcher.utils.PreferencesManager.getInstance(requireContext())
        prefs.putString(com.orbitx.launcher.utils.PreferencesManager.KEY_SELECTED_VERSION, version.id)
        
        // Update profile info
        updateProfileInfo()
        
        showToast("Selected: ${version.id}")
    }
    
    /**
     * Install a version
     */
    private fun installVersion(version: MinecraftVersion) {
        showToast("Installing: ${version.id}")
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                minecraftManager.installVersion(version) { success, message ->
                    if (success) {
                        showToast(message ?: getString(com.orbitx.launcher.R.string.success_installed))
                        loadData()
                    } else {
                        showToast(message ?: getString(com.orbitx.launcher.R.string.failed))
                    }
                }
            } catch (e: Exception) {
                Logging.e(TAG, "Failed to install version", e)
                showToast(getString(com.orbitx.launcher.R.string.failed))
            }
        }
    }
    
    /**
     * Show profile editor
     */
    private fun showProfileEditor() {
        showToast("Profile editor")
    }
    
    /**
     * Show create profile dialog
     */
    private fun showCreateProfileDialog() {
        showToast("Create profile")
    }
    
    /**
     * Show import profile dialog
     */
    private fun showImportProfileDialog() {
        showToast("Import profile")
    }
    
    /**
     * Show export profile dialog
     */
    private fun showExportProfileDialog() {
        showToast("Export profile")
    }
    
    /**
     * Show delete profile dialog
     */
    private fun showDeleteProfileDialog() {
        showToast("Delete profile")
    }
    
    override fun onResume() {
        super.onResume()
        Logging.d(TAG, "LaunchFragment resumed")
        loadData()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        Logging.d(TAG, "LaunchFragment view destroyed")
    }
    
    /**
     * Adapter for recent versions
     */
    inner class RecentVersionsAdapter(
        private val onVersionClick: (MinecraftVersion) -> Unit,
        private val onVersionInstall: (MinecraftVersion) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<RecentVersionViewHolder>() {
        
        private val items = mutableListOf<MinecraftVersion>()
        
        fun submitList(newItems: List<MinecraftVersion>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentVersionViewHolder {
            val binding = com.orbitx.launcher.databinding.ItemVersionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return RecentVersionViewHolder(binding)
        }
        
        override fun onBindViewHolder(holder: RecentVersionViewHolder, position: Int) {
            val version = items[position]
            holder.bind(version, onVersionClick, onVersionInstall)
        }
        
        override fun getItemCount(): Int = items.size
    }
    
    /**
     * ViewHolder for recent version items
     */
    inner class RecentVersionViewHolder(private val binding: com.orbitx.launcher.databinding.ItemVersionBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        
        fun bind(
            version: MinecraftVersion,
            onVersionClick: (MinecraftVersion) -> Unit,
            onVersionInstall: (MinecraftVersion) -> Unit
        ) {
            binding.textViewVersionName.text = version.getDisplayName()
            binding.textViewVersionType.text = version.getTypeDisplayName()
            binding.textViewVersionDate.text = version.releaseTime
            
            // Hide buttons for recent versions list (simplified view)
            binding.buttonInstall.visibility = View.GONE
            binding.buttonUninstall.visibility = View.GONE
            
            binding.root.setOnClickListener { onVersionClick(version) }
        }
    }
}

/**
 * LaunchViewModel - ViewModel for LaunchFragment
 */
class LaunchViewModel : androidx.lifecycle.ViewModel() {
    // ViewModel for launch data
}
