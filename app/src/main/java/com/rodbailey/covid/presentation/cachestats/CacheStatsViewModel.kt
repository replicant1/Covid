package com.rodbailey.covid.presentation.cachestats

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodbailey.covid.data.repo.CacheEntry
import com.rodbailey.covid.data.repo.CovidRepository
import com.rodbailey.covid.presentation.Result
import com.rodbailey.covid.presentation.asResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val PREFS_FILE = "cache_stats_prefs"
private const val PREF_SORT_OPTION = "sort_option"

@HiltViewModel
class CacheStatsViewModel @Inject constructor(
    repo: CovidRepository,
    @ApplicationContext context: Context
) : ViewModel() {

    data class UIState(
        val entries: Result<List<CacheEntry>> = Result.Loading,
        val sortOption: SortOption = SortOption.ISO_CODE_ASC
    )

    private val prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)

    // Read once so both _sortOption and the stateIn initialValue agree on the starting sort.
    private val initialSortOption: SortOption =
        SortOption.entries.firstOrNull { it.name == prefs.getString(PREF_SORT_OPTION, null) }
            ?: SortOption.ISO_CODE_ASC

    private val _sortOption = MutableStateFlow(initialSortOption)

    val uiState: StateFlow<UIState> = combine(
        repo.getCacheEntriesStream().asResult(),
        _sortOption
    ) { entriesResult, sort ->
        UIState(entries = entriesResult, sortOption = sort)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = UIState(sortOption = initialSortOption)
    )

    /** Updates the active sort option and persists it to SharedPreferences. */
    fun setSortOption(option: SortOption) {
        _sortOption.value = option
        prefs.edit().putString(PREF_SORT_OPTION, option.name).apply()
    }
}