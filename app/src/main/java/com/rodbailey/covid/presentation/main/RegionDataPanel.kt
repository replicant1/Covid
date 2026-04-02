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
import com.rodbailey.covid.presentation.MainViewModel
import com.rodbailey.covid.presentation.core.UIText


/**
 * Panel of covid statistics for a particular region. Shows confirmed, deaths, active and
 * fatality rate.
 *
 * @param dataPanelUIState Current state of the data panel — loading or open with data
 * @param clickCallback Invoked when user clicks on this panel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionDataPanel(
    dataPanelUIState: MainViewModel.DataPanelUIState,
    clickCallback: () -> Unit
) {
    val isLoading = dataPanelUIState is MainViewModel.DataPanelUIState.DataPanelOpenWithLoading
    val title = (dataPanelUIState as? MainViewModel.DataPanelUIState.DataPanelOpenWithData)
        ?.reportDataTitle?.asString() ?: ""
    val reportData = (dataPanelUIState as? MainViewModel.DataPanelUIState.DataPanelOpenWithData)
        ?.reportData ?: ReportData()

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
        dataPanelUIState = MainViewModel.DataPanelUIState.DataPanelOpenWithData(
            reportDataTitle = UIText.DynamicString(ReportDataParameterProvider.title),
            reportData = reportData
        ),
        clickCallback = {}
    )
}

@Preview
@Composable
fun RegionDataPanelPreviewLoading() {
    RegionDataPanel(
        dataPanelUIState = MainViewModel.DataPanelUIState.DataPanelOpenWithLoading,
        clickCallback = {}
    )
}
