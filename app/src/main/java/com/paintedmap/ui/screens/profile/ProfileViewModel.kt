package com.paintedmap.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paintedmap.data.IUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ProfileUiState(
    val userId: Int = 0,
    val shareProfile: Boolean = false,
    val userName: String = "",
    val userNickName: String = "",
    val search: String = "",
    val userSubAmount: Int = 0,
    val userAuthorsAmount: Int = 0,
    val userRegionsAmount: Int = 0,
    val userAuthorsList: List<ProfileAuthorUiState> = emptyList()
)

data class ProfileAuthorUiState(
    val id: Int,
    val name: String,
    val nickname: String
)

sealed interface ProfileAction {
    data class SearchAuthorChanged(val search: String) : ProfileAction
    data object ShareClicked : ProfileAction
}

class ProfileViewModel(
    private val userId: Int,
    private val userRepository: IUserRepository
) : ViewModel() {
    private val searchQuery = MutableStateFlow<String>("")
    private val shareProfile = MutableStateFlow<Boolean>(false)

    val uiState: StateFlow<ProfileUiState> = combine(
        searchQuery, shareProfile
    ) { query, share ->
        val user = userRepository.findById(userId)

        val filtered = user?.getAuthors()?.mapNotNull { authorId ->
            userRepository.findById(authorId)
        }?.filter { author ->
            author.userName.contains(query, ignoreCase = true) ||
                    author.userNickName.contains(query, ignoreCase = true)
        }?.map { author ->
            ProfileAuthorUiState(
                id = author.userId,
                name = author.userName,
                nickname = author.userNickName
            )
        } ?: emptyList()

        ProfileUiState(
            userId = user?.userId ?: 0,
            shareProfile = share,
            search = query,
            userName = user?.userName ?: "",
            userNickName = user?.userNickName ?: "",
            userSubAmount = user?.amountOfSubs() ?: 0,
            userAuthorsAmount = user?.amountOfAuthors() ?: 0,
            userRegionsAmount = user?.amountOfRegions() ?: 0,
            userAuthorsList = filtered
        )
    }.stateIn(
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
        }
    }
}
