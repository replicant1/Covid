package com.rodbailey.covid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rodbailey.covid.dom.Region
import com.rodbailey.covid.dom.ReportData
import com.rodbailey.covid.net.CovidAPI
import com.rodbailey.covid.net.CovidAPIClient
import com.rodbailey.covid.ui.MainViewModel
import com.rodbailey.covid.ui.theme.CovidTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CovidTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val searchText by viewModel.searchText.collectAsState()
                    val regions by viewModel.regions.collectAsState()
                    val isSearching by viewModel.isSearching.collectAsState()
                    val reportData by viewModel.reportData.collectAsState()
                    val reportDataTitle by viewModel.reportDataTitle.collectAsState()

                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        TextField(
                            value = searchText,
                            onValueChange = viewModel::onSearchTextChanged,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(text = "Search country") }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(regions) { region ->
                                RegionSearchResultItem(region = region, viewModel)
                            }
                        }
                        // ReportDataPanel here - toggle visibility
                        RegionDataPanel(title = reportDataTitle, reportData = reportData)
                    }
                }
            }
        }
    }
}

//@Preview
//@Composable
//fun RegionSearchResultItemPreview() {
//    RegionSearchResultItem(region = Region("ELO", "Electric Light Orchestra", view))
//}

// TODO Pass click lambda instead of viewModel then restore @Preview
@Composable
fun RegionSearchResultItem(region: Region, viewModel: MainViewModel) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .clickable {
                println("*** Clicked on ${region.name} ***")
                viewModel.loadReportDataForRegion(region.iso3Code)
            }) {
        Icon(
            modifier = Modifier.padding(end = 8.dp),
            imageVector = Icons.Default.AccountBox,
            contentDescription = "Icon"
        )
        Text(
            text = "${region.name}"
        )
    }
}

@Preview
@Composable
fun RegionDataPanelPreview() {
    RegionDataPanel(
        title = "Big Country",
        reportData = ReportData(
            confirmed = 10000L,
            deaths = 200L,
            recovered = 1234L,
            active = 9876L,
            fatalityRate = 0.56F
        )
    )
}

@Composable
fun RowScope.TableCell(text: String, weight: Float) {
    Text(
        text = text,
        modifier = Modifier
//            .border(1.dp, Color.Black)
            .weight(weight)
            .padding(8.dp)
    )
}

@Composable
fun RegionDataPanel(title: String, reportData: ReportData) {
    val tableData = mutableListOf<Pair<String, String>>()
    tableData.add(Pair("Confirmed:", "${reportData.confirmed}"))
    tableData.add(Pair("Deaths:", "${reportData.deaths}"))
    tableData.add(Pair("Active:", "${reportData.active}"))
    tableData.add(Pair("Fatality Rate:", "${reportData.fatalityRate}"))

    Column() {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 24.dp, bottom = 8.dp, top = 16.dp, end = 16.dp)
        )
        LazyColumn(
            Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {

            items(tableData) {
                val (id, text) = it
                Row() {
                    TableCell(text = id.toString(), weight = .2f)
                    TableCell(text = text, weight = .2f)
                }
            }
        }
    }
}