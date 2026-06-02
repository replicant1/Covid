package com.rodbailey.covid.presentation.cachestats

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rodbailey.covid.R
import com.rodbailey.covid.data.repo.BYTES_PER_STATS_ROW
import com.rodbailey.covid.data.repo.CacheEntry
import com.rodbailey.covid.presentation.Result

// ---------------------------------------------------------------------------
// Age formatting
// ---------------------------------------------------------------------------

/**
 * Converts [millis] to a human-readable age string.
 * Examples: "45s", "3m 12s", "2h 05m 30s", "1d 14h 30m 05s".
 */
fun formatAge(millis: Long): String {
    if (millis <= 0L) return "0s"
    val totalSeconds = millis / 1000L
    val days = totalSeconds / 86400L
    val hours = (totalSeconds % 86400L) / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return when {
        days > 0 -> "${days}d ${hours}h ${minutes.toString().padStart(2, '0')}m ${seconds.toString().padStart(2, '0')}s"
        hours > 0 -> "${hours}h ${minutes.toString().padStart(2, '0')}m ${seconds.toString().padStart(2, '0')}s"
        minutes > 0 -> "${minutes}m ${seconds.toString().padStart(2, '0')}s"
        else -> "${seconds}s"
    }
}

private fun formatBytes(bytes: Int): String = when {
    bytes >= 1024 -> "${bytes / 1024} KB"
    else -> "$bytes B"
}

// ---------------------------------------------------------------------------
// Sort
// ---------------------------------------------------------------------------

/** The four orderings available to the user via the sort control. */
enum class SortOption {
    ISO_CODE_ASC {
        @Composable override fun label() = stringResource(R.string.sort_option_iso_code_asc)
    },
    ISO_CODE_DESC {
        @Composable override fun label() = stringResource(R.string.sort_option_iso_code_desc)
    },
    AGE_ASC {
        @Composable override fun label() = stringResource(R.string.sort_option_age_asc)
    },
    AGE_DESC {
        @Composable override fun label() = stringResource(R.string.sort_option_age_desc)
    };

    @Composable abstract fun label(): String
}

private fun List<CacheEntry>.applySortOption(option: SortOption): List<CacheEntry> = when (option) {
    SortOption.ISO_CODE_ASC  -> sortedBy { it.iso3Code }
    SortOption.ISO_CODE_DESC -> sortedByDescending { it.iso3Code }
    SortOption.AGE_ASC       -> sortedBy { it.ageMillis }
    SortOption.AGE_DESC      -> sortedByDescending { it.ageMillis }
}

// ---------------------------------------------------------------------------
// Root composable (stateful)
// ---------------------------------------------------------------------------

/**
 * Stateful entry point for the cache statistics screen. Owns the [CacheStatsViewModel]
 * and delegates rendering to [CacheStatsScreenContent].
 *
 * @param onDismiss Invoked when the user navigates back (back button or system gesture).
 */
@Composable
fun CacheStatsScreen(
    onDismiss: () -> Unit,
    viewModel: CacheStatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val entries = (uiState.entries as? Result.Success)?.data ?: emptyList()

    CacheStatsScreenContent(
        entries = entries,
        sortOption = uiState.sortOption,
        onSortOptionSelected = viewModel::setSortOption,
        onDismiss = onDismiss
    )
}

// ---------------------------------------------------------------------------
// Content composable (stateless)
// ---------------------------------------------------------------------------

/**
 * Stateless content for the cache statistics screen.
 *
 * Layout (top to bottom, nothing scrolls except the bar chart's own LazyColumn):
 * - [CacheStatsSummaryTable] — pinned below the top bar.
 * - [SortControl] — dropdown for choosing the bar chart's sort order.
 * - [HorizontalBarChart] — fills remaining height, scrolls independently.
 *
 * @param entries Raw cache entries from the repository (unsorted).
 * @param sortOption Currently active sort order, owned by the caller.
 * @param onSortOptionSelected Invoked when the user picks a new sort order.
 * @param onDismiss Invoked when the user navigates back.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CacheStatsScreenContent(
    entries: List<CacheEntry>,
    sortOption: SortOption,
    onSortOptionSelected: (SortOption) -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    val sortedEntries = remember(entries, sortOption) { entries.applySortOption(sortOption) }
    val barChartEntries = remember(sortedEntries) {
        sortedEntries.map { BarChartEntry(label = it.iso3Code, value = it.ageMillis.toFloat()) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.cache_stats_title)) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cache_stats_back_button)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Summary table — fixed, not scrollable
            CacheStatsSummaryTable(
                entries = entries,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sort control — fixed, not scrollable
            SortControl(
                selected = sortOption,
                onOptionSelected = onSortOptionSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bar chart — fills remaining height; LazyColumn inside scrolls independently
            HorizontalBarChart(
                entries = barChartEntries,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                valueFormatter = { formatAge(it.toLong()) }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Sort control
// ---------------------------------------------------------------------------

/**
 * Dropdown control that lets the user choose how the bar chart is ordered.
 *
 * @param selected      The currently active [SortOption].
 * @param onOptionSelected Callback invoked when the user picks a different option.
 * @param modifier      Applied to the [ExposedDropdownMenuBox] root.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortControl(
    selected: SortOption,
    onOptionSelected: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected.label(),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.cache_stats_sort_control_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SortOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label()) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Summary table
// ---------------------------------------------------------------------------

@Composable
fun CacheStatsSummaryTable(
    entries: List<CacheEntry>,
    modifier: Modifier = Modifier
) {
    val count = remember(entries) { entries.size }
    val totalBytes = remember(entries) { count * BYTES_PER_STATS_ROW }
    val avgAgeMillis = remember(entries) { if (entries.isEmpty()) 0L else entries.sumOf { it.ageMillis } / count }
    val oldest = remember(entries) { entries.maxByOrNull { it.ageMillis } }
    val youngest = remember(entries) { entries.minByOrNull { it.ageMillis } }

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            SummaryRow(label = stringResource(R.string.cache_stats_summary_entries_label), value = count.toString())
            SummaryRow(label = stringResource(R.string.cache_stats_summary_total_size_label), value = formatBytes(totalBytes))
            SummaryRow(label = stringResource(R.string.cache_stats_summary_average_age_label), value = formatAge(avgAgeMillis))
            SummaryRow(
                label = stringResource(R.string.cache_stats_summary_oldest_entry_label),
                value = if (oldest != null) "${oldest.iso3Code} — ${formatAge(oldest.ageMillis)}" else "—"
            )
            SummaryRow(
                label = stringResource(R.string.cache_stats_summary_youngest_entry_label),
                value = if (youngest != null) "${youngest.iso3Code} — ${formatAge(youngest.ageMillis)}" else "—"
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

private val previewEntries = listOf(
    CacheEntry("AFG", 2_478_000L),
    CacheEntry("AUS", 1_620_000L),
    CacheEntry("BRA",   660_000L),
    CacheEntry("CAN", 1_440_000L),
    CacheEntry("DEU",   300_000L),
    CacheEntry("EGY", 2_330_000L),
    CacheEntry("FRA",   900_000L),
)

@Preview(showBackground = true, name = "Cache Stats — with data")
@Composable
private fun PreviewCacheStatsWithData() {
    CacheStatsScreenContent(
        entries = previewEntries,
        sortOption = SortOption.ISO_CODE_ASC,
        onSortOptionSelected = {},
        onDismiss = {}
    )
}

@Preview(showBackground = true, name = "Cache Stats — empty cache")
@Composable
private fun PreviewCacheStatsEmpty() {
    CacheStatsScreenContent(
        entries = emptyList(),
        sortOption = SortOption.ISO_CODE_ASC,
        onSortOptionSelected = {},
        onDismiss = {}
    )
}
