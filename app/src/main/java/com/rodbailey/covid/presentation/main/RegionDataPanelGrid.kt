package com.rodbailey.covid.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.rodbailey.covid.R
import com.rodbailey.covid.domain.ReportData

/**
 * 2 column table, 4 rows, showing data in a single [ReportData] instance.
 * Both columns are left-aligned.
 */
@Composable
fun RegionDataPanelGrid(reportData: ReportData) {
    val confirmedLabel = stringResource(R.string.data_field_confirmed)
    val deathsLabel = stringResource(R.string.data_field_deaths)
    val activeLabel = stringResource(R.string.data_field_active)
    val fatalityRateLabel = stringResource(R.string.data_field_fatality_rate)

    ConstraintLayout {
        val (fieldLabelConfirmed, fieldLabelDeaths, fieldLabelActive, fieldLabelFatalityRate,
            fieldValueConfirmed, fieldValueDeaths, fieldValueActive, fieldValueFatalityRate) = createRefs()

        // "Confirmed:" field label — contentDescription merges label+value for TalkBack
        Text(
            text = confirmedLabel,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .semantics { contentDescription = "$confirmedLabel ${reportData.confirmed}" }
                .padding(bottom = 8.dp, end = 16.dp)
                .constrainAs(fieldLabelConfirmed) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                }
        )

        // "Deaths:" field label
        Text(
            text = deathsLabel,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .semantics { contentDescription = "$deathsLabel ${reportData.deaths}" }
                .padding(bottom = 8.dp, end = 16.dp)
                .constrainAs(fieldLabelDeaths) {
                    start.linkTo(parent.start)
                    top.linkTo(fieldLabelConfirmed.bottom)
                }
        )

        // "Active:" field label
        Text(
            text = activeLabel,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .semantics { contentDescription = "$activeLabel ${reportData.active}" }
                .padding(bottom = 8.dp, end = 16.dp)
                .constrainAs(fieldLabelActive) {
                    start.linkTo(parent.start)
                    top.linkTo(fieldLabelDeaths.bottom)
                }
        )

        // "Fatality Rate:" field label
        Text(
            text = fatalityRateLabel,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .semantics { contentDescription = "$fatalityRateLabel ${reportData.fatalityRate}%" }
                .padding(end = 16.dp)
                .constrainAs(fieldLabelFatalityRate) {
                    start.linkTo(parent.start)
                    top.linkTo(fieldLabelActive.bottom)
                }
        )

        // Vertical barrier is automatically located at right-most end of the four field labels.
        // Numbers in the second column are left-aligned to the barrier.
        val barrier = createEndBarrier(fieldLabelConfirmed, fieldLabelDeaths, fieldLabelActive, fieldLabelFatalityRate)

        // "Confirmed" field value — hidden from TalkBack; announced via label's contentDescription
        Text(
            text = "${reportData.confirmed}",
            fontSize = 16.sp,
            modifier = Modifier
                .semantics { hideFromAccessibility() }
                .padding(bottom = 8.dp)
                .constrainAs(fieldValueConfirmed) {
                    start.linkTo(barrier)
                }
        )

        // "Deaths" field value
        Text(
            text = "${reportData.deaths}",
            fontSize = 16.sp,
            modifier = Modifier
                .semantics { hideFromAccessibility() }
                .padding(bottom = 8.dp)
                .constrainAs(fieldValueDeaths) {
                    start.linkTo(barrier)
                    top.linkTo(fieldValueConfirmed.bottom)
                }
        )

        // "Active" field value
        Text(
            text = "${reportData.active}",
            fontSize = 16.sp,
            modifier = Modifier
                .semantics { hideFromAccessibility() }
                .padding(bottom = 8.dp)
                .constrainAs(fieldValueActive) {
                    start.linkTo(barrier)
                    top.linkTo(fieldValueDeaths.bottom)
                }
        )

        // "Fatality Rate" field value
        Text(
            text = "${reportData.fatalityRate}",
            fontSize = 16.sp,
            modifier = Modifier
                .semantics { hideFromAccessibility() }
                .constrainAs(fieldValueFatalityRate) {
                    start.linkTo(barrier)
                    top.linkTo(fieldValueActive.bottom)
                }
        )
    } // ConstraintLayout
}

@Preview
@Composable
fun RegionDataPanelGridPreview(
    @PreviewParameter(ReportDataParameterProvider::class) reportData: ReportData
) {
    RegionDataPanelGrid(reportData)
}
