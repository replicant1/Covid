package com.rodbailey.covid.presentation.cachestats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodbailey.covid.data.repo.CacheEntry
import com.rodbailey.covid.data.repo.CovidRepository
import com.rodbailey.covid.presentation.Result
import com.rodbailey.covid.presentation.asResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CacheStatsViewModel @Inject constructor(
    repo: CovidRepository
) : ViewModel() {

    data class UIState(
        val entries: Result<List<CacheEntry>> = Result.Loading
    )

    val uiState: StateFlow<UIState> =
        repo.getCacheEntriesStream()
            .asResult()
            .map { UIState(entries = it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = UIState()
            )
}
