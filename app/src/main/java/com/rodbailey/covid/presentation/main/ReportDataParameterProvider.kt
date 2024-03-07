package com.rodbailey.covid.presentation.main

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.rodbailey.covid.domain.ReportData

class ReportDataParameterProvider  : PreviewParameterProvider<ReportData> {
    companion object {
        const val title = "United Arab Emirates"
    }
    override val values = sequenceOf(
        ReportData(
            confirmed = 10000L,
            deaths = 200L,
            recovered = 1234L,
            active = 9876L,
            fatalityRate = 0.56F
        )
    )
}