package com.paintedmap.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.paintedmap.data.RegionEntry
import com.paintedmap.ui.screens.map.MapAction
import com.paintedmap.ui.screens.map.MapUiState
import com.paintedmap.ui.theme.Dimens


@Composable
fun AddRegionBar(
    searchQuery: String,
    regions: List<RegionEntry>,
    onSearchChange: (String) -> Unit,
    state: MapUiState,
    onAction: (MapAction) -> Unit
) {
    Column{
        GlassPanel(
            modifier = Modifier
                .padding(Dimens.sm)
                .fillMaxWidth()
                .height(Dimens.searchBarHeight),
            color = lerp(
                MaterialTheme.colorScheme.secondary,
                MaterialTheme.colorScheme.background,
                0.5f
            ),
            alpha = 1f
        ) {
            SearchBar(
                search = searchQuery,
                onSearchChange = onSearchChange
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.xs)
        ) {
            itemsIndexed(regions) { _, region ->
                GlassPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.regionsHeight)
                        .padding(horizontal = Dimens.sm)
                        .clickable { onAction(MapAction.CircuitRegionClicked(region)) },
                    shape = MaterialTheme.shapes.medium,
                    color = lerp(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                        0.5f
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = Dimens.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicText(
                            text = region.id,
                            modifier = Modifier.weight(0.2f),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            autoSize = TextAutoSize.StepBased(
                                minFontSize = 4.sp,
                                maxFontSize = 28.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        BasicText(
                            text = region.name,
                            modifier = Modifier.weight(0.7f),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            autoSize = TextAutoSize.StepBased(
                                minFontSize = 4.sp,
                                maxFontSize = 28.sp
                            ),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (state.userRegionsSet.contains(region)) {
                            Box(
                                modifier = Modifier.weight(0.1f),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = "Done",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        } else {
                            ActionButton(
                                onClick = { onAction(MapAction.AddRegionClicked(region)) },
                                modifier = Modifier
                                    .weight(0.1f)
                                    .aspectRatio(1f),
                                icon = Icons.Default.Add,
                                contentDescription = "Add region",
                                containerColor = Color.Transparent,
                                iconColor = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddRegionDialog(
    region: RegionEntry,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Добавление поездки: ${region.name}") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Подтвердить",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Отмена",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}
