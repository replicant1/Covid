package com.rodbailey.covid.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.rodbailey.covid.domain.ReportData

@Composable
fun RegionDataPanelGrid(reportData: ReportData) {
    ConstraintLayout {
        val (fieldLabelConfirmed, fieldLabelDeaths, fieldLabelActive, fieldLabelFatalityRate,
            fieldValueConfirmed, fieldValueDeaths, fieldValueAcdtive, fieldValueFatalityRate) = createRefs()

        Text(
            text = "Confirmed Cases:",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp, end = 8.dp).constrainAs(fieldLabelConfirmed) {
                start.linkTo(parent.start)
                top.linkTo(parent.top)
            }
        )

        Text(
            text = "Deaths:",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp, end = 8.dp).constrainAs(fieldLabelDeaths) {
                start.linkTo(parent.start)
                top.linkTo(fieldLabelConfirmed.bottom)
            }
        )

        Text(
            text = "Active Cases:",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp, end = 8.dp).constrainAs(fieldLabelActive) {
                start.linkTo(parent.start)
                top.linkTo(fieldLabelDeaths.bottom)
            }
        )

        Text(
            text = "Fatality Rate:",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(end = 8.dp).constrainAs(fieldLabelFatalityRate) {
                start.linkTo(parent.start)
                top.linkTo(fieldLabelActive.bottom)
            }
        )

        val barrier = createEndBarrier(fieldLabelConfirmed, fieldLabelDeaths, fieldLabelActive, fieldLabelFatalityRate)

        Text(
            text = "${reportData.confirmed}",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp).constrainAs(fieldValueConfirmed) {
                start.linkTo(barrier)
            }
        )
        
        Text (
            text = "${reportData.deaths}",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp).constrainAs(fieldValueDeaths) {
                start.linkTo(barrier)
                top.linkTo(fieldValueConfirmed.bottom)
            }
        )

        Text(
            text = "${reportData.active}",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp).constrainAs(fieldValueAcdtive) {
                start.linkTo(barrier)
                top.linkTo(fieldValueDeaths.bottom)
            }
        )
        
        Text(
            text = "${reportData.fatalityRate}",
            fontSize = 16.sp,
            modifier = Modifier.constrainAs(fieldValueFatalityRate) {
                start.linkTo(barrier)
                top.linkTo(fieldValueAcdtive.bottom)
            }

        )
    } // ConstraintLayout
}

@Preview
@Composable
fun RegionDataPanelGridPreview() {
    RegionDataPanelGrid(reportData = ReportData(
        confirmed =  100L,
        deaths = 200L,
        active = 300L,
        fatalityRate = 0.34F
    ))
}