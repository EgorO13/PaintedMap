package com.paintedmap.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paintedmap.data.RussiaMap
import com.paintedmap.data.RussiaRegions
import com.paintedmap.ui.components.ActionButton
import com.paintedmap.ui.components.GlassPanel
import com.paintedmap.ui.components.SearchBar
import com.paintedmap.ui.components.StatisticsContent
import com.paintedmap.ui.theme.Dimens

@Composable
fun MapScreen(
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
) {
    var showRegions by rememberSaveable { mutableStateOf(false) }
    var isSheetExpanded by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        RussiaMap(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.55f)
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
                    .animateContentSize()
                    .height(
                        if (showRegions && isSheetExpanded) Dimens.profileBarHeight * 5f
                        else Dimens.mapGlassHeight
                    ),
                //shape = if (panel == Panel.REGIONS) RoundedCornerShape(topStart = Dimens.cornerLg, topEnd = Dimens.cornerLg) else MaterialTheme.shapes.medium,
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface
            ) {
                if (!showRegions) {
                    StatisticsContent(now = 67, total = 89)
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                                .clickable { isSheetExpanded = !isSheetExpanded },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSheetExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                contentDescription = "Arrow",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            RegionsContent(isSheetExpanded)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.sm))

            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionButton(
                    onClick = {
                        showRegions = !showRegions
                        if (!showRegions) isSheetExpanded = false
                    },
                    icon = if (showRegions) Icons.Default.QueryStats else Icons.Default.List,
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimens.buttonHeight)
                        .padding(horizontal = Dimens.xs),
                    contentDescription = "Statistic/Regions button",
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    iconColor = MaterialTheme.colorScheme.onSurface
                )
                ActionButton(
                    onClick = {},
                    icon = Icons.Default.Add,
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimens.buttonHeight)
                        .padding(horizontal = Dimens.xs),
                    contentDescription = "Add region",
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    iconColor = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun RegionsContent(showSearch: Boolean) {
    var search by remember { mutableStateOf("") }
    val filteredRegions = remember(search) {
        RussiaRegions.filter {
            it.name.contains(search, ignoreCase = true) || it.id.contains(search)
        }
    }
    Column {
        if (showSearch) {
            GlassPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.searchBarHeight)
                    .padding(horizontal = Dimens.sm),
                color = lerp(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.background, 0.5f),
                alpha = 1f
            ) {
                SearchBar(
                    search = search,
                    onSearchChange = { search = it }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.xs)
        ) {
            itemsIndexed(filteredRegions) { _, region ->
                GlassPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.regionsHeight)
                        .padding(horizontal = Dimens.sm),
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
                            modifier = Modifier.weight(0.8f),
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
                    }
                }
            }
        }
    }
}

@Composable
fun addRegion() {

}
