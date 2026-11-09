package com.paintedmap.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paintedmap.data.IUserRepository
import com.paintedmap.data.RegionEntry
import com.paintedmap.data.RussiaRegions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface RegionsListState {
    data class Content(val list: List<RegionEntry>) : RegionsListState
    object EmptySearch : RegionsListState
}

data class MapUiState (
    val showRegions: Boolean = false,
    val showAddRegionDialog: Boolean = false,
    val selectedRegionForAdd: RegionEntry? = null,
    val selectedRegionForCircuit: RegionEntry? = null,
    val search: String = "",
    val regionsState: RegionsListState = RegionsListState.Content(RussiaRegions),
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

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModel (
    private val userRepository: IUserRepository
) : ViewModel() {
    private val showRegions = MutableStateFlow<Boolean>(false)
    private val showRegionCircuit = MutableStateFlow<RegionEntry?>(null)
    private val addRegion = MutableStateFlow<RegionEntry?>(null)
    private val showDialog = MutableStateFlow<Boolean>(false)
    private val searchQuery = MutableStateFlow<String>("")
    private var circuitJob: Job? = null // подсветка контура региона

    val uiState : StateFlow<MapUiState> = combine(
        showRegions, showRegionCircuit, searchQuery, showDialog, addRegion
    ) { showRegs, circuitReg, query, dialog, selRegion ->
        listOf(showRegs, circuitReg, query, dialog, selRegion)
    }
        .distinctUntilChanged()
        .flatMapLatest { params ->
            val showRegs = params[0] as Boolean
            val circuitReg = params[1] as RegionEntry?
            val query = params[2] as String
            val dialog = params[3] as Boolean
            val selRegion = params[4] as RegionEntry?

            flow {
                val parallelResult = coroutineScope {
                    val filteredDeferred = async {
                        RussiaRegions.filter {
                            it.name.contains(query, ignoreCase = true) || it.id.contains(query)
                        }
                    }

                    val userDeferred = async {
                        try {
                            userRepository.findById(1)
                        } catch (e: Exception) {
                            if (e is CancellationException) throw e
                            null
                        }
                    }

                    Pair(filteredDeferred.await(), userDeferred.await())
                }

                val filtered = parallelResult.first
                val user = parallelResult.second

                val finalRegionsState = if (filtered.isEmpty()) {
                    RegionsListState.EmptySearch
                } else {
                    RegionsListState.Content(filtered)
                }

                emit(
                    MapUiState(
                        showRegions = showRegs,
                        selectedRegionForCircuit = circuitReg,
                        search = query,
                        regionsState = finalRegionsState,
                        userRegionsCount = user?.amountOfRegions() ?: 0,
                        userRegionsSet = user?.getRegions()?.toSet() ?: emptySet(),
                        showAddRegionDialog = dialog,
                        selectedRegionForAdd = selRegion
                    )
                )
            }
        }
        .stateIn(
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
                    val user = userRepository.findById(1)
                    if (user != null) {
                        user.addRegion(action.region)
                    }
                    showDialog.value = false
                    addRegion.value = null

                    val currentSearch = searchQuery.value
                    searchQuery.value = ""
                    searchQuery.value = currentSearch
                }
            }
        }
    }
}
