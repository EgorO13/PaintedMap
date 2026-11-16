package com.paintedmap.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.paintedmap.domain.model.userMessage
import com.paintedmap.domain.repository.UserRepository
import com.paintedmap.ui.components.GlassPanel
import com.paintedmap.ui.components.SearchBar
import com.paintedmap.ui.theme.Dimens


@Composable
fun ProfileScreenContainer(
    userId: Int,
    innerPadding: PaddingValues,
    userRepository: UserRepository,
    onAuthorNavigate: (Int) -> Unit
) {
    val viewModel: ProfileViewModel = viewModel(
        key = userId.toString(),
        factory = viewModelFactory {
            initializer {
                ProfileViewModel(userId, userRepository)
            }
        }
    )

    LaunchedEffect(userId) {
        viewModel.onAction(ProfileAction.Refresh)
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileScreen(
        innerPadding = innerPadding,
        state = state,
        onAction = { action ->
            if (action is ProfileAction.AuthorClicked) {
                onAuthorNavigate(action.author)
            } else {
                viewModel.onAction(action)
            }
        }
    )
}

@Composable
fun ProfileScreen(
    innerPadding: PaddingValues,
    state: ProfileUiState,
    onAction: (ProfileAction) -> Unit
) {
    val gridState = rememberLazyGridState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItems = gridState.layoutInfo.totalItemsCount
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisible >= totalItems - 2
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onAction(ProfileAction.LoadNextPage)
        }
    }

    LazyVerticalGrid(
        state = gridState,
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

        when (val authorsState = state.authorsState) {
            is AuthorsListState.Loading -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.md),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            is AuthorsListState.Empty -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = "Тут будут отображаться подписки",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.md)
                    )
                }
            }
            is AuthorsListState.EmptySearch -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = "Пользователь с таким именем не найден",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.md)
                    )
                }
            }
            is AuthorsListState.Error -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    NetworkErrorBlock(
                        error = authorsState.error,
                        message = authorsState.message,
                        isRetryBlocked = state.isRetryBlocked,
                        onRetry = { onAction(ProfileAction.RetryClicked) }
                    )
                }
            }
            is AuthorsListState.Content -> {
                itemsIndexed(
                    items = authorsState.list,
                    key = { _, item -> item.id }
                ) { _, subscription ->
                    GlassPanel(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clickable { onAction(ProfileAction.AuthorClicked(subscription.id)) },
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

                if (authorsState.isLoadingNextPage) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.sm),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp))
                        }
                    }
                }

                if (authorsState.paginationError != null) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        NetworkErrorBlock(
                            error = authorsState.paginationError,
                            message = authorsState.paginationError.userMessage(),
                            isRetryBlocked = state.isRetryBlocked,
                            onRetry = { onAction(ProfileAction.LoadNextPage) }
                        )
                    }
                }
            }
        }
    }
}