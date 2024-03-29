package com.rodbailey.covid.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.tooling.preview.PreviewParameter
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
    Card(
        onClick = clickCallback, modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .testTag(MainScreenTag.TAG_CARD.tag)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
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
                // Region name is title of the data panel
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .testTag(MainScreenTag.TAG_CARD_TITLE.tag)
                )

                RegionDataPanelGrid(reportData)

            } // Column
        } // Box
    } // Card
}

@Preview
@Composable
fun RegionDataPanelPreviewNotLoading(
    @PreviewParameter(ReportDataParameterProvider::class) reportData: ReportData
) {
    RegionDataPanel(
        title = ReportDataParameterProvider.title,
        reportData = reportData,
        clickCallback = {},
        isLoading = false
    )
}

@Preview
@Composable
fun RegionDataPanelPreviewLoading(
    @PreviewParameter(ReportDataParameterProvider::class) reportData: ReportData
) {
    RegionDataPanel(
        title = ReportDataParameterProvider.title,
        reportData = reportData,
        clickCallback = {},
        isLoading = true
    )
}
