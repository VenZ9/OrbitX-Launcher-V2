package com.orbitx.launcher.ui.browser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.orbitx.launcher.core.OrbitXApplication
import com.orbitx.launcher.databinding.FragmentBrowserBinding
import com.orbitx.launcher.manager.ModManager
import com.orbitx.launcher.model.Mod
import com.orbitx.launcher.ui.BaseFragment
import com.orbitx.launcher.utils.Logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ModBrowserFragment - Fragment for browsing and managing mods
 * Similar to Zalith Launcher 2's mod browser
 */
class ModBrowserFragment : BaseFragment() {
    
    companion object {
        private const val TAG = "ModBrowserFragment"
        private const val SOURCE_ALL = "all"
        private const val SOURCE_MODRINTH = "modrinth"
        private const val SOURCE_CURSEFORGE = "curseforge"
        private const val SOURCE_PLANET_MINECRAFT = "planetminecraft"
        
        fun newInstance(): ModBrowserFragment {
            return ModBrowserFragment()
        }
    }
    
    private lateinit var binding: FragmentBrowserBinding
    private lateinit var viewModel: ModBrowserViewModel
    private lateinit var modManager: ModManager
    private lateinit var modAdapter: ModAdapter
    
    private var currentSource = SOURCE_ALL
    private var currentFilter = ModManager.ModFilter()
    private var installedMods = emptyList<Mod>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logging.d(TAG, "ModBrowserFragment created")
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBrowserBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize view model
        viewModel = ViewModelProvider(this)[ModBrowserViewModel::class.java]
        
        // Initialize managers
        modManager = ModManager.getInstance(requireContext())
        
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
        setupRecyclerView()
        setupRefreshLayout()
        setupSearch()
        setupTabs()
        setupClickListeners()
    }
    
    /**
     * Setup toolbar
     */
    private fun setupToolbar() {
        binding.toolbar.title = getString(com.orbitx.launcher.R.string.mods)
    }
    
    /**
     * Setup RecyclerView for mods
     */
    private fun setupRecyclerView() {
        modAdapter = ModAdapter(
            onModClick = { mod ->
                onModClicked(mod)
            },
            onInstallClick = { mod ->
                onInstallClicked(mod)
            },
            onRemoveClick = { mod ->
                onRemoveClicked(mod)
            }
        )
        
        binding.recyclerViewMods.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = modAdapter
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
     * Setup search
     */
    private fun setupSearch() {
        binding.editTextSearch.setOnEditorActionListener { _, _, _ ->
            performSearch()
            true
        }
        
        binding.buttonSearch.setOnClickListener {
            performSearch()
        }
    }
    
    /**
     * Setup tabs for different sources
     */
    private fun setupTabs() {
        binding.chipAll.setOnClickListener { selectSource(SOURCE_ALL) }
        binding.chipModrinth.setOnClickListener { selectSource(SOURCE_MODRINTH) }
        binding.chipCurseforge.setOnClickListener { selectSource(SOURCE_CURSEFORGE) }
        binding.chipPlanetMinecraft.setOnClickListener { selectSource(SOURCE_PLANET_MINECRAFT) }
        
        // Select default tab
        selectSource(SOURCE_ALL)
    }
    
    /**
     * Setup click listeners
     */
    private fun setupClickListeners() {
        binding.buttonFilter.setOnClickListener { showFilterDialog() }
        binding.buttonSort.setOnClickListener { showSortDialog() }
        binding.fabAddMod.setOnClickListener { showAddModDialog() }
    }
    
    /**
     * Select a source
     */
    private fun selectSource(source: String) {
        currentSource = source
        
        // Update chip selection
        binding.chipAll.isChecked = source == SOURCE_ALL
        binding.chipModrinth.isChecked = source == SOURCE_MODRINTH
        binding.chipCurseforge.isChecked = source == SOURCE_CURSEFORGE
        binding.chipPlanetMinecraft.isChecked = source == SOURCE_PLANET_MINECRAFT
        
        // Reload data
        loadData()
    }
    
    /**
     * Perform search
     */
    private fun performSearch() {
        val query = binding.editTextSearch.text.toString()
        currentFilter = currentFilter.copy(searchQuery = query)
        loadData()
    }
    
    /**
     * Load data
     */
    private fun loadData() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                binding.swipeRefreshLayout.isRefreshing = true
                binding.progressBar.visibility = View.VISIBLE
                
                // Load installed mods
                installedMods = modManager.getInstalledMods()
                
                // Load available mods
                val mods = modManager.getAvailableMods(currentSource, currentFilter)
                
                // Update UI
                updateMods(mods)
                
            } catch (e: Exception) {
                Logging.e(TAG, "Failed to load mods", e)
                showToast(getString(com.orbitx.launcher.R.string.error))
            } finally {
                binding.swipeRefreshLayout.isRefreshing = false
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    /**
     * Update mods in the UI
     */
    private fun updateMods(mods: List<Mod>) {
        val modItems = mods.map { mod ->
            ModItem(
                mod = mod,
                isInstalled = installedMods.any { it.id == mod.id }
            )
        }
        
        modAdapter.submitList(modItems)
        
        // Update empty state
        if (modItems.isEmpty()) {
            binding.textViewEmptyState.visibility = View.VISIBLE
            binding.recyclerViewMods.visibility = View.GONE
        } else {
            binding.textViewEmptyState.visibility = View.GONE
            binding.recyclerViewMods.visibility = View.VISIBLE
        }
    }
    
    /**
     * Handle mod click
     */
    private fun onModClicked(mod: Mod) {
        // Show mod details
        showModDetails(mod)
    }
    
    /**
     * Handle install click
     */
    private fun onInstallClicked(mod: Mod) {
        modManager.installMod(mod) { success, message ->
            if (success) {
                showToast(message ?: getString(com.orbitx.launcher.R.string.success_installed))
                loadData()
            } else {
                showToast(message ?: getString(com.orbitx.launcher.R.string.failed))
            }
        }
    }
    
    /**
     * Handle remove click
     */
    private fun onRemoveClicked(mod: Mod) {
        modManager.uninstallMod(mod) { success, message ->
            if (success) {
                showToast(message ?: getString(com.orbitx.launcher.R.string.success_removed))
                loadData()
            } else {
                showToast(message ?: getString(com.orbitx.launcher.R.string.failed))
            }
        }
    }
    
    /**
     * Show mod details
     */
    private fun showModDetails(mod: Mod) {
        // Implementation would show a dialog or navigate to details screen
        showToast("Details for ${mod.name}")
    }
    
    /**
     * Show filter dialog
     */
    private fun showFilterDialog() {
        // Implementation would show a filter dialog
        showToast("Filter mods")
    }
    
    /**
     * Show sort dialog
     */
    private fun showSortDialog() {
        // Implementation would show a sort dialog
        showToast("Sort mods")
    }
    
    /**
     * Show add mod dialog
     */
    private fun showAddModDialog() {
        // Implementation would show a dialog to add a mod from URL or file
        showToast("Add mod")
    }
    
    override fun onResume() {
        super.onResume()
        Logging.d(TAG, "ModBrowserFragment resumed")
        loadData()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        Logging.d(TAG, "ModBrowserFragment view destroyed")
    }
    
    /**
     * Data class for mod items in the list
     */
    data class ModItem(
        val mod: Mod,
        val isInstalled: Boolean
    )
    
    /**
     * Adapter for mod list
     */
    inner class ModAdapter(
        private val onModClick: (Mod) -> Unit,
        private val onInstallClick: (Mod) -> Unit,
        private val onRemoveClick: (Mod) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<ModViewHolder>() {
        
        private val items = mutableListOf<ModItem>()
        
        fun submitList(newItems: List<ModItem>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModViewHolder {
            val binding = com.orbitx.launcher.databinding.ItemModBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ModViewHolder(binding)
        }
        
        override fun onBindViewHolder(holder: ModViewHolder, position: Int) {
            val item = items[position]
            holder.bind(item, onModClick, onInstallClick, onRemoveClick)
        }
        
        override fun getItemCount(): Int = items.size
    }
    
    /**
     * ViewHolder for mod items
     */
    inner class ModViewHolder(private val binding: com.orbitx.launcher.databinding.ItemModBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        
        fun bind(
            item: ModItem,
            onModClick: (Mod) -> Unit,
            onInstallClick: (Mod) -> Unit,
            onRemoveClick: (Mod) -> Unit
        ) {
            val mod = item.mod
            
            binding.textViewModName.text = mod.getDisplayName()
            binding.textViewModDescription.text = mod.description
            binding.textViewModAuthor.text = getString(com.orbitx.launcher.R.string.author) + ": " + mod.author
            binding.textViewModDownloads.text = mod.downloads.toString() + " " + getString(com.orbitx.launcher.R.string.downloads)
            binding.textViewModSize.text = mod.getFormattedSize()
            binding.textViewModVersion.text = getString(com.orbitx.launcher.R.string.version) + ": " + mod.version
            
            if (item.isInstalled) {
                binding.buttonInstall.text = getString(com.orbitx.launcher.R.string.remove)
                binding.buttonInstall.setOnClickListener { onRemoveClick(mod) }
            } else {
                binding.buttonInstall.text = getString(com.orbitx.launcher.R.string.install)
                binding.buttonInstall.setOnClickListener { onInstallClick(mod) }
            }
            
            binding.root.setOnClickListener { onModClick(mod) }
        }
    }
}

/**
 * ModBrowserViewModel - ViewModel for ModBrowserFragment
 */
class ModBrowserViewModel : androidx.lifecycle.ViewModel() {
    // ViewModel for mod browser data
    // Can be extended as needed
}
