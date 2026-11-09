package com.paintedmap.ui.screens.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.paintedmap.data.IUserRepository
import com.paintedmap.data.RussiaMap
import com.paintedmap.ui.components.AddRegionBar
import com.paintedmap.ui.components.AddRegionDialog
import com.paintedmap.ui.components.GlassPanel
import com.paintedmap.ui.components.StatisticsContent
import com.paintedmap.ui.theme.Dimens

@Composable
fun MapScreenContainer(
    innerPadding: PaddingValues,
    userRepository: IUserRepository
) {
    val viewModel: MapViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                MapViewModel(userRepository)
            }
        }
    )

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MapScreen(
        innerPadding = innerPadding,
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun MapScreen(
    innerPadding: PaddingValues,
    state: MapUiState,
    onAction: (MapAction) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        RussiaMap(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.45f).padding(horizontal = Dimens.sm),
            visitedRegions = state.userRegionsSet,
            circuitedRegion = state.selectedRegionForCircuit
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding() + Dimens.sm)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            GlassPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.sm)
                    .height(
                        if (state.showRegions) Dimens.mapGlassHeight * 1.5f
                        else Dimens.mapGlassHeight
                    ),
                shape = MaterialTheme.shapes.medium,
            ) {
                if (!state.showRegions) {
                    StatisticsContent(now = state.userRegionsCount, total = state.allRegionsCount, onAction = onAction)
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .clickable { onAction(MapAction.AddRegionButtonClicked) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Arrow",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            AddRegionBar(
                                searchQuery = state.search,
                                onSearchChange = { text ->
                                    onAction(MapAction.SearchRegionChanged(text))
                                },
                                state = state,
                                onAction = onAction
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.sm))

            if (state.showAddRegionDialog && state.selectedRegionForAdd != null) {
                AddRegionDialog(
                    region = state.selectedRegionForAdd,
                    onDismiss = { onAction(MapAction.DismissDialog) },
                    onConfirm = { onAction(MapAction.ConfirmDialog(state.selectedRegionForAdd)) }
                )
            }
        }
    }
}
