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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rodbailey.covid.data.repo.BYTES_PER_STATS_ROW
import com.rodbailey.covid.data.repo.CacheEntry
import com.rodbailey.covid.presentation.Result
import com.rodbailey.covid.presentation.theme.CovidTheme

// ---------------------------------------------------------------------------
// Age formatting
// ---------------------------------------------------------------------------

/**
 * Converts [millis] to a human-readable age string.
 * Examples: "45s", "3m 12s", "2h 05m", "1d 14h".
 */
fun formatAge(millis: Long): String {
    if (millis <= 0L) return "0s"
    val totalSeconds = millis / 1000L
    val days = totalSeconds / 86400L
    val hours = (totalSeconds % 86400L) / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return when {
        days > 0 -> "${days}d ${hours}h"
        hours > 0 -> "${hours}h ${minutes.toString().padStart(2, '0')}m"
        minutes > 0 -> "${minutes}m ${seconds.toString().padStart(2, '0')}s"
        else -> "${seconds}s"
    }
}

private fun formatBytes(bytes: Int): String = when {
    bytes >= 1024 -> "${bytes / 1024} KB"
    else -> "$bytes B"
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
        onDismiss = onDismiss
    )
}

// ---------------------------------------------------------------------------
// Content composable (stateless)
// ---------------------------------------------------------------------------

/**
 * Stateless content for the cache statistics screen.
 *
 * Layout:
 * - [CacheStatsSummaryTable] pinned below the top bar (never scrolls).
 * - [HorizontalBarChart] fills the remaining height with its own independent scroll.
 *
 * @param entries Cache entries sorted by ISO3 code (caller responsible for ordering).
 * @param onDismiss Invoked when the user navigates back.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CacheStatsScreenContent(
    entries: List<CacheEntry>,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    CovidTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Cache Statistics") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
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

                // Bar chart — fills remaining height; LazyColumn inside scrolls independently
                HorizontalBarChart(
                    entries = entries.map {
                        BarChartEntry(label = it.iso3Code, value = it.ageMillis.toFloat())
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    valueFormatter = { formatAge(it.toLong()) }
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
    val count = entries.size
    val totalBytes = count * BYTES_PER_STATS_ROW
    val avgAgeMillis = if (entries.isEmpty()) 0L else entries.map { it.ageMillis }.average().toLong()
    val oldest = entries.maxByOrNull { it.ageMillis }
    val youngest = entries.minByOrNull { it.ageMillis }

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            SummaryRow(label = "Entries", value = count.toString())
            SummaryRow(label = "Total size", value = formatBytes(totalBytes))
            SummaryRow(label = "Average age", value = formatAge(avgAgeMillis))
            SummaryRow(
                label = "Oldest entry",
                value = if (oldest != null) "${oldest.iso3Code} — ${formatAge(oldest.ageMillis)}" else "—"
            )
            SummaryRow(
                label = "Youngest entry",
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
    CacheStatsScreenContent(entries = previewEntries, onDismiss = {})
}

@Preview(showBackground = true, name = "Cache Stats — empty cache")
@Composable
private fun PreviewCacheStatsEmpty() {
    CacheStatsScreenContent(entries = emptyList(), onDismiss = {})
}
