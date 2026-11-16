package com.paintedmap.ui.screens.profile

import com.paintedmap.domain.model.NetworkError

sealed interface AuthorsListState {
    data object Loading : AuthorsListState
    data class Content(
        val list: List<ProfileAuthorUiState>,
        val nextCursor: String? = null,
        val isLoadingNextPage: Boolean = false,
        val paginationError: NetworkError? = null,
    ) : AuthorsListState
    data object Empty : AuthorsListState
    data object EmptySearch : AuthorsListState
    data class Error(
        val error: NetworkError,
        val message: String,
    ) : AuthorsListState
}

data class ProfileUiState(
    val userId: Int = 0,
    val shareProfile: Boolean = false,
    val userName: String = "",
    val userNickName: String = "",
    val search: String = "",
    val userSubAmount: Int = 0,
    val userAuthorsAmount: Int = 0,
    val userRegionsAmount: Int = 0,
    val authorsState: AuthorsListState = AuthorsListState.Loading,
    val isRetryBlocked: Boolean = false,
)

data class ProfileAuthorUiState(
    val id: Int,
    val name: String,
    val nickname: String,
)

sealed interface ProfileAction {
    data class SearchAuthorChanged(val search: String) : ProfileAction
    data class AuthorClicked(val author: Int) : ProfileAction
    data object ShareClicked : ProfileAction
    data object RetryClicked : ProfileAction
    data object LoadNextPage : ProfileAction
    data object Refresh : ProfileAction
}
