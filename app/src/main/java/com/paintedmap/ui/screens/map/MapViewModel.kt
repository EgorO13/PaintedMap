package com.paintedmap.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paintedmap.data.IUserRepository
import com.paintedmap.data.RegionEntry
import com.paintedmap.data.RussiaRegions
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MapUiState (
    val showRegions: Boolean = false,
    val showAddRegionDialog: Boolean = false,
    val selectedRegionForAdd: RegionEntry? = null,
    val selectedRegionForCircuit: RegionEntry? = null,
    val search: String = "",
    val filteredRegions: List<RegionEntry> = emptyList(),
    val userRegionsSet: Set<RegionEntry> = emptySet(),
    val userRegionsCount: Int = 0,
    val allRegionsCount: Int = RussiaRegions.size,
)

sealed interface MapAction {
    data object AddRegionButtonClicked : MapAction
    data class SearchRegionChanged (val search : String) : MapAction
    data class AddRegionClicked (val region : RegionEntry) : MapAction
    data class CircuitRegionClicked (val region : RegionEntry) : MapAction

    data object DismissDialog : MapAction
    data class ConfirmDialog (val region : RegionEntry) : MapAction
}

class MapViewModel (
    private val userRepository: IUserRepository
) : ViewModel() {
    private val showRegions = MutableStateFlow<Boolean>(false)

    private val showRegionCircuit = MutableStateFlow<RegionEntry?>(null)
    private val addRegion = MutableStateFlow<RegionEntry?>(null)
    private val showDialog = MutableStateFlow<Boolean>(false)
    private val searchQuery = MutableStateFlow<String>("")

    val uiState : StateFlow<MapUiState> = combine (
        showRegions, showRegionCircuit, searchQuery, showDialog, addRegion
    ) { showRegions, showRegionCircuit, query, showDialog, selectedRegion ->
        val user = userRepository.findById(1)

        val filtered = RussiaRegions.filter {
            it.name.contains(query, ignoreCase = true) || it.id.contains(query)
        }
        MapUiState(
            showRegions = showRegions,
            selectedRegionForCircuit = showRegionCircuit,
            search = query,
            filteredRegions = filtered,
            userRegionsCount = user?.amountOfRegions() ?: 0,
            userRegionsSet = user?.getRegions()?.toSet() ?: emptySet(), // что бы карта перерисовалась
            showAddRegionDialog = showDialog,
            selectedRegionForAdd = selectedRegion
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MapUiState()
    )

    fun onAction(action : MapAction) {
        when (action) {
            MapAction.AddRegionButtonClicked -> {
                showRegions.update { !it }
            }
            is MapAction.SearchRegionChanged -> {
                searchQuery.value = action.search
            }
            is MapAction.CircuitRegionClicked -> {
                // корутина внутри жизненного цикла ViewModel
                viewModelScope.launch {
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
                val user = userRepository.findById(1)
                if (user != null) user.addRegion(action.region)
                showDialog.value = false
                addRegion.value = null
            }
        }
    }
}
