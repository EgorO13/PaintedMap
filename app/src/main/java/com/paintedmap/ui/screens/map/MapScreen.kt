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
                //color = MaterialTheme.colorScheme.surface
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
                                regions = state.filteredRegions,
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
//
//@Composable
//private fun RegionsContent(
//    showSearch: Boolean,
//    searchQuery: String,
//    regions: List<RegionEntry>,
//    onSearchChange: (String) -> Unit,
//    state: MapUiState,
//    onAction: (MapAction) -> Unit
//) {
//    Column {
//        if (showSearch) {
//            GlassPanel(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(Dimens.searchBarHeight)
//                    .padding(horizontal = Dimens.sm),
//                color = lerp(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.background, 0.5f),
//                alpha = 1f
//            ) {
//                SearchBar(
//                    search = searchQuery,
//                    onSearchChange = onSearchChange
//                )
//            }
//        }
//
//        LazyColumn(
//            modifier = Modifier.fillMaxHeight(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(Dimens.xs)
//        ) {
//            itemsIndexed(regions) { _, region ->
//                GlassPanel(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(Dimens.regionsHeight)
//                        .padding(horizontal = Dimens.sm),
//                    shape = MaterialTheme.shapes.medium,
//                    color = lerp(
//                        MaterialTheme.colorScheme.surface,
//                        MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
//                        0.5f
//                    )
//                ) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(horizontal = Dimens.sm),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        BasicText(
//                            text = region.id,
//                            modifier = Modifier.weight(0.2f),
//                            style = MaterialTheme.typography.bodyLarge.copy(
//                                color = MaterialTheme.colorScheme.onSurface
//                            ),
//                            autoSize = TextAutoSize.StepBased(
//                                minFontSize = 4.sp,
//                                maxFontSize = 28.sp
//                            ),
//                            maxLines = 1,
//                            overflow = TextOverflow.Ellipsis
//                        )
//                        BasicText(
//                            text = region.name,
//                            modifier = Modifier.weight(0.7f),
//                            style = MaterialTheme.typography.bodyLarge.copy(
//                                color = MaterialTheme.colorScheme.onSurface
//                            ),
//                            autoSize = TextAutoSize.StepBased(
//                                minFontSize = 4.sp,
//                                maxFontSize = 28.sp
//                            ),
//                            maxLines = 3,
//                            overflow = TextOverflow.Ellipsis
//                        )
//                        if (state.userRegionsSet.contains(region)) {
//                            Box(
//                                modifier = Modifier.weight(0.1f),
//                            ) {
//                                Icon(
//                                    imageVector = Icons.Default.Done,
//                                    contentDescription = "Done",
//                                    tint = MaterialTheme.colorScheme.onSurface
//                                )
//                            }
//                        } else {
//                            ActionButton(
//                                onClick = { onAction(MapAction.AddRegionClicked(region)) },
//                                modifier = Modifier.weight(0.1f).aspectRatio(1f),
//                                icon = Icons.Default.Add,
//                                contentDescription = "Add region",
//                                containerColor = Color.Transparent,
//                                iconColor = MaterialTheme.colorScheme.onSurface
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun AddRegionDialog(
//    region: RegionEntry,
//    onDismiss: () -> Unit,
//    onConfirm: () -> Unit
//) {
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text(text = "Добавление поездки: ${region.name}") },
//        confirmButton = {
//            TextButton(onClick = onConfirm) {
//                Text("Подтвердить")
//            }
//        },
//        dismissButton = {
//            TextButton(onClick = onDismiss) {
//                Text("Отмена")
//            }
//        }
//    )
//}
