package com.paintedmap.ui.screens.profile

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.paintedmap.data.IUserRepository
import com.paintedmap.ui.components.ActionButton
import com.paintedmap.ui.components.GlassPanel
import com.paintedmap.ui.components.SearchBar
import com.paintedmap.ui.theme.Dimens

@Composable
fun ProfileScreenContainer(
    userId: Int,
    innerPadding: PaddingValues,
    userRepository: IUserRepository
) {
    val viewModel: ProfileViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                ProfileViewModel(userId, userRepository)
            }
        }
    )

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileScreen(
        innerPadding = innerPadding,
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun ProfileScreen(
    innerPadding: PaddingValues,
    state: ProfileUiState,
    onAction: (ProfileAction) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.md,
            end = Dimens.md,
            top = Dimens.md,
            bottom = innerPadding.calculateBottomPadding() + Dimens.md
        ),
        horizontalArrangement = Arrangement.spacedBy(Dimens.md),
        verticalArrangement = Arrangement.spacedBy(Dimens.md)
    ) {
        item(
            span = { GridItemSpan(maxLineSpan) }
        ) {
            GlassPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.profileBarHeight),
                color = MaterialTheme.colorScheme.primary
            ) {
                ProfileBar(state)
            }
        }

        stickyHeader {
            GlassPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.searchBarHeight),
                color = MaterialTheme.colorScheme.secondary,
                alpha = 1f
            ) {
                SearchBar(
                    search = state.search,
                    onSearchChange = { text ->
                        onAction(ProfileAction.SearchAuthorChanged(text))
                    }
                )
            }
        }

        itemsIndexed(state.userAuthorsList) { _, subscription ->
            GlassPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                color = lerp(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.background, 0.5f),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = subscription.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileBar(state: ProfileUiState) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.md),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.userName,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Text(
                    text = "@${state.userNickName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    maxLines = 1,
                )
            }

            ActionButton(
                modifier = Modifier.size(44.dp),
                onClick = {
                    val link = "paintedmap://user/${state.userId}"
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, link)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Поделиться профилем"))
                },
                icon = Icons.Rounded.Share,
                contentDescription = "Поделиться профилем",
                containerColor = Color.Transparent,
                iconColor = MaterialTheme.colorScheme.onPrimary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.userRegionsAmount.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "регионы",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Normal
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.userSubAmount.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "подписчики",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Normal
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.userAuthorsAmount.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "подписки",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}
