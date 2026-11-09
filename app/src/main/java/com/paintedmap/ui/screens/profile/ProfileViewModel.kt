package com.paintedmap.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paintedmap.data.IUserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

sealed interface AuthorsListState {
    object Loading : AuthorsListState
    data class Content(val list: List<ProfileAuthorUiState>) : AuthorsListState
    data class Base(val list: List<ProfileAuthorUiState>) : AuthorsListState
    object Empty : AuthorsListState
    object EmptySearch : AuthorsListState
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
    val authorsState: AuthorsListState = AuthorsListState.Loading
)

data class ProfileAuthorUiState(
    val id: Int,
    val name: String,
    val nickname: String
)

sealed interface ProfileAction {
    data class SearchAuthorChanged(val search: String) : ProfileAction
    data class AuthorClicked(val author: Int) : ProfileAction
    data object ShareClicked : ProfileAction
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val userId: Int,
    private val userRepository: IUserRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow<String>("")
    private val shareProfile = MutableStateFlow<Boolean>(false)

    val uiState: StateFlow<ProfileUiState> = combine(searchQuery, shareProfile) { query, share ->
        Pair(query, share)
    }
        //.debounce(100L)
        .distinctUntilChanged()
        .flatMapLatest { (query, share) ->
            flow {
                emit(
                    ProfileUiState(
                        search = query,
                        shareProfile = share,
                        authorsState = AuthorsListState.Loading
                    )
                )
                val currentUser = userRepository.findById(userId)

                if (query.isBlank()) {
                    val baseAuthors = currentUser?.getAuthors()?.mapNotNull { authorId ->
                        userRepository.findById(authorId)
                    }?.map { author ->
                        ProfileAuthorUiState(
                            id = author.userId,
                            name = author.userName,
                            nickname = author.userNickName
                        )
                    } ?: emptyList()

                    val finalState = if (baseAuthors.isEmpty()) {
                        AuthorsListState.Empty
                    } else {
                        AuthorsListState.Base(baseAuthors)
                    }

                    emit(
                        ProfileUiState(
                            userId = currentUser?.userId ?: 0,
                            shareProfile = share,
                            search = query,
                            userName = currentUser?.userName ?: "",
                            userNickName = currentUser?.userNickName ?: "",
                            userSubAmount = currentUser?.amountOfSubs() ?: 0,
                            userAuthorsAmount = currentUser?.amountOfAuthors() ?: 0,
                            userRegionsAmount = currentUser?.amountOfRegions() ?: 0,
                            authorsState = finalState
                        )
                    )
                    return@flow
                }

                val foundUsers = userRepository.searchAllUsers(query)

                if (foundUsers.isEmpty()) {
                    emit(
                        ProfileUiState(
                            userId = currentUser?.userId ?: 0,
                            shareProfile = share,
                            search = query,
                            userName = currentUser?.userName ?: "",
                            userNickName = currentUser?.userNickName ?: "",
                            userSubAmount = currentUser?.amountOfSubs() ?: 0,
                            userAuthorsAmount = currentUser?.amountOfAuthors() ?: 0,
                            userRegionsAmount = currentUser?.amountOfRegions() ?: 0,
                            authorsState = AuthorsListState.EmptySearch
                        )
                    )
                    return@flow
                }

                val filteredAndSorted = foundUsers
                    .filter { it.userId != userId }
                    .sortedByDescending { searchedUser ->
                        currentUser?.getAuthors()?.contains(searchedUser.userId) ?: false
                    }
                    .map { author ->
                        ProfileAuthorUiState(
                            id = author.userId,
                            name = author.userName,
                            nickname = author.userNickName
                        )
                    }

                val finalAuthorsState = if (filteredAndSorted.isEmpty()) {
                    AuthorsListState.EmptySearch
                } else {
                    AuthorsListState.Content(filteredAndSorted)
                }

                emit(
                    ProfileUiState(
                        userId = currentUser?.userId ?: 0,
                        shareProfile = share,
                        search = query,
                        userName = currentUser?.userName ?: "",
                        userNickName = currentUser?.userNickName ?: "",
                        userSubAmount = currentUser?.amountOfSubs() ?: 0,
                        userAuthorsAmount = currentUser?.amountOfAuthors() ?: 0,
                        userRegionsAmount = currentUser?.amountOfRegions() ?: 0,
                        authorsState = finalAuthorsState
                    )
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfileUiState()
        )

    fun onAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.SearchAuthorChanged -> {
                searchQuery.value = action.search
            }
            ProfileAction.ShareClicked -> {
                shareProfile.value = !shareProfile.value
            }
            is ProfileAction.AuthorClicked -> {}
        }
    }
}
