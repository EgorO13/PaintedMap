package com.paintedmap.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.paintedmap.ui.theme.Dimens
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeChild

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    //hazeState: HazeState? = null,
    shape: Shape = RoundedCornerShape(Dimens.cornerMd),
    alpha: Float = 0.85f,
    color: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = color.copy(alpha = alpha),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        content = content
    )
}