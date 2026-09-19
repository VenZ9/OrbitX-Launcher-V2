package com.orbitx.launcher.ui.browser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.orbitx.launcher.core.OrbitXApplication
import com.orbitx.launcher.databinding.FragmentBrowserBinding
import com.orbitx.launcher.manager.ShaderManager
import com.orbitx.launcher.model.ShaderPack
import com.orbitx.launcher.ui.BaseFragment
import com.orbitx.launcher.utils.Logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ShaderBrowserFragment - Fragment for browsing and managing shader packs
 * Similar to Zalith Launcher 2's shader pack browser
 */
class ShaderBrowserFragment : BaseFragment() {
    
    companion object {
        private const val TAG = "ShaderBrowserFragment"
        private const val SOURCE_ALL = "all"
        private const val SOURCE_MODRINTH = "modrinth"
        private const val SOURCE_CURSEFORGE = "curseforge"
        private const val SOURCE_SHADERPACKS = "shaderpacks"
        private const val SOURCE_SHADERSMODS = "shadersmods"
        
        fun newInstance(): ShaderBrowserFragment {
            return ShaderBrowserFragment()
        }
    }
    
    private lateinit var binding: FragmentBrowserBinding
    private lateinit var viewModel: ShaderBrowserViewModel
    private lateinit var shaderManager: ShaderManager
    private lateinit var shaderAdapter: ShaderAdapter
    
    private var currentSource = SOURCE_ALL
    private var currentFilter = ShaderManager.ShaderFilter()
    private var installedShaders = emptyList<ShaderPack>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logging.d(TAG, "ShaderBrowserFragment created")
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
        viewModel = ViewModelProvider(this)[ShaderBrowserViewModel::class.java]
        
        // Initialize managers
        shaderManager = ShaderManager.getInstance(requireContext())
        
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
        binding.toolbar.title = getString(com.orbitx.launcher.R.string.shaders)
    }
    
    /**
     * Setup RecyclerView for shader packs
     */
    private fun setupRecyclerView() {
        shaderAdapter = ShaderAdapter(
            onShaderClick = { shader ->
                onShaderClicked(shader)
            },
            onInstallClick = { shader ->
                onInstallClicked(shader)
            },
            onRemoveClick = { shader ->
                onRemoveClicked(shader)
            }
        )
        
        binding.recyclerViewMods.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = shaderAdapter
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
        binding.chipPlanetMinecraft.setOnClickListener { selectSource(SOURCE_SHADERPACKS) }
        
        // Update tab labels
        binding.chipModrinth.text = "Modrinth"
        binding.chipCurseforge.text = "CurseForge"
        binding.chipPlanetMinecraft.text = "ShaderPacks"
        
        // Select default tab
        selectSource(SOURCE_ALL)
    }
    
    /**
     * Setup click listeners
     */
    private fun setupClickListeners() {
        binding.buttonFilter.setOnClickListener { showFilterDialog() }
        binding.buttonSort.setOnClickListener { showSortDialog() }
        binding.fabAddMod.setOnClickListener { showAddShaderDialog() }
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
        binding.chipPlanetMinecraft.isChecked = source == SOURCE_SHADERPACKS
        
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
                
                // Load installed shaders
                installedShaders = shaderManager.getInstalledShaderPacks()
                
                // Load available shaders
                val shaders = shaderManager.getAvailableShaderPacks(currentSource, currentFilter)
                
                // Update UI
                updateShaders(shaders)
                
            } catch (e: Exception) {
                Logging.e(TAG, "Failed to load shader packs", e)
                showToast(getString(com.orbitx.launcher.R.string.error))
            } finally {
                binding.swipeRefreshLayout.isRefreshing = false
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    /**
     * Update shader packs in the UI
     */
    private fun updateShaders(shaders: List<ShaderPack>) {
        val shaderItems = shaders.map { shader ->
            ShaderItem(
                shader = shader,
                isInstalled = installedShaders.any { it.id == shader.id }
            )
        }
        
        shaderAdapter.submitList(shaderItems)
        
        // Update empty state
        if (shaderItems.isEmpty()) {
            binding.textViewEmptyState.visibility = View.VISIBLE
            binding.recyclerViewMods.visibility = View.GONE
        } else {
            binding.textViewEmptyState.visibility = View.GONE
            binding.recyclerViewMods.visibility = View.VISIBLE
        }
    }
    
    /**
     * Handle shader click
     */
    private fun onShaderClicked(shader: ShaderPack) {
        showShaderDetails(shader)
    }
    
    /**
     * Handle install click
     */
    private fun onInstallClicked(shader: ShaderPack) {
        shaderManager.installShaderPack(shader) { success, message ->
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
    private fun onRemoveClicked(shader: ShaderPack) {
        shaderManager.uninstallShaderPack(shader) { success, message ->
            if (success) {
                showToast(message ?: getString(com.orbitx.launcher.R.string.success_removed))
                loadData()
            } else {
                showToast(message ?: getString(com.orbitx.launcher.R.string.failed))
            }
        }
    }
    
    /**
     * Show shader details
     */
    private fun showShaderDetails(shader: ShaderPack) {
        showToast("Details for ${shader.name}")
    }
    
    /**
     * Show filter dialog
     */
    private fun showFilterDialog() {
        showToast("Filter shaders")
    }
    
    /**
     * Show sort dialog
     */
    private fun showSortDialog() {
        showToast("Sort shaders")
    }
    
    /**
     * Show add shader dialog
     */
    private fun showAddShaderDialog() {
        showToast("Add shader")
    }
    
    override fun onResume() {
        super.onResume()
        Logging.d(TAG, "ShaderBrowserFragment resumed")
        loadData()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        Logging.d(TAG, "ShaderBrowserFragment view destroyed")
    }
    
    /**
     * Data class for shader items in the list
     */
    data class ShaderItem(
        val shader: ShaderPack,
        val isInstalled: Boolean
    )
    
    /**
     * Adapter for shader list
     */
    inner class ShaderAdapter(
        private val onShaderClick: (ShaderPack) -> Unit,
        private val onInstallClick: (ShaderPack) -> Unit,
        private val onRemoveClick: (ShaderPack) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<ShaderViewHolder>() {
        
        private val items = mutableListOf<ShaderItem>()
        
        fun submitList(newItems: List<ShaderItem>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShaderViewHolder {
            val binding = com.orbitx.launcher.databinding.ItemShaderBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ShaderViewHolder(binding)
        }
        
        override fun onBindViewHolder(holder: ShaderViewHolder, position: Int) {
            val item = items[position]
            holder.bind(item, onShaderClick, onInstallClick, onRemoveClick)
        }
        
        override fun getItemCount(): Int = items.size
    }
    
    /**
     * ViewHolder for shader items
     */
    inner class ShaderViewHolder(private val binding: com.orbitx.launcher.databinding.ItemShaderBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        
        fun bind(
            item: ShaderItem,
            onShaderClick: (ShaderPack) -> Unit,
            onInstallClick: (ShaderPack) -> Unit,
            onRemoveClick: (ShaderPack) -> Unit
        ) {
            val shader = item.shader
            
            binding.textViewShaderName.text = shader.getDisplayName()
            binding.textViewShaderDescription.text = shader.description
            binding.textViewShaderAuthor.text = getString(com.orbitx.launcher.R.string.author) + ": " + shader.author
            binding.textViewShaderDownloads.text = shader.downloads.toString() + " " + getString(com.orbitx.launcher.R.string.downloads)
            binding.textViewShaderSize.text = shader.getFormattedSize()
            binding.textViewShaderVersion.text = getString(com.orbitx.launcher.R.string.version) + ": " + shader.version
            binding.textViewShaderCompatibility.text = "Compatible: " + shader.compatibility.joinToString(", ")
            binding.textViewShaderRating.text = "Rating: " + shader.rating
            
            if (item.isInstalled) {
                binding.buttonInstall.text = getString(com.orbitx.launcher.R.string.remove)
                binding.buttonInstall.setOnClickListener { onRemoveClick(shader) }
            } else {
                binding.buttonInstall.text = getString(com.orbitx.launcher.R.string.install)
                binding.buttonInstall.setOnClickListener { onInstallClick(shader) }
            }
            
            binding.root.setOnClickListener { onShaderClick(shader) }
        }
    }
}

/**
 * ShaderBrowserViewModel - ViewModel for ShaderBrowserFragment
 */
class ShaderBrowserViewModel : androidx.lifecycle.ViewModel() {
    // ViewModel for shader browser data
}
