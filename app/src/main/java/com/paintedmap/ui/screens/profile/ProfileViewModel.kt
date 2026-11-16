package com.paintedmap.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paintedmap.domain.model.LoadResult
import com.paintedmap.domain.model.NetworkError
import com.paintedmap.domain.model.User
import com.paintedmap.domain.model.UserPage
import com.paintedmap.domain.model.userMessage
import com.paintedmap.domain.repository.UserRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class ProfileViewModel(
    private val userId: Int,
    private val userRepository: UserRepository,
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")
    private val _uiState = MutableStateFlow(ProfileUiState(userId = userId))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var nextPageJob: Job? = null
    private var rateLimitJob: Job? = null

    init {
        viewModelScope.launch {
            searchQuery
                .debounce(300L)
                .distinctUntilChanged()
                .collectLatest { query ->
                    loadInitialData(query)
                }
        }
    }

    fun onAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.SearchAuthorChanged -> {
                _uiState.update { it.copy(search = action.search) }
                searchQuery.value = action.search
            }
            ProfileAction.ShareClicked -> {
                _uiState.update { it.copy(shareProfile = !it.shareProfile) }
            }
            ProfileAction.RetryClicked -> {
                if (!_uiState.value.isRetryBlocked) {
                    loadInitialData(searchQuery.value)
                }
            }
            ProfileAction.LoadNextPage -> {
                loadNextPage()
            }
            ProfileAction.Refresh -> {
                if (!_uiState.value.isRetryBlocked) {
                    loadInitialData(searchQuery.value)
                }
            }
            is ProfileAction.AuthorClicked -> Unit
        }
    }

    private fun loadInitialData(query: String) {
        loadJob?.cancel()
        nextPageJob?.cancel()

        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(authorsState = AuthorsListState.Loading) }
            coroutineScope {
                val userDeferred = async { userRepository.getUserById(userId) }
                val listDeferred = async {
                    if (query.isBlank()) {
                        userRepository.getSubscriptions(userId = userId, cursor = null)
                    } else {
                        userRepository.searchUsers(query = query, cursor = null)
                    }
                }

                val userResult = userDeferred.await()
                val listResult = listDeferred.await()

                val loadedUser: User? = when (userResult) {
                    is LoadResult.Success -> userResult.value
                    is LoadResult.Failure -> null
                }

                if (userResult is LoadResult.Failure && listResult is LoadResult.Failure) {
                    handleRateLimitIfNeeded(userResult.error)
                    _uiState.update {
                        it.copy(
                            authorsState = AuthorsListState.Error(
                                error = userResult.error,
                                message = userResult.error.userMessage()
                            )
                        )
                    }
                    return@coroutineScope
                }

                val newAuthorsState = when (listResult) {
                    is LoadResult.Success -> mapPageToState(listResult.value, query)
                    is LoadResult.Failure -> {
                        handleRateLimitIfNeeded(listResult.error)
                        AuthorsListState.Error(
                            error = listResult.error,
                            message = listResult.error.userMessage()
                        )
                    }
                }

                _uiState.update { state ->
                    state.copy(
                        userId = loadedUser?.userId ?: state.userId,
                        userName = loadedUser?.userName ?: state.userName,
                        userNickName = loadedUser?.userNickName ?: state.userNickName,
                        userSubAmount = loadedUser?.userSubsCount ?: state.userSubAmount,
                        userAuthorsAmount = loadedUser?.userAuthorsCount ?: state.userAuthorsAmount,
                        userRegionsAmount = loadedUser?.userRegionsCount ?: state.userRegionsAmount,
                        authorsState = newAuthorsState
                    )
                }
            }
        }
    }

    private fun loadNextPage() {
        val currentAuthors = _uiState.value.authorsState as? AuthorsListState.Content ?: return
        val cursor = currentAuthors.nextCursor ?: return
        if (currentAuthors.isLoadingNextPage || _uiState.value.isRetryBlocked) return

        nextPageJob?.cancel()
        nextPageJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    authorsState = currentAuthors.copy(
                        isLoadingNextPage = true,
                        paginationError = null
                    )
                )
            }

            val query = searchQuery.value
            val result = if (query.isBlank()) {
                userRepository.getSubscriptions(userId = userId, cursor = cursor)
            } else {
                userRepository.searchUsers(query = query, cursor = cursor)
            }

            when (result) {
                is LoadResult.Success -> {
                    val newItems = result.value.items
                        .filter { it.userId != userId }
                        .map { ProfileAuthorUiState(it.userId, it.userName, it.userNickName) }

                    _uiState.update {
                        it.copy(
                            authorsState = currentAuthors.copy(
                                list = currentAuthors.list + newItems,
                                nextCursor = result.value.nextCursor,
                                isLoadingNextPage = false,
                                paginationError = null
                            )
                        )
                    }
                }
                is LoadResult.Failure -> {
                    handleRateLimitIfNeeded(result.error)
                    _uiState.update {
                        it.copy(
                            authorsState = currentAuthors.copy(
                                isLoadingNextPage = false,
                                paginationError = result.error
                            )
                        )
                    }
                }
            }
        }
    }

    private fun mapPageToState(page: UserPage, query: String): AuthorsListState {
        val mapped = page.items
            .filter { it.userId != userId }
            .map { ProfileAuthorUiState(it.userId, it.userName, it.userNickName) }

        return when {
            mapped.isEmpty() && query.isBlank() -> AuthorsListState.Empty
            mapped.isEmpty() && query.isNotBlank() -> AuthorsListState.EmptySearch
            else -> AuthorsListState.Content(
                list = mapped,
                nextCursor = page.nextCursor
            )
        }
    }

    private fun handleRateLimitIfNeeded(error: NetworkError) {
        if (error is NetworkError.RateLimited) {
            rateLimitJob?.cancel()
            val delayMs = error.retryAfter?.inWholeMilliseconds ?: 5_000L
            rateLimitJob = viewModelScope.launch {
                _uiState.update { it.copy(isRetryBlocked = true) }
                delay(delayMs)
                _uiState.update { it.copy(isRetryBlocked = false) }
            }
        }
    }
}