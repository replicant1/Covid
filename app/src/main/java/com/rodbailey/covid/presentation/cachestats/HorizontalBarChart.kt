package com.rodbailey.covid.presentation.cachestats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * One entry in a [HorizontalBarChart].
 *
 * @param label Text shown to the left of the bar (e.g. a country code).
 * @param value Numeric value that determines bar length relative to the maximum entry.
 */
data class BarChartEntry(val label: String, val value: Float)

private val LABEL_WIDTH = 56.dp
private val BAR_HEIGHT = 20.dp
private val ROW_SPACING = 6.dp
private val VALUE_TEXT_PADDING = 6.dp

/**
 * Reusable horizontal bar chart. Each entry is rendered as a labelled bar whose length
 * is proportional to [BarChartEntry.value] relative to the maximum value in [entries].
 *
 * The chart is internally scrollable via [LazyColumn] — callers should **not** place it
 * inside another vertical scroll container.
 *
 * @param entries Data to display. Caller is responsible for ordering.
 * @param modifier Modifier applied to the [LazyColumn] root.
 * @param valueFormatter Converts a [BarChartEntry.value] to the label shown beside the bar.
 *   Defaults to an empty string (no value label).
 */
@Composable
fun HorizontalBarChart(
    entries: List<BarChartEntry>,
    modifier: Modifier = Modifier,
    valueFormatter: (Float) -> String = { "" }
) {
    val maxValue = entries.maxOfOrNull { it.value }?.takeIf { it > 0f } ?: 1f

    LazyColumn(modifier = modifier) {
        items(entries, key = { it.label }) { entry ->
            HorizontalBarRow(
                entry = entry,
                fraction = entry.value / maxValue,
                valueFormatter = valueFormatter
            )
            Spacer(modifier = Modifier.height(ROW_SPACING))
        }
    }
}

@Composable
private fun HorizontalBarRow(
    entry: BarChartEntry,
    fraction: Float,
    valueFormatter: (Float) -> String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(BAR_HEIGHT),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Fixed-width label column so all bars start at the same X position
        Text(
            text = entry.label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(LABEL_WIDTH),
            maxLines = 1
        )

        // Bar — fills remaining width proportionally
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = fraction.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary)
            )
        }

        // Value text to the right of the bar
        val valueText = valueFormatter(entry.value)
        if (valueText.isNotEmpty()) {
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                modifier = Modifier.padding(start = VALUE_TEXT_PADDING),
                maxLines = 1
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(showBackground = true, name = "HorizontalBarChart")
@Composable
private fun PreviewHorizontalBarChart() {
    HorizontalBarChart(
        entries = listOf(
            BarChartEntry("AFG", 2478000f),
            BarChartEntry("AUS", 1620000f),
            BarChartEntry("BRA", 660000f),
            BarChartEntry("CAN", 1440000f),
            BarChartEntry("DEU", 300000f),
        ),
        valueFormatter = { formatAge(it.toLong()) }
    )
}
