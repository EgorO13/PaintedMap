package com.paintedmap.ui.screens.map

import com.paintedmap.data.RegionEntry
import com.paintedmap.data.RussiaRegions

sealed interface RegionsListState {
    data class Content(val list: List<RegionEntry>) : RegionsListState
    data object EmptySearch : RegionsListState
}

data class MapUiState(
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
    data class SearchRegionChanged(val search: String) : MapAction
    data class AddRegionClicked(val region: RegionEntry) : MapAction
    data class CircuitRegionClicked(val region: RegionEntry) : MapAction
    data object DismissDialog : MapAction
    data class ConfirmDialog(val region: RegionEntry) : MapAction
}