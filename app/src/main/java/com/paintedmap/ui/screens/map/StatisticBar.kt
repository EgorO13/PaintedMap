package com.paintedmap.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.paintedmap.ui.screens.map.MapAction
import com.paintedmap.ui.theme.Dimens
import kotlin.math.roundToInt

@Composable
fun StatisticsContent(
    now: Int,
    total: Int,
    modifier: Modifier = Modifier,
    onAction: (MapAction) -> Unit
) {
    val percent = if (total > 0) (now.toFloat() / total * 100f).roundToInt() else 0
    val progress = if (total > 0) now.toFloat() / total else 0f

    val underBarColor = MaterialTheme.colorScheme.outline
    val arcColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimens.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row (modifier = Modifier.padding(bottom = Dimens.sm)) {
            Text(
                text = "Статистика",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = Dimens.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(5f)
            )

            Box (
                modifier = Modifier.weight(1f),
            ) {
                ActionButton(
                    onClick = { onAction(MapAction.AddRegionButtonClicked) },
                    icon = Icons.Default.Add,
                    modifier = Modifier
                        .padding(horizontal = Dimens.xs)
                        .aspectRatio(1f),
                    contentDescription = "Statistic/Regions button",
                    containerColor = MaterialTheme.colorScheme.primary,
                    iconColor = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.weight(Dimens.statSpacerWeight))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.md)
                .weight(Dimens.statContentWeight),
            contentAlignment = Alignment.Center
        ) {
            val widthPx = constraints.maxWidth.toFloat()
            val indicatorWidthPx = with(LocalDensity.current) { Dimens.statIndicatorWidth.toPx() }

            val indicatorStartPadding = with(LocalDensity.current) {
                ((widthPx * progress) - (indicatorWidthPx / 2f))
                    .coerceIn(0f, widthPx - indicatorWidthPx)
                    .toDp()
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier.padding(start = indicatorStartPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        BasicText(
                            text = "$now",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            ),
                            autoSize = TextAutoSize.StepBased(
                                minFontSize = 10.sp,
                                maxFontSize = Dimens.bodyLarge
                            ),
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.xs))

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.statTrackThickness)
                ) {
                    val stroke = size.height
                    val y = size.height / 2f
                    val startX = 0f
                    val endX = size.width
                    val progressX = startX + (endX - startX) * progress

                    drawLine(
                        color = underBarColor,
                        start = Offset(startX, y),
                        end = Offset(endX, y),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    )

                    if (progress > 0f) {
                        drawLine(
                            color = arcColor,
                            start = Offset(startX, y),
                            end = Offset(progressX, y),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.xs))

                Row(modifier = Modifier.fillMaxWidth()) {
                    BasicText(
                        text = "0",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = Dimens.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = Dimens.statLabelAlpha)
                        ),
                        autoSize = TextAutoSize.StepBased(
                            minFontSize = 4.sp,
                            maxFontSize = Dimens.bodyMedium
                        ),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    BasicText(
                        text = "$total",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = Dimens.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = Dimens.statLabelAlpha)
                        ),
                        autoSize = TextAutoSize.StepBased(
                            minFontSize = 4.sp,
                            maxFontSize = Dimens.bodyMedium
                        ),
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(Dimens.statSpacerWeight))

        BasicText(
            text = "Вы посетили $percent% регионов России",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            ),
            autoSize = TextAutoSize.StepBased(
                minFontSize = 10.sp,
                maxFontSize = Dimens.bodyLarge
            ),
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimens.sm)
        )
    }
}
