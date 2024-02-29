package com.rodbailey.covid.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodbailey.covid.domain.ReportData


/**
 * Panel of covid statistics for a particular region. Shows confirmed, deaths, active and
 * fatality rate.
 *
 * @param title Name of the region - appears above data table
 * @param reportData Covid stats to put in the data table
 * @param clickCallback Invoked when user clicks on this panel
 * @param isLoading true if the data to be displayed is still loading, so show a progress monitor
 * instead of the data table.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionDataPanel(
    title: String,
    reportData: ReportData,
    clickCallback: () -> Unit,
    isLoading: Boolean
) {
    val tableData = mutableListOf<Pair<String, String>>()
    tableData.add(Pair("Confirmed:", "${reportData.confirmed}"))
    tableData.add(Pair("Deaths:", "${reportData.deaths}"))
    tableData.add(Pair("Active:", "${reportData.active}"))
    tableData.add(Pair("Fatality Rate:", "${reportData.fatalityRate}"))

    Card(onClick = clickCallback, modifier = Modifier
        .padding(horizontal = 16.dp)
        .testTag("tag.card")) {
        Box() {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(64.dp)
                    .alpha(if (isLoading) 1f else 0f)
                    .align(Alignment.Center),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Column(
                modifier = Modifier.alpha(if (isLoading) 0f else 1f)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .padding(start = 24.dp, bottom = 8.dp, top = 16.dp, end = 16.dp)
                        .testTag("tag.card.title")
                )
                LazyColumn(
                    Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {

                    items(tableData) {
                        val (id, text) = it
                        Row() {
                            TableCell(text = id, weight = .2f)
                            TableCell(text = text, weight = .2f)
                        }
                    } // items
                } // LazyColumn
            } // Column
        } // Box
    } // Card
}

/**
 * A cell in the data table part of a [RegionDataPanel]
 * @param text Textual contents of the table cell
 * @param weight Proportional width of this cell
 */
@Composable
fun RowScope.TableCell(text: String, weight: Float) {
    Text(
        fontSize = 16.sp,
        text = text,
        modifier = Modifier
            .weight(weight)
            .padding(8.dp)
    )
}

@Preview
@Composable
fun RegionDataPanelPreviewNotLoading() {
    RegionDataPanel(
        title = "Big Country",
        reportData = ReportData(
            confirmed = 10000L,
            deaths = 200L,
            recovered = 1234L,
            active = 9876L,
            fatalityRate = 0.56F
        ),
        clickCallback = {},
        isLoading = false
    )
}

@Preview
@Composable
fun RegionDataPanelPreviewLoading() {
    RegionDataPanel(
        title = "Big Country",
        reportData = ReportData(
            confirmed = 10000L,
            deaths = 200L,
            recovered = 1234L,
            active = 9876L,
            fatalityRate = 0.56F
        ),
        clickCallback = {},
        isLoading = true
    )
}
