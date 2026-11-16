package com.paintedmap.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paintedmap.data.RegionEntry
import com.paintedmap.data.RussiaRegions
import com.paintedmap.domain.model.LoadResult
import com.paintedmap.domain.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val showRegions = MutableStateFlow(false)
    private val showRegionCircuit = MutableStateFlow<RegionEntry?>(null)
    private val addRegion = MutableStateFlow<RegionEntry?>(null)
    private val showDialog = MutableStateFlow(false)
    private val searchQuery = MutableStateFlow("")
    private val visitedRegions = MutableStateFlow<Set<RegionEntry>>(emptySet())
    private var circuitJob: Job? = null

    init { loadVisitedRegions() }
    private fun loadVisitedRegions() {
        viewModelScope.launch {
            val result = userRepository.getUserRegions(userId = 1)
            if (result is LoadResult.Success) {
                val ids = result.value
                visitedRegions.value = RussiaRegions.filter { it.id in ids }.toSet()
            }
        }
    }

    val uiState: StateFlow<MapUiState> = combine(
        showRegions, showRegionCircuit, searchQuery, showDialog, addRegion, visitedRegions
    ) { params ->
        val showRegs = params[0] as Boolean
        val circuitReg = params[1] as RegionEntry?
        val query = params[2] as String
        val dialog = params[3] as Boolean
        val selRegion = params[4] as RegionEntry?
        val regions = params[5] as Set<RegionEntry>

        val filtered = RussiaRegions.filter {
            it.name.contains(query, ignoreCase = true) || it.id.contains(query)
        }
        val finalRegionsState = if (filtered.isEmpty()) {
            RegionsListState.EmptySearch
        } else {
            RegionsListState.Content(filtered)
        }

        MapUiState(
            showRegions = showRegs,
            selectedRegionForCircuit = circuitReg,
            search = query,
            regionsState = finalRegionsState,
            userRegionsCount = regions.size,
            userRegionsSet = regions,
            showAddRegionDialog = dialog,
            selectedRegionForAdd = selRegion
        )
    }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MapUiState()
        )

    fun onAction(action: MapAction) {
        when (action) {
            MapAction.AddRegionButtonClicked -> showRegions.update { !it }
            is MapAction.SearchRegionChanged -> searchQuery.value = action.search
            is MapAction.CircuitRegionClicked -> {
                circuitJob?.cancel()
                circuitJob = viewModelScope.launch {
                    showRegionCircuit.value = action.region
                    delay(3000L)
                    showRegionCircuit.value = null
                }
            }
            is MapAction.AddRegionClicked -> {
                addRegion.value = action.region
                showDialog.value = true
            }
            MapAction.DismissDialog -> {
                showDialog.value = false
                addRegion.value = null
            }
            is MapAction.ConfirmDialog -> {
                viewModelScope.launch {
                    val result = userRepository.addUserRegion(userId = 1, regionId = action.region.id)
                    if (result is LoadResult.Success) {
                        visitedRegions.update { it + action.region }
                    }
                    showDialog.value = false
                    addRegion.value = null
                }
            }
        }
    }
}