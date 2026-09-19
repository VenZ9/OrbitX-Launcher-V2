package com.orbitx.launcher.ui.browser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.orbitx.launcher.core.OrbitXApplication
import com.orbitx.launcher.databinding.FragmentBrowserBinding
import com.orbitx.launcher.manager.ResourcePackManager
import com.orbitx.launcher.model.ResourcePack
import com.orbitx.launcher.ui.BaseFragment
import com.orbitx.launcher.utils.Logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ResourcePackBrowserFragment - Fragment for browsing and managing resource packs
 * Similar to Zalith Launcher 2's resource pack browser
 */
class ResourcePackBrowserFragment : BaseFragment() {
    
    companion object {
        private const val TAG = "ResourcePackBrowserFragment"
        private const val SOURCE_ALL = "all"
        private const val SOURCE_MODRINTH = "modrinth"
        private const val SOURCE_CURSEFORGE = "curseforge"
        private const val SOURCE_PLANET_MINECRAFT = "planetminecraft"
        private const val SOURCE_RESOURCE_PACKS = "resourcepacks"
        
        fun newInstance(): ResourcePackBrowserFragment {
            return ResourcePackBrowserFragment()
        }
    }
    
    private lateinit var binding: FragmentBrowserBinding
    private lateinit var viewModel: ResourcePackBrowserViewModel
    private lateinit var resourcePackManager: ResourcePackManager
    private lateinit var resourcePackAdapter: ResourcePackAdapter
    
    private var currentSource = SOURCE_ALL
    private var currentFilter = ResourcePackManager.ResourcePackFilter()
    private var installedPacks = emptyList<ResourcePack>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logging.d(TAG, "ResourcePackBrowserFragment created")
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
        viewModel = ViewModelProvider(this)[ResourcePackBrowserViewModel::class.java]
        
        // Initialize managers
        resourcePackManager = ResourcePackManager.getInstance(requireContext())
        
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
        binding.toolbar.title = getString(com.orbitx.launcher.R.string.resource_packs)
    }
    
    /**
     * Setup RecyclerView for resource packs
     */
    private fun setupRecyclerView() {
        resourcePackAdapter = ResourcePackAdapter(
            onPackClick = { pack ->
                onPackClicked(pack)
            },
            onInstallClick = { pack ->
                onInstallClicked(pack)
            },
            onRemoveClick = { pack ->
                onRemoveClicked(pack)
            }
        )
        
        binding.recyclerViewMods.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = resourcePackAdapter
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
        
        // Update tab labels
        binding.chipModrinth.text = "Modrinth"
        binding.chipCurseforge.text = "CurseForge"
        binding.chipPlanetMinecraft.text = "PlanetMC"
        
        // Select default tab
        selectSource(SOURCE_ALL)
    }
    
    /**
     * Setup click listeners
     */
    private fun setupClickListeners() {
        binding.buttonFilter.setOnClickListener { showFilterDialog() }
        binding.buttonSort.setOnClickListener { showSortDialog() }
        binding.fabAddMod.setOnClickListener { showAddPackDialog() }
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
                
                // Load installed packs
                installedPacks = resourcePackManager.getInstalledResourcePacks()
                
                // Load available packs
                val packs = resourcePackManager.getAvailableResourcePacks(currentSource, currentFilter)
                
                // Update UI
                updatePacks(packs)
                
            } catch (e: Exception) {
                Logging.e(TAG, "Failed to load resource packs", e)
                showToast(getString(com.orbitx.launcher.R.string.error))
            } finally {
                binding.swipeRefreshLayout.isRefreshing = false
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    /**
     * Update resource packs in the UI
     */
    private fun updatePacks(packs: List<ResourcePack>) {
        val packItems = packs.map { pack ->
            ResourcePackItem(
                pack = pack,
                isInstalled = installedPacks.any { it.id == pack.id }
            )
        }
        
        resourcePackAdapter.submitList(packItems)
        
        // Update empty state
        if (packItems.isEmpty()) {
            binding.textViewEmptyState.visibility = View.VISIBLE
            binding.recyclerViewMods.visibility = View.GONE
        } else {
            binding.textViewEmptyState.visibility = View.GONE
            binding.recyclerViewMods.visibility = View.VISIBLE
        }
    }
    
    /**
     * Handle resource pack click
     */
    private fun onPackClicked(pack: ResourcePack) {
        showPackDetails(pack)
    }
    
    /**
     * Handle install click
     */
    private fun onInstallClicked(pack: ResourcePack) {
        resourcePackManager.installResourcePack(pack) { success, message ->
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
    private fun onRemoveClicked(pack: ResourcePack) {
        resourcePackManager.uninstallResourcePack(pack) { success, message ->
            if (success) {
                showToast(message ?: getString(com.orbitx.launcher.R.string.success_removed))
                loadData()
            } else {
                showToast(message ?: getString(com.orbitx.launcher.R.string.failed))
            }
        }
    }
    
    /**
     * Show resource pack details
     */
    private fun showPackDetails(pack: ResourcePack) {
        showToast("Details for ${pack.name}")
    }
    
    /**
     * Show filter dialog
     */
    private fun showFilterDialog() {
        showToast("Filter resource packs")
    }
    
    /**
     * Show sort dialog
     */
    private fun showSortDialog() {
        showToast("Sort resource packs")
    }
    
    /**
     * Show add resource pack dialog
     */
    private fun showAddPackDialog() {
        showToast("Add resource pack")
    }
    
    override fun onResume() {
        super.onResume()
        Logging.d(TAG, "ResourcePackBrowserFragment resumed")
        loadData()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        Logging.d(TAG, "ResourcePackBrowserFragment view destroyed")
    }
    
    /**
     * Data class for resource pack items in the list
     */
    data class ResourcePackItem(
        val pack: ResourcePack,
        val isInstalled: Boolean
    )
    
    /**
     * Adapter for resource pack list
     */
    inner class ResourcePackAdapter(
        private val onPackClick: (ResourcePack) -> Unit,
        private val onInstallClick: (ResourcePack) -> Unit,
        private val onRemoveClick: (ResourcePack) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<ResourcePackViewHolder>() {
        
        private val items = mutableListOf<ResourcePackItem>()
        
        fun submitList(newItems: List<ResourcePackItem>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResourcePackViewHolder {
            val binding = com.orbitx.launcher.databinding.ItemResourcePackBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ResourcePackViewHolder(binding)
        }
        
        override fun onBindViewHolder(holder: ResourcePackViewHolder, position: Int) {
            val item = items[position]
            holder.bind(item, onPackClick, onInstallClick, onRemoveClick)
        }
        
        override fun getItemCount(): Int = items.size
    }
    
    /**
     * ViewHolder for resource pack items
     */
    inner class ResourcePackViewHolder(private val binding: com.orbitx.launcher.databinding.ItemResourcePackBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        
        fun bind(
            item: ResourcePackItem,
            onPackClick: (ResourcePack) -> Unit,
            onInstallClick: (ResourcePack) -> Unit,
            onRemoveClick: (ResourcePack) -> Unit
        ) {
            val pack = item.pack
            
            binding.textViewResourcePackName.text = pack.getDisplayName()
            binding.textViewResourcePackDescription.text = pack.description
            binding.textViewResourcePackAuthor.text = getString(com.orbitx.launcher.R.string.author) + ": " + pack.author
            binding.textViewResourcePackDownloads.text = pack.downloads.toString() + " " + getString(com.orbitx.launcher.R.string.downloads)
            binding.textViewResourcePackSize.text = pack.getFormattedSize()
            binding.textViewResourcePackVersion.text = getString(com.orbitx.launcher.R.string.version) + ": " + pack.version
            binding.textViewResourcePackResolution.text = "Resolution: " + pack.resolution
            binding.textViewResourcePackRating.text = "Rating: " + pack.rating
            
            if (item.isInstalled) {
                binding.buttonInstall.text = getString(com.orbitx.launcher.R.string.remove)
                binding.buttonInstall.setOnClickListener { onRemoveClick(pack) }
            } else {
                binding.buttonInstall.text = getString(com.orbitx.launcher.R.string.install)
                binding.buttonInstall.setOnClickListener { onInstallClick(pack) }
            }
            
            binding.root.setOnClickListener { onPackClick(pack) }
        }
    }
}

/**
 * ResourcePackBrowserViewModel - ViewModel for ResourcePackBrowserFragment
 */
class ResourcePackBrowserViewModel : androidx.lifecycle.ViewModel() {
    // ViewModel for resource pack browser data
}
